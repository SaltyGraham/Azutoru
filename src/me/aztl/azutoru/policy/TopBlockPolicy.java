package me.aztl.azutoru.policy;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TopBlockPolicy implements RemovalPolicy {
	
	private Supplier<Block> block;
	private Predicate<Block> condition;
	
	public TopBlockPolicy(Supplier<Block> block, Predicate<Block> condition) {
		this.block = block;
		this.condition = condition;
	}

	@Override
	public boolean test(Player player) {
		return condition.test(block.get());
	}

}
