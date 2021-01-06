package me.aztl.azutoru.policy;

import java.util.Set;

import org.bukkit.entity.Player;

import me.aztl.azutoru.policy.Policies.PolicyBuilder;

public class CompositeRemovalPolicy implements RemovalPolicy {
	
	private Set<RemovalPolicy> policies;

	CompositeRemovalPolicy(PolicyBuilder builder) {
		policies = builder.getPolicies();
	}
	
	@Override
	public boolean test(Player player) {
		return player != null ? policies.stream().anyMatch(p -> p.test(player)) : true;
	}

}
