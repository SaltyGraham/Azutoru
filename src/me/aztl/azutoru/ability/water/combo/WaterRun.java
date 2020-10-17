package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class WaterRun extends WaterAbility implements AddonAbility, ComboAbility {

	private double speed, health, damageThreshold;
	private long cooldown, duration;
	
	private Block topBlock, headBlock;
	private boolean surfaced;
	
	public WaterRun(Player player) {
		super(player);
		
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterRun.Speed");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.WaterRun.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.WaterRun.Duration");
		damageThreshold = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterRun.DamageThreshold");
		
		applyModifiers();
		
		health = player.getHealth();
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		headBlock = player.getLocation().add(0, 1.5, 0).getBlock();
		
		if (!bPlayer.canBendIgnoreBinds(this) || bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (hasAbility(player, WaterSpout.class) || hasAbility(player, WaterSpoutRush.class)) {
			return;
		}
		
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		if (!WaterAbility.isWater(topBlock) && !WaterAbility.isIce(topBlock)) {
			return;
		}
		
		start();
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			speed *= 1.1;
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			duration = (long) getNightFactor(duration);
			damageThreshold = getNightFactor(damageThreshold);
		}
		
		if (bPlayer.isAvatarState()) {
			speed *= 1.5;
			cooldown = 0;
			duration = 0;
			damageThreshold *= 2;
		}
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (player.getHealth() + damageThreshold <= health) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		if (topBlock == null) {
			remove();
			return;
		} else if (!WaterAbility.isWater(topBlock) && !WaterAbility.isIce(topBlock) && !AzutoruMethods.isIgnoredPlant(topBlock)) {
			remove();
			return;
		}
		
		headBlock = player.getLocation().add(0, 1.5, 0).getBlock();
		if (!ElementalAbility.isAir(headBlock.getType()) && !(WaterAbility.isWater(headBlock) && TempBlock.isTempBlock(headBlock))) {
			if (!surfaced) {
				Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(speed).setY(0.25);
				player.setVelocity(velocity);
			} else {
				remove();
			}
			return;
		} else {
			surfaced = true;
		}
		
		Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(speed);
		
		double waterHeight = topBlock.getY() + 1.2;
		double playerHeight = player.getLocation().getY();
		double displacement = waterHeight - playerHeight;
		
		velocity.setY(displacement * 0.5);
		
		player.setVelocity(velocity);
		
		if (player.getVelocity().length() < speed * 0.2) {
			remove();
			return;
		}
		
		playWakeAnimation();
	}
	
	public void playWakeAnimation() {
		
		Location leftWake = player.getLocation();
		if (leftWake.getYaw() > -30) {
			leftWake.setYaw(leftWake.getYaw() - 150);
		} else {
			leftWake.setYaw(leftWake.getYaw() + 180 + 40);
		}
		leftWake.setPitch(-5);
		Vector leftDir = leftWake.getDirection();
		
		Location rightWake = player.getLocation();
		if (rightWake.getYaw() < 30) {
			rightWake.setYaw(rightWake.getYaw() + 150);
		} else {
			rightWake.setYaw(rightWake.getYaw() - 180 - 40);
		}
		rightWake.setPitch(-5);
		Vector rightDir = rightWake.getDirection();
		
		for (double i = 0; i <= 3; i += 0.5) {
			leftWake.add(leftDir);
			rightWake.add(rightDir);
			if (WaterAbility.isWater(topBlock)) {
				ParticleEffect.WATER_SPLASH.display(leftWake, 5, 0, 0, 0, 10);
				ParticleEffect.WATER_SPLASH.display(rightWake, 5, 0, 0, 0, 10);
				ParticleEffect.SPIT.display(leftWake, 1, 0, .2, 0);
				ParticleEffect.SPIT.display(rightWake, 1, 0, 0.2, 0);
			} else if (WaterAbility.isIce(topBlock)) {
				ParticleEffect.SNOW_SHOVEL.display(leftWake, 1);
				ParticleEffect.SNOW_SHOVEL.display(rightWake, 1);
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this, cooldown);
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
		return "WaterRun";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a waterbender to quickly run across a water or ice surface. You can use other abilities while running if you need to. If you run ashore, you will lose connection with water and stop running. Activating WaterSpout also makes you stop running.";
	}
	
	@Override
	public String getInstructions() {
		return "WaterSpout (Hold sneak) > WaterManipulation (Left click) > WaterManipulation (Left click)";
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
	public Object createNewComboInstance(Player player) {
		return new WaterRun(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.LEFT_CLICK));
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
