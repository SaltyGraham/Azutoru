package me.aztl.azutoru.ability.water.ice.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.MathUtil;

public class MistStepping extends IceAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("HorizontalPush")
	private double horizontal;
	@Attribute("VerticalPush")
	private double vertical;
	private int maxSteps;
	@Attribute(Attribute.RANGE)
	private int maxDistance;
	
	private Block topBlock;
	private RemovalPolicy policy;
	
	public MistStepping(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		if (hasAbility(player, WaterSpout.class))
			getAbility(player, WaterSpout.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Water.MistStepping.Cooldown");
		duration = c.getLong("Abilities.Water.MistStepping.Duration");
		horizontal = c.getDouble("Abilities.Water.MistStepping.HorizontalPush");
		vertical = c.getDouble("Abilities.Water.MistStepping.VerticalPush");
		maxSteps = c.getInt("Abilities.Water.MistStepping.MaxSteps");
		maxDistance = c.getInt("Abilities.Water.MistStepping.MaxDistanceFromGround");
		
		applyModifiers();
		
		policy = Policies.builder()
					.add(new ExpirationPolicy(duration))
					.add(new UsedAmmoPolicy(() -> maxSteps)).build();
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), maxDistance);
		if (isWater(topBlock) || isIce(topBlock) || isSnow(topBlock) || WaterReturn.hasWaterBottle(player)) {
			start();
			step();
		}
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			duration = (long) getNightFactor(duration);
			horizontal = getNightFactor(horizontal);
			vertical = getNightFactor(vertical);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			horizontal *= 2;
			vertical *= 2;
			maxSteps += 10;
			maxDistance += 20;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), maxDistance);
		if (!(isWater(topBlock) || isIce(topBlock) || isSnow(topBlock) || WaterReturn.hasWaterBottle(player))) {
			remove();
			return;
		}
	}
	
	private void playStepAnimation() {
		Location playerLoc = player.getLocation();
		if (isWater(topBlock) || isIce(topBlock) || isSnow(topBlock)) {
			Location ground = topBlock.getLocation();
			for (Location loc : MathUtil.getLinePoints(player, ground, playerLoc, (int) ground.distance(playerLoc) * 2)) {
				displayParticles(loc);
			}
		} else {
			for (double d = 0; d <= 2; d += 0.5) {
				playerLoc.subtract(0, d, 0);
				displayParticles(playerLoc);
			}
		}
	}
	
	private void displayParticles(Location loc) {
		ParticleEffect.BLOCK_DUST.display(loc, 3, 0.2, 0.2, 0.2, 1, Material.BLUE_ICE.createBlockData());
	}
	
	public void step() {
		Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();
		direction.setX(direction.getX() * horizontal);
		direction.setY(vertical);
		direction.setZ(direction.getZ() * horizontal);
		
		player.setVelocity(direction);
		
		playStepAnimation();
		
		maxSteps--;
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
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "MistStepping";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an icebender to create small ice columns that push the bender's feet upwards, acting as stpes."
				+ "It can be used as long as the user is not too far above water, ice, or snow, or has a water bottle.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) WaterSpout (Tap sneak) > WaterSpout (Tap sneak) > IceSpike (Left-click)"
				+ "\nRepeatedly left-click with IceSpike to keep stepping.";
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
	public Object createNewComboInstance(Player player) {
		return new MistStepping(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("IceSpike", ClickType.LEFT_CLICK));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.MistStepping.Enabled");
	}

}
