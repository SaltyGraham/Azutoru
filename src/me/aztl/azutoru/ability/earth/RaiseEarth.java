package me.aztl.azutoru.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.ability.firebending.Combustion;
import com.jedk1.jedcore.ability.firebending.FireComet;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthSmash.State;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
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
		RAISE,
		THROW;
	}
	
	public static enum RaiseEarthShape {
		COLUMN,
		WALL;
	}
	
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

	private static Map<Block, RaiseEarth> affectedBlocks = new ConcurrentHashMap<>();
	private static Map<RaiseEarth, List<Column>> columnsByInstance = new ConcurrentHashMap<>();
	private RaiseEarth instance;
	private Location origin, location;
	private Vector direction;
	private Column column;
	private List<Column> columns = new ArrayList<>();
	private List<Block> blocks = new ArrayList<>();
	private Set<Entity> affectedEntities = new HashSet<>();
	private Block source;
	private BlockFace face;
	private RaiseEarthState state;
	private RaiseEarthShape shape;
	private int counter = 0;
	
	public RaiseEarth(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBend(this)
				|| hasAbility(player, RaiseEarth.class)
				|| !player.getEyeLocation().getBlock().isPassable())
			return;
		
		setFields();
		
		instance = this;
		Block sourceBlock = BlockSource.getEarthSourceBlock(player, sourceRange, type);
		
		state = RaiseEarthState.RAISE;
		switch (type) {
		case LEFT_CLICK:
			shape = RaiseEarthShape.COLUMN;
			break;
		case SHIFT_DOWN:
			shape = RaiseEarthShape.WALL;
			break;
		case RIGHT_CLICK:
		default:
			state = RaiseEarthState.THROW;
			sourceBlock = player.getTargetBlock(null, (int) sourceRange);
		}
		
		if (sourceBlock == null || !isEarthbendable(sourceBlock)) return;
		
		if (state == RaiseEarthState.RAISE) {
			if (bPlayer.isOnCooldown(columnString())
					|| bPlayer.isOnCooldown(wallString())
					|| affectedBlocks.containsKey(sourceBlock)) {
				return;
			}
			
			List<Block> targetBlocks = player.getLastTwoTargetBlocks(null, (int) sourceRange);
			if (targetBlocks.size() < 2) return;
			
			source = targetBlocks.get(1);
			face = source.getFace(targetBlocks.get(0));
			height = getEarthbendableBlocksLength(source, MathUtil.getFaceDirection(face).clone().multiply(-1), height);
			if (height < 1) return;
			
			if (shape == RaiseEarthShape.COLUMN) {
				Orientation o = Orientation.VERTICAL;
				if (face != BlockFace.UP && face != BlockFace.DOWN)
					o = Orientation.HORIZONTAL;
				column = new Column(o);
				
				bPlayer.addCooldown(columnString(), cooldown);
			} else if (shape == RaiseEarthShape.WALL) {
				location = source.getLocation();
				Vector eyeDir = player.getEyeLocation().getDirection().setY(0);
				direction = getDegreeRoundedVector(GeneralMethods.getOrthogonalVector(eyeDir, 90, 1), 0.25);
				
				raiseWall();
				bPlayer.addCooldown(wallString(), cooldown);
				return;
			}
		} else if (state == RaiseEarthState.THROW) {
			if (bPlayer.isOnCooldown(throwString())
					|| !affectedBlocks.containsKey(sourceBlock)
					|| !throwEnabled)
				return;
			
			origin = sourceBlock.getLocation();
			location = origin.clone();
			direction = player.getEyeLocation().getDirection();
			
			Column column = columnsByInstance.get(affectedBlocks.get(sourceBlock)).get(0);
			if (column != null) {
				Orientation o = column.getOrientation();
				direction = o == Orientation.VERTICAL ? direction.setY(0).normalize() : direction.setX(0).setZ(0).normalize();
			} else return;
			
			player.swingMainHand();
			playEarthbendingSound(location);
			
			instance = affectedBlocks.get(sourceBlock);
			columnsByInstance.get(instance).forEach(c -> new RaiseEarth(player, direction, c, this));
			
			bPlayer.addCooldown(throwString(), throwCooldown);
			return;
		} else return;
		
		start();
	}
	
	public RaiseEarth(Player player, Block sourceBlock, BlockFace face) {
		this(player, sourceBlock, face, Azutoru.az.getConfig().getInt("Abilities.Earth.RaiseEarth.Height"), null);
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
		Orientation o = (face == BlockFace.UP || face == BlockFace.DOWN) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
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
		this.origin = column.getBlocks().get(0).getLocation();
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
				counter++;
				raiseColumn();
			} else {
				instance.getColumns().add(column);
				if (columnsByInstance.get(instance) == null)
					columnsByInstance.put(instance, instance.getColumns());
				columnsByInstance.get(instance).add(column);
				remove();
				return;
			}
		} else
			throwEarth();
	}
	
	private void raiseColumn() {
		moveEarth(source, MathUtil.getFaceDirection(face), height);
		source = source.getRelative(face);
		affectedBlocks.put(source, instance);
		column.getBlocks().add(source);
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
		if (column.getOrientation() == Orientation.VERTICAL && isDiagonal(direction))
			location = location.getBlock().getLocation().add(0.5, 0.5, 0.5);
		
		List<Block> newBlocks = new ArrayList<>();
		for (Block b : blocks) {
			Location targetLoc = location.clone();
			targetLoc.add(direction.clone().multiply(speed));
			
			if (column.getOrientation() == Orientation.VERTICAL) {
				targetLoc.setY(b.getY());
			} else {
				targetLoc.setX(b.getX());
				targetLoc.setZ(b.getZ());
			}
			
			Block target = targetLoc.getBlock();
			
			if (!isViableTarget(target)) break;
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(targetLoc, hitRadius)) {
				if (affectedEntities.contains(e)) continue;
				affectedEntities.add(e);
				Vector velocity = direction.clone().multiply(knockback);
				e.setVelocity(velocity);
				if (e instanceof LivingEntity && e != player) {
					DamageHandler.damageEntity(e, damage, this);
					cleanupThrownBlocks();
					return;
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
		blocks.addAll(column.getBlocks());
	}
	
	private void cleanupThrownBlocks() {
		destroyThrownBlocks();
		blocks.clear();
		removeAllColumns();
		remove();
	}
	
	private void destroyThrownBlocks() {
		for (Block b : blocks) {
			revertBlock(b);
			playCrumbleEffect(b.getLocation().add(0.5, 0.5, 0.5));
		}
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
			
			comps[i] = FastMath.abs(comp - lowerBound) < FastMath.abs(comp - upperBound) ? lowerBound : upperBound;
		}
		
		return new Vector(comps[0], comps[1], comps[2]);
	}
	
	public static void playCrumbleEffect(Location location) {
		ParticleEffect.BLOCK_DUST.display(location, 10, 1, 1, 1, 0.1, Material.DIRT.createBlockData());
	}
	
	public static void progressAll() {
		Vector direction = new Vector();
		Iterator<RaiseEarth> it = columnsByInstance.keySet().iterator();
		while (it.hasNext()) {
			RaiseEarth re = it.next();
			Location reLoc = re.getColumns().get(0).getBlocks().get(0).getLocation();
			boolean crumbled = false;
			
			columns: for (Column c : re.getColumns()) {
				for (Block b : c.getBlocks()) {
					if (!isEarth(b)) continue;
					Location loc = b.getLocation().add(0.5, 0.5, 0.5);
					
					for (EarthSmash es : getAbilities(EarthSmash.class)) {
						if (es.getState() != State.SHOT) continue;
						
						Location esLoc = es.getLocation();
						if (esLoc != null && esLoc.getWorld() == reLoc.getWorld() 
								&& loc.distanceSquared(esLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(esLoc, loc);
							es.remove();
							break columns;
						}
					}
					
					for (FireBlastCharged cfb : getAbilities(FireBlastCharged.class)) {
						Location cfbLoc = cfb.getLocation();
						if (cfbLoc != null && cfbLoc.getWorld() == reLoc.getWorld()
								&& loc.distanceSquared(cfbLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(cfbLoc, loc);
							cfb.remove();
							break columns;
						}
					}
					
					for (FireStreams fs : getAbilities(FireStreams.class)) {
						Location fsLoc = fs.getLocation();
						if (fsLoc != null && fsLoc.getWorld() == reLoc.getWorld()
								&& loc.distanceSquared(fsLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(fsLoc, loc);
							fs.remove();
							break columns;
						}
					}
					
					for (RaiseEarth reProjectile : getAbilities(RaiseEarth.class)) {
						if (reProjectile.getState() != RaiseEarthState.THROW) continue;
						
						List<Location> reProjectileLocs = reProjectile.getLocations();
						if (reProjectileLocs != null && !reProjectileLocs.isEmpty() && reProjectileLocs.get(0).getWorld() == reLoc.getWorld()) {
							for (Location location : reProjectileLocs) {
								if (loc.distanceSquared(location) <= radius * radius) {
									crumbled = true;
									direction = GeneralMethods.getDirection(location, loc);
									reProjectile.removeAllColumns();
									break columns;
								}
							}
						}
					}
					
					if (ConfigManager.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled")) {
						for (com.projectkorra.projectkorra.firebending.combustion.Combustion co : getAbilities(com.projectkorra.projectkorra.firebending.combustion.Combustion.class)) {
							Location coLoc = co.getLocation();
							if (coLoc != null && coLoc.getWorld() == reLoc.getWorld()
									&& loc.distanceSquared(coLoc) <= radius * radius) {
								crumbled = true;
								direction = GeneralMethods.getDirection(coLoc, loc);
								co.remove();
								break columns;
							}
						}
					}
					
					if (!Bukkit.getPluginManager().isPluginEnabled(JedCore.plugin)) return;
					
					if (JedCore.plugin.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled")) {
						for (Combustion co : getAbilities(Combustion.class)) {
							Location coLoc = co.getLocation();
							if (coLoc != null && coLoc.getWorld() == reLoc.getWorld()
									&& loc.distanceSquared(coLoc) <= radius * radius) {
								crumbled = true;
								direction = GeneralMethods.getDirection(coLoc, loc);
								co.remove();
								break columns;
							}
						}
					}
					
					for (FireComet fc : getAbilities(FireComet.class)) {
						Location fcLoc = fc.getLocation();
						if (fcLoc != null && fcLoc.getWorld() == reLoc.getWorld()
								&& loc.distanceSquared(fcLoc) <= radius * radius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(fcLoc, loc);
							fc.remove();
							break columns;
						}
					}
					
				}
			}
			if (crumbled) {
				for (Column c : re.getColumns()) {
					for (Block b : c.getBlocks()) {
						Crumble.crumble(b, direction);
					}
				}
				re.removeAllColumns();
			}
		}
	}
	
	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst())
			cleanupThrownBlocks();
	}
	
	@Override
	public double getCollisionRadius() {
		return hitRadius;
	}
	
	public Set<Column> getAdjacentColumns(Column column) {
		Set<Column> columns = new HashSet<>();
		Location loc = column.getBlocks().get(0).getLocation().add(0.5, 0.5, 0.5);
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
				for (Block b : c.getBlocks()) {
					if (revert)
						revertBlock(b);
					if (crumble)
						playCrumbleEffect(b.getLocation().add(0.5, 0.5, 0.5));
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
			columnsByInstance.keySet().forEach(re -> re.removeAllColumns(false));
			CoreAbility.getAbilities(RaiseEarth.class).forEach(abil -> ((RaiseEarth) abil).removeAllColumns(false));
			columnsByInstance.clear();
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
		return blocks.stream().map(b -> b.getLocation().add(0.5, 0.5, 0.5)).collect(Collectors.toList());
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
	}

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
		return true;
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
	}

}
