package me.aztl.azutoru.ability.earth.sand.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.TopBlockPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.MathUtil;

public class DustStepping extends SandAbility implements AddonAbility, ComboAbility {

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
	
	public DustStepping(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		if (hasAbility(player, DustDevil.class))
			getAbility(player, DustDevil.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.DustStepping.Cooldown");
		duration = c.getLong("Abilities.Earth.DustStepping.Duration");
		horizontal = c.getDouble("Abilities.Earth.DustStepping.HorizontalPush");
		vertical = c.getDouble("Abilities.Earth.DustStepping.VerticalPush");
		maxSteps = c.getInt("Abilities.Earth.DustStepping.MaxSteps");
		maxDistance = c.getInt("Abilities.Earth.DustStepping.MaxDistanceFromGround");
		
		applyModifiers();
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), maxDistance);
		
		policy = Policies.builder()
				.add(new ExpirationPolicy(duration))
				.add(new TopBlockPolicy(() -> topBlock, b -> !isEarth(b)))
				.add(new UsedAmmoPolicy(() -> maxSteps)).build();
		
		if (isEarth(topBlock)) {
			start();
			step();
		}
	}
	
	private void applyModifiers() {
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
	}
	
	private void playStepAnimation() {
		Location playerLoc = player.getLocation();
		Location ground = topBlock.getLocation();
		ground.setX(playerLoc.getX());
		ground.setZ(playerLoc.getZ());
		for (Location loc : MathUtil.getLinePoints(player, ground, playerLoc, (int) ground.distance(playerLoc) * 2)) {
			ParticleEffect.BLOCK_DUST.display(loc, 1, 0.2, 0.2, 0.2, 1, topBlock.getType().createBlockData());
		}
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
		return "DustStepping";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a sandbender to create dust columns that push the bender's feet upwards, acting as steps."
				+ "It can be used as long as the user is not too far above an earthbendable block.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) DustDevil (Tap sneak) > DustDevil (Tap sneak) > EarthBlast (Left-click)"
				+ "\nRepeatedly left-click with EarthBlast to keep stepping.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new DustStepping(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("EarthBlast", ClickType.LEFT_CLICK));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustStepping.Enabled")
				&& Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustDevil.Enabled");
	}

}
