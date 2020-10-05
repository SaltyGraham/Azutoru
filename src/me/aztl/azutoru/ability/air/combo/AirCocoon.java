package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;

public class AirCocoon extends AirAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long duration;
	
	public AirCocoon(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.AirCocoon.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.AirCocoon.Duration");
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		displayCocoon();
		
		Vector velocity = player.getVelocity().multiply(0);
		player.setVelocity(velocity);
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation().add(0, 0.5, 0), 1)) {
			if (entity.getUniqueId() != player.getUniqueId()) {
				Vector ortho = GeneralMethods.getOrthogonalVector(entity.getLocation().getDirection(), 90, 1);
				entity.setVelocity(ortho);
			}
		}
	}
	
	public void displayCocoon() {
		Location loc = player.getLocation().add(0, 0.5, 0);
		for (double i = 0; i <= Math.PI; i += Math.PI / 5) {
			double radius = Math.sin(i);
			double y = Math.cos(i) * 1.5;
			for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
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
		return "AirShield (Left-click) > AirShield (Left-click) > AirShield (Tap sneak)";
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
		return true;
	}
}
