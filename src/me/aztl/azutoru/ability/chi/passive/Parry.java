package me.aztl.azutoru.ability.chi.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;
import me.aztl.azutoru.util.PlayerUtil;

public class Parry extends ChiAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private RemovalPolicy policy;
	
	public Parry(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)
				|| (hasAbility(player, Duck.class) && getAbility(player, Duck.class).isDucking())
				|| !PlayerUtil.isOnGround(player))
			return;
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Chi.Parry.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Chi.Parry.Duration");
		
		policy = Policies.builder()
					.add(new ExpirationPolicy(duration))
					.add(new SneakingPolicy(ProhibitedState.NOT_SNEAKING)).build();
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
	}
	
	public void removeWithCooldown() {
		remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation();
	}

	@Override
	public String getName() {
		return "Parry";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a chiblocker to block incoming attacks that originate close to the chiblocker. "
				+ "To use, hold sneak on any slot. You don't need to bind this move. If you're on the ground and "
				+ "the attack you're blocking came from someone or something that's close to you, you will be able "
				+ "to block their arm and prevent damage. Once you block one attack, the ability will go on cooldown.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Chi.Parry.Enabled");
	}

}
