package me.aztl.azutoru.policy;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import me.aztl.azutoru.util.PlayerUtil;

public enum Policies implements RemovalPolicy {
	IN_LIQUID(p -> p.getLocation().getBlock().isLiquid() || p.getEyeLocation().getBlock().isLiquid()),
	ON_GROUND(p -> { return PlayerUtil.isOnGround(p); } );
	
	private RemovalPolicy policy;
	
	Policies(RemovalPolicy policy) {
		this.policy = policy;
	}

	@Override
	public boolean test(Player player) {
		return policy.test(player);
	}
	
	public static PolicyBuilder builder() {
		return new PolicyBuilder();
	}
	
	public static class PolicyBuilder {
		private Set<RemovalPolicy> policies;
		
		private PolicyBuilder() {
			policies = new HashSet<>();
		}
		
		public PolicyBuilder add(RemovalPolicy policy) {
			policies.add(policy);
			return this;
		}
		
		public PolicyBuilder remove(RemovalPolicy policy) {
			policies.remove(policy);
			return this;
		}
		
		public RemovalPolicy build() {
			return new CompositeRemovalPolicy(this);
		}
		
		public Set<RemovalPolicy> getPolicies() {
			return policies;
		}
	}

}
