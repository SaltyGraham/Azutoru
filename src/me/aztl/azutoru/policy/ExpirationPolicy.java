package me.aztl.azutoru.policy;

import org.bukkit.entity.Player;

public class ExpirationPolicy implements RemovalPolicy {
	
	private final long expireTime;
	private final boolean finite;
	
	public ExpirationPolicy(long duration) {
		expireTime = System.currentTimeMillis() + duration;
		finite = duration > 0;
	}

	@Override
	public boolean test(Player player) {
		return finite && System.currentTimeMillis() > expireTime;
	}

}
