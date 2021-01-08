package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DamagePolicy;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;
import me.aztl.azutoru.util.WorldUtil;

public class WaterSphere extends WaterAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private boolean iceSource, plantSource, snowSource, bottleSource;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;

	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	private ArrayList<Entity> damagedEntities;
	private Location location;
	private Vector direction;
	private RemovalPolicy policy;
	private boolean clicked;
	
	public WaterSphere(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		if (getAbility(player, SurgeWall.class) != null)
			getAbility(player, SurgeWall.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		sourceRange = c.getDouble("Abilities.Water.WaterSphere.SourceRange");
		speed = c.getDouble("Abilities.Water.WaterSphere.Speed");
		range = c.getDouble("Abilities.Water.WaterSphere.Range");
		radius = c.getDouble("Abilities.Water.WaterSphere.Radius");
		iceSource = c.getBoolean("Abilities.Water.WaterSphere.AllowIceSource");
		plantSource = c.getBoolean("Abilities.Water.WaterSphere.AllowPlantSource");
		snowSource = c.getBoolean("Abilities.Water.WaterSphere.AllowSnowSource");
		bottleSource = c.getBoolean("Abilities.Water.WaterSphere.AllowBottleSource");
		cooldown = c.getLong("Abilities.Water.WaterSphere.Cooldown");
		duration = c.getLong("Abilities.Water.WaterSphere.Duration");
		damage = c.getDouble("Abilities.Water.WaterSphere.Damage");
		// TODO: Add damage threshold
		
		applyModifiers();
		
		clicked = false;
		affectedBlocks = new ConcurrentHashMap<>();
		damagedEntities = new ArrayList<>();
		
		policy = Policies.builder()
					.add(new DamagePolicy(3 /* TODO: future damage threshold variable */, () -> player.getHealth()))
					.add(new DifferentWorldPolicy(() -> player.getWorld()))
					.add(new ExpirationPolicy(duration))
					.add(new ProtectedRegionPolicy(this, () -> location))
					.add(new SwappedSlotsPolicy("Surge")).build();
		
		Block sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, iceSource, plantSource, snowSource, bottleSource);
		if (sourceBlock != null) {
			location = sourceBlock.getLocation().add(0, 1.5, 0);
			start();
		}
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			range = getNightFactor(range);
			radius++;
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			duration = (long) getNightFactor(duration);
		}
		
		if (bPlayer.isAvatarState()) {
			speed *= 1.25;
			range *= 1.25;
			cooldown /= 2;
			duration *= 2;
			damage *= 1.5;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock()))
			location.subtract(direction.normalize().multiply(speed));
		
		if (hasAbility(player, SurgeWall.class))
			getAbility(player, SurgeWall.class).remove();
		if (hasAbility(player, SurgeWave.class))
			getAbility(player, SurgeWave.class).remove();
		
		if (clicked) {
			if (player.getLocation().distanceSquared(location) > range * range * 1.1) {
				remove();
				return;
			}
			
			for (Block b : GeneralMethods.getBlocksAroundPoint(location, radius)) {
				if (isAir(b.getType()) && !GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
					affectedBlocks.put(b, new TempBlock(b, Material.WATER));
				}
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
				if (e != player) {
					e.setVelocity(new Vector());
				}
			}
		} else {
			WorldUtil.revertBlocks(affectedBlocks);
			
			if (player.isSneaking())
				direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, radius + 2));
			else
				direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials()));
			
			location.add(direction.normalize().multiply(speed));
			
			for (Block b : GeneralMethods.getBlocksAroundPoint(location, radius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
					remove();
					return;
				}
				if (isAir(b.getType()))
					createBlock(b, Material.WATER);
				else if (isWater(b) && !TempBlock.isTempBlock(b))
					WorldUtil.displayWaterBubble(b.getLocation());
				else if (isLava(b))
					b.setType(Material.OBSIDIAN);
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
				if (e != player) {
					Vector velocity = GeneralMethods.getDirection(e.getLocation().add(0, 1, 0), location).normalize().multiply(1.25);
					e.setVelocity(velocity);
					e.setFallDistance(0);
					if (damage > 0 && e instanceof LivingEntity && !damagedEntities.contains(e)) {
						DamageHandler.damageEntity(e, damage, this);
						damagedEntities.add(e);
					}
				}
			}
		}
	}
	
	public void onClick() {
		clicked = !clicked;
	}
	
	public void createBlock(Block block, Material material) {
		TempBlock tb = new TempBlock(block, material);
		tb.setRevertTime(50);
		affectedBlocks.put(block, tb);
	}

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		WorldUtil.revertBlocks(affectedBlocks);
		affectedBlocks.clear();
		damagedEntities.clear();
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location != null ? location : null;
	}

	@Override
	public String getName() {
		return "WaterSphere";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a waterbender to control a sphere of water that pulls entities along with it.";
	}
	
	@Override
	public String getInstructions() {
		return "You must be looking at a water source. WaterManipulation (Left-click) > Surge (Left-click) > Surge (Left-click) > Surge (Hold sneak)";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new WaterSphere(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterManipulation", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("Surge", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("Surge", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("Surge", ClickType.SHIFT_DOWN));
		return combo;
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
	public void load() {
	}

	@Override
	public void stop() {
	}
	
	@Override
	public boolean isEnabled() {
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSphere.Enabled");
	}

}
