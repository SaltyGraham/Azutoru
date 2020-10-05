package me.aztl.azutoru.ability.fire.bluefire.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;

public class Evaporate extends BlueFireAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double shieldRadius, particleSpread, speed, radiusIncreaseRate;
	private int particleAmount;
	
	private Location location;
	
	public Evaporate(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (hasAbility(player, FireShield.class)) {
			getAbility(player, FireShield.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.Evaporate.Cooldown");
		shieldRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.Evaporate.InitialShieldRadius");
		radiusIncreaseRate = Azutoru.az.getConfig().getDouble("Abilities.Fire.Evaporate.RadiusIncreaseRate");
		particleAmount = Azutoru.az.getConfig().getInt("Abilities.Fire.Evaporate.ParticleAmount");
		particleSpread = Azutoru.az.getConfig().getDouble("Abilities.Fire.Evaporate.ParticleSpread");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.Evaporate.Duration");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Fire.Evaporate.Speed");
		
		location = GeneralMethods.getTargetedLocation(player, 2);
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock()) || location.getBlock().isLiquid()) {
			remove();
			return;
		}
		
		displayCone();
	}
	
	public void displayCone() {
		location.add(location.getDirection().multiply(speed));
		shieldRadius += radiusIncreaseRate;
		
		for (double length = 0; length < shieldRadius; length += 0.5) {
			for (double angle = 0; angle < 360; angle += 20) {
				Vector ortho = GeneralMethods.getOrthogonalVector(location.getDirection(), angle, length);
				Location tendrilLoc = location.clone().add(ortho);
				
				if (!GeneralMethods.isSolid(tendrilLoc.getBlock()) && !tendrilLoc.getBlock().isLiquid()) {
					playFirebendingParticles(tendrilLoc, particleAmount, particleSpread, particleSpread, particleSpread);
				}
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, length)) {
					if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
						// The following effectively only scans for entities that are between the shield and the player.
						// I added a 1-block offset because things can move quite quickly sometimes.
						if (e.getLocation().distanceSquared(player.getLocation()) <= location.distanceSquared(player.getLocation()) + 1) {
							e.setFireTicks(80);
							new FireDamageTimer(e, player);
						}
					}
				}
			}
		}
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
		return "Evaporate";
	}
	
	@Override
	public String getDescription() {
		return "Demonstrated by Azula in her fight with Katara and Aang in the Crystal Catacombs, "
				+ "this combo allows a blue firebender to evaporate nearby water blocks "
				+ "and even incoming water attacks. This is due to blue fire's high temperature.";
	}
	
	@Override
	public String getInstructions() {
		return "FireShield (Hold sneak) > FireShield (Left-click)";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Evaporate(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
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
		return true;
	}

}
