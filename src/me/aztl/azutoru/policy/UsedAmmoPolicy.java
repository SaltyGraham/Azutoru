package me.aztl.azutoru.policy;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.projectkorra.projectkorra.ability.CoreAbility;

import me.aztl.azutoru.ability.util.Shot;

public class UsedAmmoPolicy implements RemovalPolicy {
	
	/**
	 * Predicate that checks if the player currently has a Shot ability instance active
	 */
	public static final Predicate<Player> NOT_SHOOTING = p -> CoreAbility.hasAbility(p, Shot.class);
	
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
