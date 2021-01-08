package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;

public class AirCocoon extends AirAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private RemovalPolicy policy;
	
	public AirCocoon(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Air.AirCocoon.Cooldown");
		duration = c.getLong("Abilities.Air.AirCocoon.Duration");
		
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ExpirationPolicy(duration))
					.add(new SwappedSlotsPolicy("AirShield")).build();
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		displayCocoon();
		
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playAirbendingSound(player.getLocation());
		
		player.setVelocity(new Vector());
		
		GeneralMethods.getEntitiesAroundPoint(player.getLocation().add(0, 1, 0), 1)
			.stream().filter(e -> e != player)
			.forEach(e -> e.setVelocity(GeneralMethods.getOrthogonalVector(e.getVelocity(), 90, 1)));
	}
	
	private void displayCocoon() {
		Location loc = player.getLocation().add(0, 0.5, 0);
		for (double i = 0; i <= FastMath.PI; i += FastMath.PI / 5) {
			double radius = FastMath.sin(i);
			double y = FastMath.cos(i) * 1.5;
			for (double a = 0; a < FastMath.PI * 2; a += FastMath.PI / 5) {
				double x = FastMath.cos(a) * radius;
				double z = FastMath.sin(a) * radius;
				loc.add(x, y, z);
				getAirbendingParticles().display(loc, 1);
				loc.subtract(x, y, z);
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		return;
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
		return "AirCocoon";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an airbender to create a cocoon of air that briefly protects them against incoming attacks.";
	}
	
	@Override
	public String getInstructions() {
		return "AirShield (Left-click) > AirShield (Left-click) > AirShield (Tap sneak).\n"
				+ "You can cancel the move early by switching slots.";
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
		return new AirCocoon(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("AirShield", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("AirShield", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirShield", ClickType.SHIFT_UP));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Air.AirCocoon.Enabled");
	}
}
