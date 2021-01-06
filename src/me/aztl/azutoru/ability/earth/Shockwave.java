package me.aztl.azutoru.ability.earth;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;

public class Shockwave extends EarthAbility implements AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("FallThreshold")
	private float fallThreshold;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	
	private RemovalPolicy policy;
	private boolean released;

	public Shockwave(Player player, boolean fall) {
		super(player);
		
		if (!bPlayer.canBend(this) || hasAbility(player, Shockwave.class)) return;
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.Shockwave.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.Shockwave.Duration");
		range = Azutoru.az.getConfig().getDouble("Abilities.Earth.Shockwave.Range");
		fallThreshold = Azutoru.az.getConfig().getInt("Abilities.Earth.Shockwave.FallThreshold");
		chargeTime = Azutoru.az.getConfig().getLong("Abilities.Earth.Shockwave.ChargeTime");
		
		policy = Policies.builder()
					.add(new SneakingPolicy(ProhibitedState.NOT_SNEAKING, p -> !released)).build();
		
		if (fall) {
			if (player.getFallDistance() < fallThreshold) return;
			release(false);
			return;
		}
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (!released) {
			if (System.currentTimeMillis() > getStartTime() + chargeTime) {
				
			}
		}
	}
	
	private void release(boolean cone) {
		
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}
	
	@Override
	public List<Location> getLocations() {
		return null;
	}

	@Override
	public String getName() {
		return "Shockwave";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.Shockwave.Enabled");
	}

}
