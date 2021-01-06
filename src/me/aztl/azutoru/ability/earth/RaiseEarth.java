package me.aztl.azutoru.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
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
	private static double collisionRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.CollisionRadius");

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
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (hasAbility(player, RaiseEarth.class)) {
			return;
		}
		
		if (!player.getEyeLocation().getBlock().isPassable()) {
			return;
		}
		
		setFields();
		
		instance = this;
		Block sourceBlock = BlockSource.getEarthSourceBlock(player, sourceRange, type);
		
		if (type == ClickType.LEFT_CLICK) {
			state = RaiseEarthState.RAISE;
			shape = RaiseEarthShape.COLUMN;
		} else if (type == ClickType.SHIFT_DOWN) {
			state = RaiseEarthState.RAISE;
			shape = RaiseEarthShape.WALL;
		} else if (type == ClickType.RIGHT_CLICK) {
			state = RaiseEarthState.THROW;
			sourceBlock = player.getTargetBlock(null, (int) sourceRange);
		}
		
		if (sourceBlock == null || !isEarthbendable(sourceBlock)) {
			return;
		}
		
		if (state == RaiseEarthState.RAISE) {
			if (bPlayer.isOnCooldown(columnString())
					|| bPlayer.isOnCooldown(wallString())) {
				return;
			}
			if (!affectedBlocks.containsKey(sourceBlock)) {
				List<Block> targetBlocks = player.getLastTwoTargetBlocks(null, (int) sourceRange);
				if (targetBlocks.size() > 1) {
					source = targetBlocks.get(1);
					face = source.getFace(targetBlocks.get(0));
					height = getEarthbendableBlocksLength(source, MathUtil.getFaceDirection(face).clone().multiply(-1), height);
					if (height < 1) {
						return;
					}
					
					if (shape == RaiseEarthShape.COLUMN) {
						Orientation o = Orientation.VERTICAL;
						if (face != BlockFace.UP && face != BlockFace.DOWN) {
							o = Orientation.HORIZONTAL;
						}
						column = new Column(o);
						
						bPlayer.addCooldown(columnString(), cooldown);
					} else if (shape == RaiseEarthShape.WALL) {
						Vector eyeDir = player.getEyeLocation().getDirection();
						eyeDir.setY(0);
						Vector ortho = GeneralMethods.getOrthogonalVector(eyeDir, 90, 1);
						direction = getDegreeRoundedVector(ortho, 0.25);
						
						location = source.getLocation();
						
						bPlayer.addCooldown(wallString(), cooldown);
					}
				}
			} else return;
		} else if (state == RaiseEarthState.THROW) {
			if (bPlayer.isOnCooldown(throwString())) {
				return;
			}
			if (affectedBlocks.containsKey(sourceBlock) && throwEnabled) {
				origin = sourceBlock.getLocation();
				location = origin.clone();
				
				Location eye = player.getEyeLocation();
				direction = eye.getDirection();
				
				Column column = columnsByInstance.get(affectedBlocks.get(sourceBlock)).get(0);
				if (column != null) {
					Orientation o = column.getOrientation();
					if (o == Orientation.VERTICAL) {
						direction.setY(0).normalize();
					} else if (o == Orientation.HORIZONTAL) {
						direction.setX(0).setZ(0).normalize();
					}
				}
				
				player.swingMainHand();
				playEarthbendingSound(location);
				
				instance = affectedBlocks.get(sourceBlock);
				List<Column> throwableColumns = columnsByInstance.get(instance);
				for (Column c : throwableColumns) {
					new RaiseEarth(player, direction, c, this);
				}
				
				bPlayer.addCooldown(throwString(), throwCooldown);
				return;
			} else return;
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
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (!player.getEyeLocation().getBlock().isPassable()) {
			return;
		}
		
		if (sourceBlock == null || !isEarthbendable(sourceBlock)) {
			return;
		}
		
		if (affectedBlocks.containsKey(sourceBlock)) {
			return;
		}
		
		state = RaiseEarthState.RAISE;
		shape = RaiseEarthShape.COLUMN;
		
		if (instance == null) {
			this.instance = this;
		} else {
			this.instance = instance;
		}
		this.counter = 0;
		this.source = sourceBlock;
		this.face = face;
		Orientation o = Orientation.VERTICAL;
		if (face != BlockFace.UP && face != BlockFace.DOWN) {
			o = Orientation.HORIZONTAL;
		}
		this.column = new Column(o);
		this.height = getEarthbendableBlocksLength(source, MathUtil.getFaceDirection(face).clone().multiply(-1), height);
		if (this.height < 1) {
			return;
		}
		
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
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.RaiseEarth.Cooldown");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.SourceRange");
		height = Azutoru.az.getConfig().getInt("Abilities.Earth.RaiseEarth.Height");
		width = Azutoru.az.getConfig().getInt("Abilities.Earth.RaiseEarth.WallWidth");
		throwEnabled = Azutoru.az.getConfig().getBoolean("Abilities.Earth.RaiseEarth.Throw.Enabled");
		throwCooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.RaiseEarth.Throw.Cooldown");
		speed = Azutoru.az.getConfig().getInt("Abilities.Earth.RaiseEarth.Throw.Speed");
		range = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.Throw.Range");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.Throw.HitRadius");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.Throw.Damage");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Earth.RaiseEarth.Throw.Knockback");
	}

	@Override
	public void progress() {
		if (state == RaiseEarthState.RAISE) {
			if (counter < height) {
				if (shape == RaiseEarthShape.COLUMN) {
					raiseColumn();
				} else if (shape == RaiseEarthShape.WALL) {
					raiseWall();
				}
				
				counter++;
			} else {
				instance.getColumns().add(column);
				if (columnsByInstance.get(instance) == null) {
					columnsByInstance.put(instance, instance.getColumns());
				}
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
		column.getBlocks().add(source);
	}
	
	private void raiseWall() {
		if (blocks.isEmpty()) {
			for (int i = 0; i <= width; i++) {
				if (blocks.size() == width) {
					break;
				}
				double distance = i - width / 2.0;
				Block b = location.clone().add(direction.clone().multiply(distance)).getBlock();
				
				if (b != null) {
					if (isTransparent(b)) {
						b = b.getRelative(face);
						if (isEarthbendable(b)) {
							blocks.add(b);
						}
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
		
		remove();
	}
	
	private void throwEarth() {
		if (location.distanceSquared(origin) > range * range) {
			cleanupThrownBlocks();
			return;
		}
		
		location.add(direction.clone().multiply(speed));
		if (column.getOrientation() == Orientation.VERTICAL && isDiagonal(direction)) {
			location = location.getBlock().getLocation().add(0.5, 0.5, 0.5);
		}
		
		ArrayList<Block> newBlocks = new ArrayList<>();
		for (Block b : blocks) {
			Location targetLoc = location.clone();
			targetLoc.add(direction.clone().multiply(speed));
			
			if (column.getOrientation() == Orientation.VERTICAL) {
				targetLoc.setY(b.getY());
			} else if (column.getOrientation() == Orientation.HORIZONTAL) {
				targetLoc.setX(b.getX());
				targetLoc.setZ(b.getZ());
			}
			
			Block target = targetLoc.getBlock();
			
			if (!isViableTarget(target)) {
				break;
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(targetLoc, hitRadius)) {
				if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId() && !affectedEntities.contains(e)) {
					Vector velocity = direction.clone().multiply(knockback);
					e.setVelocity(velocity);
					DamageHandler.damageEntity(e, damage, this);
					affectedEntities.add(e);
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
		if (x == 0 || z == 0) {
			return false;
		}
		double ratio = Math.abs(x / z);
		if (ratio >= 0.8 && ratio <= 1.2) {
			return true;
		}
		return false;
	}
	
	public static Vector getDegreeRoundedVector(Vector vec, double degreeIncrement) {
		if (vec == null) {
			return null;
		}
		vec.normalize();
		double[] dims = { vec.getX(), vec.getY(), vec.getZ() };

		for (int i = 0; i < dims.length; i++) {
			double dim = dims[i];
			int sign = dim >= 0 ? 1 : -1;
			int dimDivIncr = (int) (dim / degreeIncrement);

			double lowerBound = dimDivIncr * degreeIncrement;
			double upperBound = (dimDivIncr + (1 * sign)) * degreeIncrement;

			if (Math.abs(dim - lowerBound) < Math.abs(dim - upperBound)) {
				dims[i] = lowerBound;
			} else {
				dims[i] = upperBound;
			}
		}
		return new Vector(dims[0], dims[1], dims[2]);
	}
	
	public static void playCrumbleEffect(Location location) {
		ParticleEffect.BLOCK_DUST.display(location, 10, 1, 1, 1, 0.1, Material.DIRT.createBlockData());
	}
	
	public static void progressAll() {
		Set<RaiseEarth> removal = new HashSet<>();
		Vector direction = new Vector(0, 0, 0);
		for (RaiseEarth re : columnsByInstance.keySet()) {
			Location reLoc = re.getColumns().get(0).getBlocks().get(0).getLocation();
			boolean crumbled = false;
			
			columns:
			for (Column c : re.getColumns()) {
				for (Block b : c.getBlocks()) {
					if (!isEarth(b)) continue;
					Location loc = b.getLocation().add(0.5, 0.5, 0.5);
					
					for (EarthSmash es : getAbilities(EarthSmash.class)) {
						if (es.getState() != State.SHOT) {
							continue;
						}
						
						Location esLoc = es.getLocation();
						if (esLoc != null && esLoc.getWorld().equals(reLoc.getWorld())
								&& loc.distance(esLoc) <= collisionRadius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(esLoc, loc);
							es.remove();
							break columns;
						}
					}
					
					for (FireBlastCharged cfb : getAbilities(FireBlastCharged.class)) {
						Location cfbLoc = cfb.getLocation();
						if (cfbLoc != null && cfbLoc.getWorld().equals(reLoc.getWorld())
								&& loc.distance(cfbLoc) <= collisionRadius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(cfbLoc, loc);
							cfb.remove();
							break columns;
						}
					}
					
					for (FireStreams fs : getAbilities(FireStreams.class)) {
						Location fsLoc = fs.getLocation();
						if (fsLoc != null && fsLoc.getWorld().equals(reLoc.getWorld())
								&& loc.distance(fsLoc) <= collisionRadius) {
							crumbled = true;
							direction = GeneralMethods.getDirection(fsLoc, loc);
							fs.remove();
							break columns;
						}
					}
					
					for (RaiseEarth reProjectile : getAbilities(RaiseEarth.class)) {
						if (reProjectile.getState() != RaiseEarthState.THROW) {
							continue;
						}
						
						List<Location> reProjectileLocs = reProjectile.getLocations();
						if (reProjectileLocs != null && !reProjectileLocs.isEmpty() && reProjectileLocs.get(0).getWorld().equals(reLoc.getWorld())) {
							for (Location location : reProjectileLocs) {
								if (loc.distance(location) <= collisionRadius) {
									crumbled = true;
									direction = GeneralMethods.getDirection(location, loc);
									reProjectile.removeAllColumns();
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
			for (Column c : re.getColumns()) {
				for (Block b : c.getBlocks()) {
					Crumble.crumble(b, direction);
				}
			}
			re.removeAllColumns();
		}
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
		if (affectedBlocks != null) {
			affectedBlocks.clear();
		}
		if (columnsByInstance != null) {
			for (RaiseEarth re : columnsByInstance.keySet()) {
				re.removeAllColumns(false);
			}
			for (CoreAbility abil : CoreAbility.getAbilities(RaiseEarth.class)) {
				RaiseEarth re = (RaiseEarth) abil;
				re.removeAllColumns(false);
			}
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
		List<Location> locations = new ArrayList<>();
		for (Block b : blocks) {
			locations.add(b.getLocation().add(0.5, 0.5, 0.5));
		}
		return locations;
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
