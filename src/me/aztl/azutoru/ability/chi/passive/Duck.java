package me.aztl.azutoru.ability.chi.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class Duck extends ChiAbility implements AddonAbility {

	public static enum Stage {
		FIRST_SNEAK, FIRST_RELEASE, SECOND_SNEAK;
	}
	
	private long cooldown;
	private long duration;
	
	private boolean ableToDuck;
	private Stage stage;
	private boolean ducking;
	
	public Duck(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Chi.Duck.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Chi.Duck.Duration");
		
		if (player.isSneaking()) {
			stage = Stage.FIRST_SNEAK;
			start();
		}
	}
	
	@Override
	public void progress() {
		if (!AzutoruMethods.isOnGround(player)) {
			ableToDuck = false;
		} else {
			ableToDuck = true;
		}
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			removeWithCooldown();
			return;
		}
		
		if (stage == Stage.FIRST_SNEAK && ableToDuck) {
			if (System.currentTimeMillis() > getStartTime() + 1000) {
				remove();
				return;
			}
			if (!player.isSneaking()) {
				stage = Stage.FIRST_RELEASE;
			}
		} else if (stage == Stage.FIRST_RELEASE && ableToDuck) {
			if (System.currentTimeMillis() > getStartTime() + 1000) {
				remove();
				return;
			}
			if (player.isSneaking()) {
				stage = Stage.SECOND_SNEAK;
			}
		} else if (stage == Stage.SECOND_SNEAK) {
			ducking = true;
			player.setVelocity(player.getVelocity().multiply(0));
			startGliding();
			if (!player.isSneaking()) {
				stage = null;
			}
		} else {
			stopGliding();
			removeWithCooldown();
			return;
		}
	}
	
	public void startGliding() {
		if (!player.isGliding()) {
			player.setGliding(true);
		}
	}
	
	public void stopGliding() {
		if (player.isGliding()) {
			player.setGliding(false);
		}
	}
	
	public void removeWithCooldown() {
		super.remove();
		bPlayer.addCooldown(this);
	}
	
	public boolean isDucking() {
		return ducking;
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
		return "Duck";
	}
	
	@Override
	public String getDescription() {
		return "This passive allows a chiblocker to dodge an incoming attack by momentarily crouching. You do not need to bind this move. It will work on any slot.";
	}
	
	@Override
	public String getInstructions() {
		return "To use, tap sneak (on any slot), then hold sneak to duck. You can stop ducking by releasing sneak.";
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
