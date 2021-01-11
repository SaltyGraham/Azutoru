package me.aztl.azutoru.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthSmash.State;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.combo.Crumble;
import me.aztl.azutoru.ability.fire.combo.FireStreams;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.WorldUtil;

public class RaiseEarth extends EarthAbility implements AddonAbility {

	public static enum RaiseEarthState {
		RAISE, THROW;
	}
	
	public static enum RaiseEarthShape {
		COLUMN, WALL;
	}

	private static Map<Block, RaiseEarth> affectedBlocks = new ConcurrentHashMap<>();
	private static Map<RaiseEarth, List<Column>> columnsByInstance = new ConcurrentHashMap<>();
	private static double radius = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.CollisionRadius");
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.COOLDOWN)
	private long throwCooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.HEIGHT)
	private int height;
	@Attribute(Attribute.SPEED)
	private int speed;
	@Attribute(Attribute.WIDTH)
	private int width;
	private boolean throwEnabled;

	private List<Column> columns = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Set<Entity> affectedEntities = new HashSet<>();
	private RaiseEarth instance;
	private Location origin, location;
	private Vector direction;
	private Column column;
	private Block source;
	private BlockFace face;
	private RaiseEarthState state;
	private RaiseEarthShape shape;
	private int counter;
	
	public RaiseEarth(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBend(this)
				|| hasAbility(player, RaiseEarth.class)
				|| !player.getEyeLocation().getBlock().isPassable())
			return;
		
		setFields();
		
		state = type != ClickType.RIGHT_CLICK ? RaiseEarthState.RAISE : RaiseEarthState.THROW;
		shape = type == ClickType.LEFT_CLICK ? RaiseEarthShape.COLUMN : RaiseEarthShape.WALL;
		
		instance = this;
		Block sourceBlock = BlockSource.getEarthSourceBlock(player, sourceRange, type);
		
		if (state == RaiseEarthState.THROW)
			sourceBlock = player.getTargetBlock(null, (int) sourceRange);
		
		if (sourceBlock == null || !isEarthbendable(sourceBlock)) return;
		
		if (state == RaiseEarthState.RAISE) {
			if (bPlayer.isOnCooldown(columnString())
					|| bPlayer.isOnCooldown(wallString())
					|| affectedBlocks.containsKey(sourceBlock))
				return;
			
			List<Block> targetBlocks = player.getLastTwoTargetBlocks(null, (int) sourceRange);
			if (targetBlocks.size() <= 1) return;
			
			source = targetBlocks.get(1);
			face = source.getFace(targetBlocks.get(0));
			height = getEarthbendableBlocksLength(source, MathUtil.getFaceDirection(face).clone().multiply(-1), height);
			if (height < 1) return;

			bPlayer.addCooldown(shape == RaiseEarthShape.COLUMN ? columnString() : wallString(), cooldown);
			if (shape == RaiseEarthShape.COLUMN) {
				Orientation o = Orientation.get(face);
				column = new Column(o);
			} else if (shape == RaiseEarthShape.WALL) {
				Vector ortho = GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection().setY(0), 90, 1);
				direction = getDegreeRoundedVector(ortho, 0.25);
				location = source.getLocation();
				raiseWall();
				return;
			}
		} else if (state == RaiseEarthState.THROW) {
			if (bPlayer.isOnCooldown(throwString())
					|| !affectedBlocks.containsKey(sourceBlock)
					|| !throwEnabled)
				return;
			
			direction = player.getEyeLocation().getDirection();
			instance = affectedBlocks.get(sourceBlock);
			List<Column> throwableColumns = columnsByInstance.get(instance);
			Orientation o = throwableColumns.get(0).orientation;
			direction = o == Orientation.VERTICAL ? direction.setY(0).normalize() : direction.setX(0).setZ(0).normalize();
			
			player.swingMainHand();
			playEarthbendingSound(sourceBlock.getLocation());
			
			throwableColumns.forEach(c -> new RaiseEarth(player, direction, c, this));
			
			bPlayer.addCooldown(throwString(), throwCooldown);
			return;
		} else return;
		
		start();
	}
	
	public RaiseEarth(Player player, Block sourceBlock) {
		this(player, sourceBlock, BlockFace.UP);
	}
	
	public RaiseEarth(Player player, Block sourceBlock, BlockFace face) {
		this(player, sourceBlock, face, Azutoru.az.getConfig().getInt("Abilities.Earth.RaiseEarth.Height"), null);
	}
	
	public RaiseEarth(Player player, Block sourceBlock, int height) {
		this(player, sourceBlock, BlockFace.UP, height, null);
	}
	
	public RaiseEarth(Player player, Block sourceBlock, BlockFace face, int height, @Nullable RaiseEarth instance) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)
				|| !player.getEyeLocation().getBlock().isPassable()
				|| sourceBlock == null || !isEarthbendable(sourceBlock)
				|| affectedBlocks.containsKey(sourceBlock))
			return;
		
		state = RaiseEarthState.RAISE;
		shape = RaiseEarthShape.COLUMN;
		
		this.instance = instance != null ? instance : this;
		this.counter = 0;
		this.source = sourceBlock;
		this.face = face;
		Orientation o = Orientation.get(face);
		this.column = new Column(o);
		this.height = getEarthbendableBlocksLength(source, MathUtil.getFaceDirection(face).clone().multiply(-1), height);
		if (this.height < 1) return;
		
		start();
	}
	
	public RaiseEarth(Player player, Vector direction, Column column, RaiseEarth instance) {
		super(player);
		
		setFields();
		
		this.state = RaiseEarthState.THROW;
		this.column = column;
		this.instance = instance;
		this.origin = column.blocks.get(0).getLocation();
		this.location = origin.clone();
		this.direction = direction;
		
		loadAffectedBlocks();
		
		start();
	}
	
	private void setFields() {
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.RaiseEarth.Cooldown");
		sourceRange = c.getDouble("Abilities.Earth.RaiseEarth.SourceRange");
		height = c.getInt("Abilities.Earth.RaiseEarth.Height");
		width = c.getInt("Abilities.Earth.RaiseEarth.WallWidth");
		throwEnabled = c.getBoolean("Abilities.Earth.RaiseEarth.Throw.Enabled");
		throwCooldown = c.getLong("Abilities.Earth.RaiseEarth.Throw.Cooldown");
		speed = c.getInt("Abilities.Earth.RaiseEarth.Throw.Speed");
		range = c.getDouble("Abilities.Earth.RaiseEarth.Throw.Range");
		hitRadius = c.getDouble("Abilities.Earth.RaiseEarth.Throw.HitRadius");
		damage = c.getDouble("Abilities.Earth.RaiseEarth.Throw.Damage");
		knockback = c.getDouble("Abilities.Earth.RaiseEarth.Throw.Knockback");
	}

	@Override
	public void progress() {
		if (state == RaiseEarthState.RAISE) {
			if (counter < height) {
				raiseColumn();
				counter++;
			} else {
				instance.columns.add(column);
				if (columnsByInstance.get(instance) == null)
					columnsByInstance.put(instance, instance.columns);
				columnsByInstance.get(instance).add(column);
				remove();
				return;
			}
		} else if (state == RaiseEarthState.THROW) {
			throwEarth();
		}
	}
	
	private void raiseColumn() {
		moveEarth(source, MathUtil.getFaceDirection(face), height);
		source = source.getRelative(face);
		affectedBlocks.put(source, instance);
		column.blocks.add(source);
	}
	
	private void raiseWall() {
		if (blocks.isEmpty()) {
			for (int i = 0; i <= width; i++) {
				if (blocks.size() == width) break;
				double distance = i - width / 2.0;
				Block b = location.clone().add(direction.clone().multiply(distance)).getBlock();
				
				if (b != null) {
					if (isTransparent(b)) {
						b = b.getRelative(face);
						if (isEarthbendable(b))
							blocks.add(b);
					} else if (isEarthbendable(b.getRelative(face.getOppositeFace()))) {
						b = b.getRelative(face.getOppositeFace());
						blocks.add(b);
					} else if (isEarthbendable(b)) {
						blocks.add(b);
					}
				}
			}
		}
			
		for (Block b : blocks) {
			Block sourceBlock = b.getRelative(face);
			if (sourceBlock != null && isEarthbendable(sourceBlock) && !isBendableEarthTempBlock(sourceBlock)) {
				new RaiseEarth(player, sourceBlock, face, height, this);
			}
		}
	}
	
	private void throwEarth() {
		if (location.distanceSquared(origin) > range * range) {
			cleanupThrownBlocks();
			return;
		}
		
		location.add(direction.clone().multiply(speed));
		if (column.orientation == Orientation.VERTICAL && isDiagonal(direction))
			location = WorldUtil.centerBlock(location.getBlock());
		
		List<Block> newBlocks = new ArrayList<>();
		blockLoop: for (Block b : blocks) {
			Location targetLoc = location.clone().add(direction.clone().multiply(speed));
			
			if (column.orientation == Orientation.VERTICAL) {
				targetLoc.setY(b.getY());
			} else if (column.orientation == Orientation.HORIZONTAL) {
				targetLoc.setX(b.getX());
				targetLoc.setZ(b.getZ());
			}
			
			Block target = targetLoc.getBlock();
			if (!isViableTarget(target)) break;
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(targetLoc, hitRadius)) {
				if (e != player && !affectedEntities.contains(e)) {
					if (e instanceof LivingEntity)
						DamageHandler.damageEntity(e, damage, this);
					e.setVelocity(direction.clone().multiply(knockback));
					affectedEntities.add(e);
					break blockLoop;
				}
			}
			
			moveEarthBlock(b, target);
			newBlocks.add(target);
		}
		
		if (newBlocks.isEmpty()) {
			cleanupThrownBlocks();
			return;
		}
		
		blocks.clear();
		blocks.addAll(newBlocks);
		newBlocks.clear();
	}
	
	private void loadAffectedBlocks() {
		blocks.clear();
		blocks.addAll(column.blocks);
	}
	
	public void cleanupThrownBlocks() {
		for (Block b : blocks) {
			revertBlock(b);
			playCrumbleEffect(WorldUtil.centerBlock(b));
		}
		blocks.clear();
		removeAllColumns();
		remove();
	}
	
	private boolean isViableTarget(Block b) {
		return isAir(b.getType()) || WorldUtil.isIgnoredPlant(b) || isWater(b) || isTransparent(b);
	}
	
	public static boolean isDiagonal(Vector direction) {
		double x = direction.getX();
		double z = direction.getZ();
		if (x == 0 || z == 0) return false;
		double ratio = FastMath.abs(x / z);
		return ratio >= 0.8 && ratio <= 1.2;
	}
	
	public static Vector getDegreeRoundedVector(Vector vec, double degreeIncrement) {
		if (vec == null) return null;
		vec.normalize();
		double[] comps = { vec.getX(), vec.getY(), vec.getZ() };

		for (int i = 0; i < comps.length; i++) {
			double comp = comps[i];
			int sign = comp >= 0 ? 1 : -1;
			int dimDivIncr = (int) (comp / degreeIncrement);

			double lowerBound = dimDivIncr * degreeIncrement;
			double upperBound = (dimDivIncr + (1 * sign)) * degreeIncrement;
			
			comps[i] = (FastMath.abs(comp - lowerBound) < FastMath.abs(comp - upperBound)) ? lowerBound : upperBound;
		}
		return new Vector(comps[0], comps[1], comps[2]);
	}
	
	public static void playCrumbleEffect(Location location) {
		ParticleEffect.BLOCK_DUST.display(location, 5, 1, 1, 1, 0.1, Material.DIRT.createBlockData());
	}
	
	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			cleanupThrownBlocks();
		}
	}
	
	@Override
	public double getCollisionRadius() {
		return hitRadius;
	}
	
	public List<Column> getAdjacentColumns(Column column) {
		List<Column> columns = new ArrayList<>();
		Location loc = WorldUtil.centerBlock(column.blocks.get(0));
		if (loc != null) {
			for (Block b : GeneralMethods.getBlocksAroundPoint(loc, 1)) {
				if (affectedBlocks.containsKey(b)) {
					for (Column c : columnsByInstance.get(affectedBlocks.get(b))) {
						columns.add(c);
					}
				}
			}
		}
		return columns;
	}
	
	public void removeAllColumns() {
		removeAllColumns(true, true);
	}
	
	public void removeAllColumns(boolean crumble) {
		removeAllColumns(crumble, true);
	}
	
	public void removeAllColumns(boolean crumble, boolean revert) {
		if (columnsByInstance.get(instance) != null) {
			for (Column c : columnsByInstance.get(instance)) {
				for (Block b : c.blocks) {
					if (revert)
						revertBlock(b);
					if (crumble)
						playCrumbleEffect(WorldUtil.centerBlock(b));
					if (affectedBlocks.containsKey(b))
						affectedBlocks.remove(b);
				}
			}
			columnsByInstance.remove(instance);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		affectedEntities.clear();
	}
	
	public static void removeAllCleanup() {
		if (affectedBlocks != null)
			affectedBlocks.clear();
		if (columnsByInstance != null) {
			Set<RaiseEarth> removal = new HashSet<>(columnsByInstance.keySet());
			removal.forEach(re -> re.removeAllColumns(false));
			columnsByInstance.clear();
		}
	}
	
	public static void progressAll() {
		Set<RaiseEarth> removal = new HashSet<>();
		Vector direction = new Vector();
		for (RaiseEarth re : columnsByInstance.keySet()) {
			if (re.getState() != RaiseEarthState.RAISE) continue;
			World world = re.columns.get(0).blocks.get(0).getLocation().getWorld();
			boolean crumbled = false;
			
			columns:
			for (Column c : re.columns) {
				for (Block b : c.blocks) {
					Location loc = WorldUtil.centerBlock(b);
					
					for (EarthSmash es : getAbilities(EarthSmash.class)) {
						if (es.getState() != State.SHOT) continue;
						
						Location esLoc = es.getLocation();
						if (esLoc != null && esLoc.getWorld() == world
								&& loc.distanceSquared(esLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(esLoc, loc);
							es.remove();
							break columns;
						}
					}
					
					for (FireBlastCharged cfb : getAbilities(FireBlastCharged.class)) {
						Location cfbLoc = cfb.getLocation();
						if (cfbLoc != null && cfbLoc.getWorld() == world
								&& loc.distanceSquared(cfbLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(cfbLoc, loc);
							cfb.remove();
							break columns;
						}
					}
					
					if (Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireStreams.Enabled")) {
						for (FireStreams fs : getAbilities(FireStreams.class)) {
							Location fsLoc = fs.getLocation();
							if (fsLoc != null && fsLoc.getWorld() == world
									&& loc.distanceSquared(fsLoc) <= radius * radius) {
								crumbled = true;
								direction = GeneralMethods.getDirection(fsLoc, loc);
								fs.remove();
								break columns;
							}
						}
					}
					
					for (RaiseEarth reProj : getAbilities(RaiseEarth.class)) {
						if (reProj.getState() != RaiseEarthState.THROW) continue;
						
						List<Location> reProjLocs = reProj.getLocations();
						if (reProjLocs != null && !reProjLocs.isEmpty() && reProjLocs.get(0).getWorld() == world) {
							for (Location location : reProjLocs) {
								if (loc.distanceSquared(location) <= radius * radius) {
									crumbled = true;
									direction = GeneralMethods.getDirection(location, loc);
									reProj.removeAllColumns();
									break columns;
								}
							}
						}
					}
					
					if (ConfigManager.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled")) {
						for (Combustion co : getAbilities(Combustion.class)) {
							Location coLoc = co.getLocation();
							if (coLoc != null && coLoc.getWorld() == world
									&& loc.distanceSquared(coLoc) <= radius * radius) {
								crumbled = true;
								direction = GeneralMethods.getDirection(coLoc, loc);
								co.remove();
								break columns;
							}
						}
					}
					
					if (Bukkit.getPluginManager().isPluginEnabled(JedCore.plugin)) {
						if (JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled")) {
							for (com.jedk1.jedcore.ability.firebending.Combustion co : getAbilities(com.jedk1.jedcore.ability.firebending.Combustion.class)) {
								Location coLoc = co.getLocation();
								if (coLoc != null && coLoc.getWorld() == world
										&& loc.distanceSquared(coLoc) <= radius * radius) {
									crumbled = true;
									direction = GeneralMethods.getDirection(coLoc, loc);
									co.remove();
									break columns;
								}
							}
						}
					}
				}
			}
			
			if (crumbled) {
				removal.add(re);
			}
		}
		
		for (RaiseEarth re : removal) {
			for (Column c : re.columns) {
				for (Block b : c.blocks) {
					Crumble.crumble(b, direction);
				}
			}
			re.removeAllColumns();
		}
	}
	
	public static boolean isRaiseEarthBlock(Block block) {
		return affectedBlocks.containsKey(block);
	}
	
	public static Map<Block, RaiseEarth> getAffectedBlocks() {
		return affectedBlocks;
	}
	
	public static Map<RaiseEarth, List<Column>> getColumnsByInstance() {
		return columnsByInstance;
	}
	
	public RaiseEarthState getState() {
		return state;
	}
	
	public RaiseEarthShape getShape() {
		return shape;
	}
	
	public BlockFace getFace() {
		return face;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	public List<Column> getColumns() {
		return columns;
	}
	
	public RaiseEarth getParentInstance() {
		return instance;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "RaiseEarth";
	}
	
	@Override
	public String getDescription() {
		return "RaiseEarth is an extremely useful ability in any earthbender's arsenal. "
				+ "It is used to create columns and walls out of earth, which can be used for defensive purposes. "
				+ "Additionally, earthbenders can launch their RaiseEarth blocks towards an opponent, "
				+ "dealing damage and knockback.";
	}
	
	@Override
	public String getInstructions() {
		return "(Column) Left-click on an earthbendable block to pull a column from that surface. "
				+ "\n(Wall) Tap sneak on an earthbendable block to pull a wide wall from that surface. "
				+ "\n(Throw) Right-click on a RaiseEarth block to throw that column/wall in the direction you're looking.";
	}
	
	private String columnString() {
		return getName() + "Column";
	}
	
	private String wallString() {
		return getName() + "Wall";
	}
	
	private String throwString() {
		return getName() + "Throw";
	}

	@Override
	public Location getLocation() {
		return !blocks.isEmpty() ? blocks.get(0).getLocation() : player.getLocation();
	}
	
	@Override
	public List<Location> getLocations() {
		return blocks.stream().map(b -> WorldUtil.centerBlock(b)).collect(Collectors.toList());
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}

	@Override
	public String getAuthor() {
		return Azutoru.az.dev();
	}

	@Override
	public String getVersion() {
		return Azutoru.az.version();
	}
	
	@Override
	public boolean isEnabled() {
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.RaiseEarth.Enabled");
	}
	
	public class Column {
		
		private List<Block> blocks;
		private Orientation orientation;
		
		public Column(Orientation orientation) {
			blocks = new ArrayList<>();
			this.orientation = orientation;
		}
		
		public List<Block> getBlocks() {
			return blocks;
		}
		
		public Orientation getOrientation() {
			return orientation;
		}
	}
	
	public static enum Orientation {
		HORIZONTAL, VERTICAL;
		
		public static Orientation get(BlockFace face) {
			return (face == BlockFace.UP || face == BlockFace.DOWN) ? VERTICAL : HORIZONTAL;
		}
	}

}