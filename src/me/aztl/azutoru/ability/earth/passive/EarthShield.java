package me.aztl.azutoru.ability.earth.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthSmash;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;

public class EarthShield extends EarthAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.RADIUS)
	private double blockRadius;
	
	private Set<FallingBlock> affectedBlocks;
	private RemovalPolicy policy;
	
	public EarthShield(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (!bPlayer.canBendIgnoreCooldowns(getAbility(EarthSmash.class))) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.Crumble.Shield.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.Crumble.Shield.Duration");
		blockRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.Crumble.Shield.BlockRadius");
		
		affectedBlocks = new HashSet<>();
		policy = Policies.builder()
					.add(new ExpirationPolicy(duration))
					.add(new SneakingPolicy(ProhibitedState.NOT_SNEAKING))
					.add(new SwappedSlotsPolicy("EarthSmash")).build();
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || policy.test(player)) {
			remove();
			return;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), blockRadius)) {
			if (e instanceof FallingBlock) {
				FallingBlock fb = (FallingBlock) e;
				if (isEarth(fb.getBlockData().getMaterial())
						&& !affectedBlocks.contains(fb)) {
					Vector ortho = GeneralMethods.getOrthogonalVector(fb.getVelocity(), 90, 0.5);
					try {
						fb.setVelocity(ortho);
						affectedBlocks.add(fb);
					} catch (IllegalArgumentException exception) {
						// Don't affect falling blocks if ortho is not finite
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "EarthShield";
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
	public boolean isHiddenAbility() {
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.Crumble.Shield.Enabled");
	}

}
