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
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.ability.fire.FireJet;

public class JetStepping extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration, usageCooldown;
	private int steps, amount;
	private double lift, length, spread;
	
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
		steps = Azutoru.az.getConfig().getInt("Abilities.Fire.JetStepping.MaxSteps");
		usageCooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.JetStepping.StepCooldown");
		lift = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetStepping.Lift");
		length = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetStepping.ParticleLength");
		amount = Azutoru.az.getConfig().getInt("Abilities.Fire.JetStepping.ParticleAmount");
		spread = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetStepping.ParticleSpread");
		
		applyModifiers();
		
		start();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			duration *= BlueFireAbility.getRangeFactor();
			usageCooldown *= BlueFireAbility.getCooldownFactor();
			lift *= BlueFireAbility.getDamageFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			duration = (long) getDayFactor(duration);
			usageCooldown -= ((long) getDayFactor(usageCooldown) - usageCooldown);
			lift = getDayFactor(lift);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			usageCooldown /= 2;
			lift *= 1.5;
			steps += 10;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (player.isSneaking()) {
			remove();
			return;
		}
		
		if (!bPlayer.getBoundAbilityName().equals("FireJet")) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (AzutoruMethods.isOnGround(player)) {
			remove();
			return;
		}
		
		if (steps < 1) {
			remove();
			return;
		}
	}
	
	public void onClick() {
		if (bPlayer.isOnCooldown(getName() + "Step")) {
			return;
		}
		
		Vector direction = player.getEyeLocation().getDirection();
		if (direction.getY() < 0) {
			direction.setY(direction.getY() * -1);
		}
		
		player.setVelocity(direction.multiply(lift));
		
		Location loc = player.getLocation();
		Vector opposite = direction.multiply(-1);
		for (double d = 0; d <= length; d += 0.5) {
			loc.add(opposite);
			playFirebendingParticles(loc, amount, spread, spread, spread);
		}
		
		bPlayer.addCooldown(getName() + "Step", usageCooldown);
		steps--;
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
		return false;
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
		combo.add(new AbilityInformation("FireBlast", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
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
		return false;
	}

}
