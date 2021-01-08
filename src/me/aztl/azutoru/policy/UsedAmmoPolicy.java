package me.aztl.azutoru.policy;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class UsedAmmoPolicy implements RemovalPolicy {
	
	private Supplier<Integer> ammo;
	private Predicate<Player> condition;
	
	public UsedAmmoPolicy(Supplier<Integer> ammo, @Nullable Predicate<Player> condition) {
		this.ammo = ammo;
		this.condition = condition;
	}

	public UsedAmmoPolicy(Supplier<Integer> ammo) {
		this(ammo, null);
	}
	
	@Override
	public boolean test(Player player) {
		return ammo.get() <= 0 && (condition != null ? condition.test(player) : true);
	}

}
