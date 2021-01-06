package me.aztl.azutoru.policy;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class RangePolicy implements RemovalPolicy {
	
	private double range;
	private Location origin;
	private Supplier<Location> current;
	private Predicate<Player> condition;
	
	public RangePolicy(double range, Location origin, Supplier<Location> current, @Nullable Predicate<Player> condition) {
		this.range = Math.abs(range);
		this.origin = origin;
		this.current = current;
		this.condition = condition;
	}
	
	public RangePolicy(double range, Location origin, Supplier<Location> current) {
		this(range, origin, current, null);
	}
	
	public RangePolicy(double range, Supplier<Location> current) {
		this(range, current.get(), current, null);
	}

	@Override
	public boolean test(Player player) {
		return current.get().distanceSquared(origin) >= range * range && (condition != null ? condition.test(player) : true);
	}

}
