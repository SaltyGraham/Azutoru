package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

import me.aztl.azutoru.Azutoru;

public class WaterPinwheel extends WaterAbility implements AddonAbility, ComboAbility {

	public static enum AnimateState {
		RISE, TOWARD_PLAYER, CIRCLE;
	}
	
	private long cooldown, duration;
	private double sourceRange, range, damage, deflectDamage, hitRadius, ringRadius, speed, knockback;
	
	private boolean clicked;
	private Block sourceBlock;
	private Location location, eyeLoc;
	private Vector direction;
	private AnimateState animation;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	
	public WaterPinwheel(Player player) {
		super(player);
		
		SurgeWall surgeWall = getAbility(player, SurgeWall.class);
		if (surgeWall != null) {
			surgeWall.remove();
		}
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.WaterPinwheel.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.WaterPinwheel.Duration");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.SourceRange");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.Range");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.Damage");
		deflectDamage = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.DeflectDamage");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.HitRadius");
		ringRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.RingRadius");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.Speed");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterPinwheel.Knockback");
		animation = AnimateState.RISE;
		affectedBlocks = new ConcurrentHashMap<>();
		clicked = false;
		
		sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, true, true, true, true);
		if (sourceBlock != null && !GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation())) {
			location = sourceBlock.getLocation().clone();
			start();
		}
	}
	
	@Override
	public void progress() {
		player.sendMessage("Progressing");
		if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
			new PlantRegrowth(player, sourceBlock, 2);
			sourceBlock.setType(Material.AIR);
		}
		
		if (TempBlock.isTempBlock(sourceBlock)) {
			TempBlock tb = TempBlock.get(sourceBlock);
			if (Torrent.getFrozenBlocks().containsKey(tb)) {
				Torrent.massThaw(tb);
			} else if (!isBendableWaterTempBlock(tb)) {
				remove();
				return;
			}
		}
		
		if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("surge")) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (location.distanceSquared(player.getLocation()) > range * range) {
			remove();
			return;
		}
		
		if (clicked) {
			animation = null;
		}
		
		if (direction == null) {
			direction = player.getEyeLocation().getDirection();
		}
		
		eyeLoc = player.getTargetBlock((HashSet<Material>) null, 3).getLocation();
		eyeLoc.setY(player.getLocation().getY());
		
		if (animation == AnimateState.RISE && location != null) {
			revertBlocks();
			location.add(0, 1, 0);
			Block block = location.getBlock();
			
			if (!(isWaterbendable(block) || ElementalAbility.isAir(block.getType())
					|| GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation()))) {
				remove();
				return;
			}
			
			createBlock(block, Material.WATER);
			if (location.distanceSquared(sourceBlock.getLocation()) > 4) {
				animation = AnimateState.TOWARD_PLAYER;
			}
		} else if (animation == AnimateState.TOWARD_PLAYER) {
			revertBlocks();
			Vector vec = GeneralMethods.getDirection(location, eyeLoc);
			location.add(vec.normalize());
			Block block = location.getBlock();
			
			if (!(isWaterbendable(block) || ElementalAbility.isAir(block.getType())
					|| GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation()))) {
				remove();
				return;
			}
			
			createBlock(block, Material.WATER);
			if (location.distanceSquared(eyeLoc) < 2) {
				animation = AnimateState.CIRCLE;
				direction = player.getLocation().getDirection();
				revertBlocks();
			}
		} else if (animation == AnimateState.CIRCLE) {
			displayWheel();
		} else {
			revertBlocks();
			Location destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
			direction = GeneralMethods.getDirection(location, destination).normalize();
			location.add(direction.clone().multiply(speed));
			if (!isTransparent(location.getBlock())) {
				location.subtract(direction.clone().multiply(speed));
			}
			createBlockDelayRevert(location.getBlock(), Material.WATER, 100);
			if (new Random().nextInt(4) == 0) {
				playWaterbendingSound(location);
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					DamageHandler.damageEntity(entity, damage, this);
					Vector travelVec = GeneralMethods.getDirection(location, entity.getLocation());
					entity.setVelocity(travelVec.normalize().multiply(knockback));
				}
			}
		}
	}
	
	public void displayWheel() {
		List<Block> wheel = new ArrayList<Block>();
		revertBlocks();
		location = player.getLocation();
		int rotationsCount = 0;
		int step = 1;
		
		while (rotationsCount < 3) {
			boolean completed = false;
			double angle = 3 + step * 0.15;
			double xRotation = 2 * Math.PI / 3;
			Vector vec = new Vector(Math.cos(angle), Math.sin(angle), 0).multiply(ringRadius);
			vec = vec.setY(vec.getY() * Math.cos(xRotation) - vec.getZ() * Math.sin(xRotation))
					.setZ(vec.getY() * Math.sin(xRotation) + vec.getZ() * Math.cos(xRotation));
			if (!wheel.contains(location.add(vec).getBlock())) {
				completed = true;
				Block block = location.add(vec).getBlock();
				if (isTransparent(block)) {
					wheel.add(block);
				} else {
					for (int i = 0; i < 4; i++) {
						if (isTransparent(block.getRelative(BlockFace.UP, i))) {
							wheel.add(block.getRelative(BlockFace.UP, i));
							break;
						}
					}
				}
			}
			if (completed) {
				rotationsCount++;
			}
			if (animation != AnimateState.CIRCLE) {
				break;
			}
			step++;
		}
		for (Block block : wheel) {
			createBlock(block, Material.WATER);
			if (new Random(10).nextInt() == 0) {
				playWaterbendingSound(block.getLocation());
			}
		}
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
			if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
				entity.setVelocity(location.getDirection().multiply(1.25));
				DamageHandler.damageEntity(entity, deflectDamage, this);
			}
		}
		if (animation != AnimateState.CIRCLE) {
			if (!wheel.isEmpty()) {
				Collections.reverse(wheel);
			}
		}
	}
	
	/*public void displayWheel2() {
		List<Block> wheel = new ArrayList<Block>();
		revertBlocks();
		for (double angle = 0; angle < 360; angle+= 10) {
			location = player.getEyeLocation();
			direction = location.getDirection();
			location.setX(location.getX() + ringRadius * direction.getX() * Math.cos(angle));
			location.setY(location.getY() + ringRadius * Math.sin(angle));
			location.setZ(location.getZ() + ringRadius * direction.getZ() * Math.cos(angle));
			if (isTransparent(location.getBlock())) {
				wheel.add(location.getBlock());
			} else {
				for (int i = 0; i < 4; i++) {
					if (isTransparent(location.getBlock().getRelative(BlockFace.UP, i))) {
						wheel.add(location.getBlock().getRelative(BlockFace.UP, i));
						break;
					}
				}
			}
			for (Block block : wheel) {
				createBlock(block, Material.WATER);
				if (new Random().nextInt(10) == 0) {
					playWaterbendingSound(block.getLocation());
				}
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					entity.setVelocity(location.getDirection().multiply(1.25));
					DamageHandler.damageEntity(entity, deflectDamage, this);
				}
			}
		}
	}
	*/
	public void onClick() {
		if (animation == AnimateState.CIRCLE) {
			clicked = true;
		}
		SurgeWave surgeWave = getAbility(player, SurgeWave.class);
		if (surgeWave != null) {
			surgeWave.remove();
			return;
		}
	}
	
	public void createBlock(Block block, Material material) {
		affectedBlocks.put(block, new TempBlock(block, material));
	}
	
	public void createBlockDelayRevert(Block block, Material material, long revertTime) {
		TempBlock tb = new TempBlock(block, material);
		tb.setRevertTime(revertTime);
		affectedBlocks.put(block, tb);
	}
	
	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertBlocks();
		affectedBlocks.clear();
		bPlayer.addCooldown(this);
		return;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "WaterPinwheel";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a waterbender to encompass themselves in a wheel of water that blocks incoming attacks and can be used offensively.";
	}
	
	@Override
	public String getInstructions() {
		return "Torrent (Hold sneak) > Surge (Release sneak) > Surge (Hold sneak) > Surge (Click multiple times)";
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
		return new WaterPinwheel(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Surge", ClickType.SHIFT_UP));
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
		return false;
	}

}
