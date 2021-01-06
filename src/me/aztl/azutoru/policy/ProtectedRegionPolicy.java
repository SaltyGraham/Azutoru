package me.aztl.azutoru.policy;

import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class ProtectedRegionPolicy implements RemovalPolicy {
	
	private CoreAbility ability;
	private Supplier<Location> location;

	public ProtectedRegionPolicy(CoreAbility ability, Supplier<Location> location) {
		this.ability = ability;
		this.location = location;
	}
	
	@Override
	public boolean test(Player player) {
		return GeneralMethods.isRegionProtectedFromBuild(ability, location.get());
	}

}
