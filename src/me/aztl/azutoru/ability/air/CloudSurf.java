package me.aztl.azutoru.ability.air;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class CloudSurf extends AirAbility implements AddonAbility {

	private long cooldown, duration;
	private boolean forceCloudParticles, allowSneakMoves;
	private double breakDamage;
	
	private Location cloudLoc;
	private double health;
	
	public CloudSurf(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (AzutoruMethods.isOnGround(player)) {
			return;
		}
		
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.CloudSurf.Duration");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.CloudSurf.Cooldown");
		forceCloudParticles = Azutoru.az.getConfig().getBoolean("Abilities.Air.CloudSurf.ForceCloudParticles");
		allowSneakMoves = Azutoru.az.getConfig().getBoolean("Abilities.Air.CloudSurf.AllowSneakMoves");
		breakDamage = Azutoru.az.getConfig().getDouble("Abilities.Air.CloudSurf.BreakDamage");
		health = player.getHealth();
		
		if (bPlayer.isAvatarState()) {
			duration = 0;
			cooldown = 0;
			breakDamage = breakDamage * 5;
		}
		
		flightHandler.createInstance(player, getName());
		allowFlight();
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
		
		if (player.isSneaking() && !bPlayer.getBoundAbilityName().equalsIgnoreCase("cloudsurf") && !allowSneakMoves) {
			remove();
			return;
		}
		
		if (player.getHealth() + breakDamage <= health) {
			remove();
			return;
		}
		
		if (AzutoruMethods.isOnGround(player)) {
			remove();
			return;
		}
		
		playCloudAnimation();
	}
	
	public void playCloudAnimation() {
		cloudLoc = player.getLocation().subtract(0, 0.5, 0);
		if (forceCloudParticles) {
			ParticleEffect.CLOUD.display(cloudLoc, 5, 0.8, 0.5, 0.8);
		} else {
			playAirbendingParticles(cloudLoc, 5, 0.8, 0.5, 0.8);
		}
	}
	
	private void allowFlight() {
		if (!player.getAllowFlight()) {
			player.setAllowFlight(true);
		}
		if (!player.isFlying()) {
			player.setFlying(true);
		}
	}
	
	private void removeFlight() {
		if (player.getAllowFlight()) {
			player.setAllowFlight(false);
		}
		if (player.isFlying()) {
			player.setFlying(false);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		flightHandler.removeInstance(player, getName());
		removeFlight();
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
		return "CloudSurf";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows airbenders to temporarily levitate atop a small cloud.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click while off the ground to start CloudSurfing. You may use other abilities while surfing. You must be hovering over your CloudSurf slot when you sneak to descend, or else the move will end.";
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
