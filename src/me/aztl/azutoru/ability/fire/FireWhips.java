package me.aztl.azutoru.ability.fire;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;

import me.aztl.azutoru.Azutoru;

public class FireWhips extends FireAbility implements AddonAbility {
	
	public FireWhips(Player player) {
		super(player);
		
		start();
	}
	
	@Override
	public void progress() {
		displayRightWhip();
		displayLeftWhip();
	}
	
	public void displayRightWhip() {
		
	}
	
	public void displayLeftWhip() {
		
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "FireWhips";
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
		return false;
	}

}
