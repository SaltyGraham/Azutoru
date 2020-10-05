package me.aztl.azutoru.ability.earth.sand;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.ability.earth.sand.combo.DustDevilRush;

public class DustDevil extends SandAbility implements AddonAbility {

	private long cooldown, duration;
	private double height, damage;
	private int blindnessTime;
	
	private Block topBlock;
	private Material topBlockType;
	private World world;
	private double currentHeight;
	
	public DustDevil(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevil.Cooldown");
		height = Azutoru.az.getConfig().getDouble("Abilities.Earth.DustDevil.Height");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevil.Duration");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Earth.DustDevil.Damage");
		blindnessTime = Azutoru.az.getConfig().getInt("Abilities.Earth.DustDevil.BlindnessTime");
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) height);
		world = player.getWorld();
		
		if (!AzutoruMethods.isDust(topBlock)) {
			return;
		}
		
		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(topBlock.getLocation(), heightRemoveThreshold)) {
			return;
		}
		
		flightHandler.createInstance(player, getName());
		allowFlight();
		start();
	}
	
	@Override
	public void progress() {
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) height);
		
		if (AzutoruMethods.isIgnoredPlant(topBlock)) {
			topBlockType = topBlock.getRelative(BlockFace.DOWN, 2).getType();
		} else {
			topBlockType = topBlock.getType();
		}
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (!AzutoruMethods.isDust(topBlock)) {
			if (!AzutoruMethods.isIgnoredPlant(topBlock)) {
				remove();
				return;
			} else {
				if (!AzutoruMethods.isDust(topBlock.getRelative(BlockFace.DOWN, 2))) {
					remove();
					return;
				}
			}
		}
		
		if (!AzutoruMethods.isDust(topBlock) && (!AzutoruMethods.isIgnoredPlant(topBlock) && !AzutoruMethods.isDust(topBlock.getRelative(BlockFace.DOWN, 2)))) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			remove();
			return;
		}
		
		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(topBlock.getLocation(), heightRemoveThreshold)) {
			remove();
			return;
		}
		
		Block eyeBlock = player.getEyeLocation().getBlock();
		if (isWater(eyeBlock) || GeneralMethods.isSolid(eyeBlock)) {
			remove();
			return;
		}
		
		player.setFallDistance(0);
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		
		currentHeight = player.getLocation().getY() - topBlock.getY();
		if (currentHeight > height) {
			removeFlight();
		} else {
			allowFlight();
		}
		
		rotateDustColumn();
	}
	
	private void rotateDustColumn() {
		for (double i = 0; i <= currentHeight; i += 0.5) {
			Location loc = player.getLocation().subtract(0, i, 0);
			double radius = 1;
			for (double a = 0; a <= Math.PI * 2; a += Math.PI / 4) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
				loc.add(x, 0, z);
				ParticleEffect.BLOCK_DUST.display(loc, 1, 0, Math.random(), 0, 1, topBlockType.createBlockData());
				loc.subtract(x, 0, z);
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					DamageHandler.damageEntity(entity, damage, this);
					if (entity instanceof Player) {
						Player target = (Player) entity;
						target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessTime, 1));
					}
				}
			}
		}
	}
	
	public void remove() {
		super.remove();
		if (CoreAbility.hasAbility(player, DustDevilRush.class)) {
			CoreAbility.getAbility(player, DustDevilRush.class).remove();
		}
		bPlayer.addCooldown(this);
		flightHandler.removeInstance(player, getName());
		removeFlight();
		return;
	}
	
	private void allowFlight() {
		if (!player.getAllowFlight()) {
			player.setAllowFlight(true);
		}
		if (!player.isFlying()) {
			player.setFlying(true);
		}
	}
	
	private void removeFlight() {
		if (player.getAllowFlight()) {
			player.setAllowFlight(false);
		}
		if (player.isFlying()) {
			player.setFlying(false);
		}
	}
	
	private boolean isWithinMaxSpoutHeight(Location baseBlockLocation, double threshold) {
		if (baseBlockLocation == null) {
			return false;
		}
		
		double playerHeight = player.getLocation().getY();
		if (playerHeight > baseBlockLocation.getY() + height + threshold) {
			return false;
		}
		return true;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "DustDevil";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a sandbender to mount themselves on a column of dust or sand. The rotating dust particles slightly damage entities that come into contact with them and cause temporary blindness.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click to start using DustDevil. Hold jump to ascend and sneak to descend. Left-click once again to disable the move.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
