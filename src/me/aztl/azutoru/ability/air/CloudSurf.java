package me.aztl.azutoru.ability.air;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DamagePolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;
import me.aztl.azutoru.util.PlayerUtil;

public class CloudSurf extends AirAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	private boolean forceCloudParticles;
	private boolean allowSneakMoves;
	@Attribute(Attribute.DAMAGE + "Threshold")
	private double damageThreshold;
	
	private Location cloudLoc;
	private RemovalPolicy policy;
	private boolean canFly, isFlying;
	
	public CloudSurf(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this) || PlayerUtil.isOnGround(player)) {
			return;
		}
		
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.CloudSurf.Duration");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.CloudSurf.Cooldown");
		forceCloudParticles = Azutoru.az.getConfig().getBoolean("Abilities.Air.CloudSurf.ForceCloudParticles");
		allowSneakMoves = Azutoru.az.getConfig().getBoolean("Abilities.Air.CloudSurf.AllowSneakMoves");
		damageThreshold = Azutoru.az.getConfig().getDouble("Abilities.Air.CloudSurf.DamageThreshold");
		
		if (bPlayer.isAvatarState()) {
			duration = 0;
			cooldown = 0;
			damageThreshold = damageThreshold * 5;
		}
		
		canFly = player.getAllowFlight();
		isFlying = player.isFlying();
		
		Predicate<Player> sneakConditions = p -> {
			return !BendingPlayer.getBendingPlayer(p).getBoundAbilityName().equals(getName()) && !allowSneakMoves;
		};
		
		policy = Policies.builder()
					.add(Policies.IN_LIQUID)
					.add(Policies.ON_GROUND)
					.add(new DamagePolicy(damageThreshold, () -> this.player.getHealth()))
					.add(new ExpirationPolicy(duration))
					.add(new SneakingPolicy(ProhibitedState.SNEAKING, sneakConditions)).build();
		
		flightHandler.createInstance(player, getName());
		PlayerUtil.allowFlight(player);
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		playCloudAnimation();
	}
	
	public void playCloudAnimation() {
		cloudLoc = player.getLocation().subtract(0, 0.5, 0);
		
		if (forceCloudParticles)
			ParticleEffect.CLOUD.display(cloudLoc, 5, 0.8, 0.5, 0.8);
		else
			playAirbendingParticles(cloudLoc, 5, 0.8, 0.5, 0.8);
		
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playAirbendingSound(cloudLoc);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		flightHandler.removeInstance(player, getName());
		PlayerUtil.removeFlight(player, canFly, isFlying);
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Air.CloudSurf.Enabled");
	}

}
