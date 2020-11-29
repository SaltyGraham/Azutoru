package me.aztl.azutoru.ability.water.ice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class IceRidge extends IceAbility implements AddonAbility {

	private long cooldown, duration, revertTime;
	private double sourceRange, range, damage, knockback, knockup, hitRadius;
	private int minHeight, maxHeight;
	
	private boolean progressing;
	private int counter = 0;
	private BlockFace face;
	private Block sourceBlock;
	private World world;
	private Location location, origin;
	private Vector direction;
	private List<Location> locations;
	private Set<LivingEntity> affectedEntities;
	
	public IceRidge(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (hasAbility(player, IceRidge.class)) {
			IceRidge ir = getAbility(player, IceRidge.class);
			if (ir.progressing) {
				return;
			}
			ir.remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.IceRidge.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.IceRidge.Duration");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.SourceRange");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.Range");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.Damage");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.Knockback");
		knockup = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.Knockup");
		minHeight = Azutoru.az.getConfig().getInt("Abilities.Water.IceRidge.MinHeight");
		maxHeight = Azutoru.az.getConfig().getInt("Abilities.Water.IceRidge.MaxHeight");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.IceRidge.HitRadius");
		revertTime = Azutoru.az.getConfig().getLong("Abilities.Water.IceRidge.RevertTime");
		
		applyModifiers();
		
		sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, true, false, true, false);
		if (sourceBlock == null) {
			return;
		}
		
		List<Block> targets = player.getLastTwoTargetBlocks(null, (int) sourceRange);
		face = targets.get(1).getFace(targets.get(0));
		
		origin = sourceBlock.getLocation().add(AzutoruMethods.getFaceDirection(face));
		location = origin.clone();
		direction = GeneralMethods.getDirection(origin, GeneralMethods.getTargetedLocation(player, range)).normalize();
		locations = new ArrayList<>();
		affectedEntities = new HashSet<>();
		world = player.getWorld();
		
		start();
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			duration = (long) getNightFactor(duration);
			sourceRange = getNightFactor(sourceRange);
			range = getNightFactor(range);
			damage = getNightFactor(damage);
			knockback = getNightFactor(knockback);
			knockup = getNightFactor(knockup);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			sourceRange *= 1.5;
			range *= 1.5;
			damage *= 1.25;
			knockback *= 1.25;
			knockup *= 1.25;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			removeWithCooldown();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			if (progressing) {
				bPlayer.addCooldown(this);
			}
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			removeWithCooldown();
			return;
		}
		
		if (location.distanceSquared(origin) > range * range) {
			removeWithCooldown();
			return;
		}
		
		if (!progressing) {
			if (player.getLocation().distanceSquared(origin) >= sourceRange * sourceRange) {
				remove();
				return;
			}
			
			playFocusWaterEffect(sourceBlock);
			direction = player.getEyeLocation().getDirection();
			return;
		}
		
		if (player.isSneaking()) {
			direction.add(player.getEyeLocation().getDirection().multiply(0.5));
		}
		
		if (face == BlockFace.UP || face == BlockFace.DOWN) {
			direction.setY(0).normalize();
		} else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
			direction.setZ(0).normalize();
		} else if (face == BlockFace.EAST || face == BlockFace.WEST) {
			direction.setX(0).normalize();
		}
		
		location.add(direction);
		
		if (!isTransparent(location.getBlock())) {
			location.add(AzutoruMethods.getFaceDirection(face));
		}
		
		Block topBlock = AzutoruMethods.getTopBlock(location, face.getOppositeFace(), 3);
		if (!isIce(topBlock) && !isWater(topBlock) && !isSnow(topBlock)) {
			removeWithCooldown();
			return;
		}
		
		int currentHeight = AzutoruMethods.getRandomNumberInRange(minHeight, maxHeight);
		addBlocks(topBlock, face, currentHeight);
		
		updateLocations(currentHeight);
		
		if (counter % 6 == 0) {
			playIcebendingSound(location);
		}
		counter++;
		
		for (Location loc : locations) {
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, hitRadius)) {
				if (e.getUniqueId() != player.getUniqueId()) {
					Vector travelVec = direction.clone().multiply(knockback).setY(direction.getY() * knockup);
					e.setVelocity(travelVec);
					if (e instanceof LivingEntity && !affectedEntities.contains((LivingEntity) e)) {
						DamageHandler.damageEntity(e, damage, this);
						affectedEntities.add((LivingEntity) e);
					}
				}
			}
		}
	}
	
	private void addBlocks(Block topBlock, BlockFace face, int height) {
		for (int i = 1; i <= height; i++) {
			Block b = topBlock.getRelative(face, i);
			if (isAir(b.getType())) {
				TempBlock tb = new TempBlock(b, Material.ICE);
				tb.setRevertTime(revertTime);
				addWaterbendableTempBlock(tb);
				tb.setRevertTask(() -> removeWaterbendableTempBlock(tb));
			}
		}
	}
	
	private void updateLocations(int currentHeight) {
		locations.clear();
		for (int i = 1; i <= currentHeight; i++) {
			Location loc = location.clone().add(AzutoruMethods.getFaceDirection(face).multiply(i));
			locations.add(loc);
		}
	}
	
	public void onClick() {
		progressing = true;
	}
	
	@Override
	public void remove() {
		super.remove();
		affectedEntities.clear();
	}
	
	public void removeWithCooldown() {
		remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "IceRidge";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows an icebender to create a moving wall of ice that can deal damage and knockback.";
	}
	
	@Override
	public String getInstructions() {
		return "Tap sneak on an ice, water, or snow block to select a source, then left-click to create an extending wall of ice. Hold sneak to change the direction of the wall.";
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	@Override
	public List<Location> getLocations() {
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.IceRidge.Enabled");
	}

}
