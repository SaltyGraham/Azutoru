package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.fire.FireJet;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;

public class JetStepping extends FireAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("HorizontalPush")
	private double horizontal;
	@Attribute("VerticalPush")
	private double vertical;
	
	private RemovalPolicy policy;
	private int maxSteps;	
	
	public JetStepping(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (hasAbility(player, FireJet.class)) {
			getAbility(player, FireJet.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.JetStepping.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.JetStepping.Duration");
		horizontal = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetStepping.HorizontalPush");
		vertical = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetStepping.VerticalPush");
		maxSteps = Azutoru.az.getConfig().getInt("Abilities.Fire.JetStepping.MaxSteps");
		
		applyModifiers();
		
		policy = Policies.builder()
					.add(Policies.IN_LIQUID)
					.add(new ExpirationPolicy(duration))
					.add(new UsedAmmoPolicy(() -> maxSteps)).build();
		
		start();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			duration *= BlueFireAbility.getRangeFactor();
			horizontal *= BlueFireAbility.getRangeFactor();
			vertical *= BlueFireAbility.getRangeFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			duration = (long) getDayFactor(duration);
			horizontal = getDayFactor(horizontal);
			vertical = getDayFactor(vertical);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			horizontal *= 2;
			vertical *= 2;
			maxSteps += 10;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
	}
	
	private void playStepAnimation() {
		Location playerLoc = player.getLocation();
		for (double d = 0; d <= 2; d += 0.5) {
			playerLoc.subtract(0, d, 0);
			playFirebendingParticles(playerLoc, 1, 0.2, 0.2, 0.2);
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
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "JetStepping";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a firebender to propel themselves in small bursts that push the bender's feet upwards, acting as steps.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) FireJet (Tap sneak) > FireJet (Tap sneak) > Blaze (Left-click)"
				+ "\nRepeatedly left-click with Blaze to keep stepping.";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new JetStepping(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Blaze", ClickType.LEFT_CLICK));
		return combo;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.JetStepping.Enabled");
	}

}
