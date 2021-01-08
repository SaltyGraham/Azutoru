package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DamagePolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.WorldUtil;

public class WaterRun extends WaterAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.DAMAGE + "Threshold")
	private double damageThreshold;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private Block topBlock, headBlock;
	private RemovalPolicy policy;
	private boolean surfaced;
	
	public WaterRun(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this) 
				|| bPlayer.isOnCooldown(this)
				|| hasAbility(player, WaterSpout.class)
				|| hasAbility(player, WaterSpoutRush.class))
			return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		speed = c.getDouble("Abilities.Water.WaterRun.Speed");
		cooldown = c.getLong("Abilities.Water.WaterRun.Cooldown");
		duration = c.getLong("Abilities.Water.WaterRun.Duration");
		damageThreshold = c.getDouble("Abilities.Water.WaterRun.DamageThreshold");
		
		applyModifiers();
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		headBlock = player.getLocation().add(0, 1.5, 0).getBlock();
		
		policy = Policies.builder()
					.add(new DamagePolicy(damageThreshold, () -> player.getHealth()))
					.add(new ExpirationPolicy(duration)).build();
		
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		if (!isWater(topBlock) && !isIce(topBlock) && !isSnow(topBlock)) return;
		
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
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), 3);
		if (!isWater(topBlock) && !isIce(topBlock) && !isSnow(topBlock) && !WorldUtil.isIgnoredPlant(topBlock)) {
			remove();
			return;
		}
		
		headBlock = player.getLocation().add(0, 1.5, 0).getBlock();
		if (!isAir(headBlock.getType()) && !(isWater(headBlock) && TempBlock.isTempBlock(headBlock))) {
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
	
	private void playWakeAnimation() {
		Location leftWake = MathUtil.getModifiedLocation(player.getLocation().clone(), -150, -5);
		Location rightWake = MathUtil.getModifiedLocation(player.getLocation().clone(), 150, -5);
		
		displayWake(leftWake, leftWake.getDirection());
		displayWake(rightWake, rightWake.getDirection());
	}
	
	private void displayWake(Location loc, Vector dir) {
		for (double i = 0; i <= 3; i += 0.5) {
			loc.add(dir);
			if (isWater(topBlock)) {
				ParticleEffect.WATER_SPLASH.display(loc, 5, 0, 0, 0, 10);
				ParticleEffect.SPIT.display(loc, 1, 0, 0.2, 0);
			} else if (isIce(topBlock) || isSnow(topBlock)) {
				ParticleEffect.SNOW_SHOVEL.display(loc, 1);
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterRun.Enabled");
	}

}
