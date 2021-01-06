package me.aztl.azutoru.policy;

import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

public class SolidLiquidPolicy implements RemovalPolicy {
	
	private Supplier<Location> location;
	private Supplier<Vector> direction;
	
	public SolidLiquidPolicy(Supplier<Location> location, Supplier<Vector> direction) {
		this.location = location;
		this.direction = direction;
	}

	@Override
	public boolean test(Player player) {
		return GeneralMethods.isSolid(location.get().getBlock())
				|| location.get().getBlock().isLiquid()
				|| GeneralMethods.checkDiagonalWall(location.get(), direction.get());
	}

}
