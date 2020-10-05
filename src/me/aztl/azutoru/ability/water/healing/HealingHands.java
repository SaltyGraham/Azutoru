package me.aztl.azutoru.ability.water.healing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;

public class HealingHands extends HealingAbility implements AddonAbility {

	private double range;
	private long cooldown;
	private int potionPotency;
	
	private Location location;
	
	public HealingHands(Player player) {
		super(player);
		
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.HealingHands.Range");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.HealingHands.Cooldown");
		potionPotency = Azutoru.az.getConfig().getInt("Abilities.Water.HealingHands.PotionPotency");
		location = player.getLocation().add(0, 1, 0);
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		location = GeneralMethods.getTargetedLocation(player, range);
		
		if (location.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
			location.add(0, 1.25, 0);
		}
		
		if (location.getBlock().getType() == Material.WATER) {
			
			for (Block affectedBlock : GeneralMethods.getBlocksAroundPoint(location, 3)) {
				location.getWorld().spawnParticle(Particle.REDSTONE, affectedBlock.getLocation(), 2, 0.1, 0.1, 0.1, 0.02, new DustOptions(Color.fromRGB(153, 237, 255), 1));
			}
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
				if (entity.getLocation().getBlock().getType() == Material.WATER && !TempBlock.isTempBlock(entity.getLocation().getBlock())) {
					if (entity instanceof Player && entity.getUniqueId() == player.getUniqueId()) {
						heal(player);
					} else if (entity instanceof LivingEntity) {
						heal((LivingEntity) entity);
					}
				}
			}
		}
	}
	
	public void heal(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				player.removePotionEffect(effect.getType());
			}
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, potionPotency));
		AirAbility.breakBreathbendingHold(player);
	}
	
	public void heal(LivingEntity livingEntity) {
		for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				livingEntity.removePotionEffect(effect.getType());
			}
		}
		livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, potionPotency));
		AirAbility.breakBreathbendingHold(livingEntity);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
		return "HealingHands";
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public String getInstructions() {
		return "";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
