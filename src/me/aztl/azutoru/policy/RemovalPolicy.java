package me.aztl.azutoru.policy;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface RemovalPolicy extends Predicate<Player> {
	
	boolean test(Player player);

}
