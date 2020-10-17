package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class WaterSphere extends WaterAbility implements AddonAbility, ComboAbility {

	private double sourceRange, speed, range, radius, damage;
	private boolean iceSource, plantSource, snowSource, bottleSource;
	private long cooldown, duration;
	
	private Location location;
	private Vector direction;
	private boolean clicked;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	private World world;
	private ArrayList<Entity> damagedEntities;
	
	public WaterSphere(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (getAbility(player, SurgeWall.class) != null) {
			getAbility(player, SurgeWall.class).remove();
		}
		
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSphere.SourceRange");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSphere.Speed");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSphere.Range");
		radius = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSphere.Radius");
		iceSource = Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSphere.AllowIceSource");
		plantSource = Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSphere.AllowPlantSource");
		snowSource = Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSphere.AllowSnowSource");
		bottleSource = Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSphere.AllowBottleSource");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSphere.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSphere.Duration");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSphere.Damage");
		
		applyModifiers();
		
		clicked = false;
		affectedBlocks = new ConcurrentHashMap<>();
		world = player.getWorld();
		damagedEntities = new ArrayList<>();
		
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
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("surge")) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock())) {
			location.subtract(direction.normalize().multiply(speed));
		}
		
		if (getAbility(player, SurgeWall.class) != null) {
			getAbility(player, SurgeWall.class).remove();
		} else if (getAbility(player, SurgeWave.class) != null) {
			getAbility(player, SurgeWave.class).remove();
		}
		
		if (clicked) {
			if (hasAbility(player, SurgeWave.class)) {
				getAbility(player, SurgeWave.class).remove();
			} else if (hasAbility(player, SurgeWall.class)) {
				getAbility(player, SurgeWall.class).remove();
			}
			
			if (player.getLocation().distanceSquared(location) > range * range * 1.1) {
				remove();
				return;
			}
			
			for (Block b : GeneralMethods.getBlocksAroundPoint(location, radius)) {
				if (ElementalAbility.isAir(b.getType()) && !GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
					new TempBlock(b, Material.WATER);
				}
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
				if (e.getUniqueId() != player.getUniqueId()) {
					e.setVelocity(new Vector(0, 0, 0));
				}
			}
		} else {
			AzutoruMethods.revertBlocks(affectedBlocks);
			
			if (player.isSneaking()) {
				direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, radius + 2));
			} else {
				direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials()));
			}
			
			location.add(direction.normalize().multiply(speed));
			
			for (Block b : GeneralMethods.getBlocksAroundPoint(location, radius)) {
				if (ElementalAbility.isAir(b.getType()) && !GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
					createBlock(b, Material.WATER);
				}
				if (GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
					remove();
					return;
				}
				if (ElementalAbility.isWater(b) && !TempBlock.isTempBlock(b)) {
					AzutoruMethods.displayWaterBubble(b.getLocation());
				}
				if (ElementalAbility.isLava(b)) {
					b.setType(Material.OBSIDIAN);
				}
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
				if (e.getUniqueId() != player.getUniqueId()) {
					Vector velocity = GeneralMethods.getDirection(e.getLocation().add(0, 1, 0), location).normalize();
					
					if (!(e instanceof Player)) {
						velocity.multiply(1.5);
					}
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
		if (clicked) {
			clicked = false;
		} else {
			clicked = true;
		}
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
		AzutoruMethods.revertBlocks(affectedBlocks);
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
		return true;
	}

}
