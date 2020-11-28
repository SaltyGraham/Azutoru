package me.aztl.azutoru.ability.water.multiability;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;

import me.aztl.azutoru.Azutoru;

public class Transform extends WaterAbility implements AddonAbility, MultiAbility {

	public Transform(Player player) {
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
		return "Transform";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
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
