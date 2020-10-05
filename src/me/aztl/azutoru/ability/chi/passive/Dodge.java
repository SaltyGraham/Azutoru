package me.aztl.azutoru.ability.chi.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class Dodge extends ChiAbility implements AddonAbility {

	private long cooldown;
	
	public Dodge(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Multi-Elemental.Dodge.Cooldown");
		
		if (!AzutoruMethods.isOnGround(player) && !player.getLocation().getBlock().isLiquid() && player.isSneaking()) {
			start();
		}
	}
	
	@Override
	public void progress() {
		Location eyeTarget = GeneralMethods.getTargetedLocation(player, 2);
		Vector direction = GeneralMethods.getDirection(eyeTarget, player.getEyeLocation());
		player.setVelocity(direction.multiply(0.5));
		player.setFallDistance(0);
		ParticleEffect.CLOUD.display(player.getLocation(), 10, Math.random(), 0.2, Math.random());
		remove();
		bPlayer.addCooldown(this);
		
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation() != null ? player.getLocation() : null;
	}

	@Override
	public String getName() {
		return "Dodge";
	}
	
	@Override
	public String getDescription() {
		return "This passive allows a bender or chiblocker to quickly dodge an incoming attack. You do not need to bind this move. It will work on any slot.";
	}
	
	@Override
	public String getInstructions() {
		return "To use, you must be off the ground. Then, hold sneak and right-click (on any slot) in the direction opposite from where you want to go.";
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
