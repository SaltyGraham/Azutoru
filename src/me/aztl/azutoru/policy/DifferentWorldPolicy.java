package me.aztl.azutoru.policy;

import java.util.function.Supplier;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class DifferentWorldPolicy implements RemovalPolicy {
	
	private World original;
	private Supplier<World> current;
	
	public DifferentWorldPolicy(Supplier<World> current) {
		this.original = current.get();
		this.current = current;
	}

	@Override
	public boolean test(Player player) {
		return !original.equals(current.get());
	}

}
