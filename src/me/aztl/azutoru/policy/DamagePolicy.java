package me.aztl.azutoru.policy;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DamagePolicy implements RemovalPolicy {
	
	private double damageThreshold;
	private double originalHealth;
	private Supplier<Double> currentHealth;
	private Predicate<Player> condition;
	
	public DamagePolicy(double damageThreshold, Supplier<Double> currentHealth, @Nullable Predicate<Player> condition) {
		this.damageThreshold = damageThreshold;
		this.originalHealth = currentHealth.get();
		this.currentHealth = currentHealth;
		this.condition = condition;
	}
	
	public DamagePolicy(double damageThreshold, Supplier<Double> currentHealth) {
		this(damageThreshold, currentHealth, null);
	}

	@Override
	public boolean test(Player player) {
		if (damageThreshold <= 0) return false;
		return currentHealth.get() <= originalHealth - damageThreshold && (condition != null ? condition.test(player) : true);
	}

}
