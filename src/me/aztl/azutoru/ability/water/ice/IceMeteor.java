package me.aztl.azutoru.ability.water.ice;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;

import me.aztl.azutoru.Azutoru;

public class IceMeteor extends IceAbility implements AddonAbility {

	public IceMeteor(Player player) {
		super(player);
	}

	@Override
	public void progress() {
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
		return 0;
	}

	@Override
	public String getName() {
		return "IceMeteor";
	}

	@Override
	public Location getLocation() {
		return null;
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
		return false;
	}

}
