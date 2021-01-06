package me.aztl.azutoru.policy;

import java.util.function.Predicate;

import org.bukkit.entity.Player;

public class SneakingPolicy implements RemovalPolicy {
	
	/**
	 * Either SNEAKING or NOT_SNEAKING, whatever is grounds for ability removal
	 */
	public enum ProhibitedState {
		SNEAKING(p -> p.isSneaking()),
		NOT_SNEAKING(p -> !p.isSneaking());
		
		private RemovalPolicy policy;
		
		private ProhibitedState(RemovalPolicy policy) {
			this.policy = policy;
		}
		
		public RemovalPolicy get() {
			return policy;
		}
	}
	
	private ProhibitedState state;
	private Predicate<Player> condition;
	
	public SneakingPolicy(ProhibitedState state, Predicate<Player> condition) {
		this.state = state;
		this.condition = condition;
	}
	
	public SneakingPolicy(ProhibitedState prohibitedState) {
		this(prohibitedState, null);
	}

	@Override
	public boolean test(Player player) {
		return state.get().test(player) && (condition != null ? condition.test(player) : true);
	}

}
