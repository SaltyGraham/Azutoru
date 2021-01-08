package me.aztl.azutoru.policy;

import java.util.Optional;
import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.projectkorra.projectkorra.BendingPlayer;

public class SwappedSlotsPolicy implements RemovalPolicy {

	private String expected;
	private Predicate<Player> condition;
	
	public SwappedSlotsPolicy(String expected, @Nullable Predicate<Player> condition) {
		this.expected = expected;
		this.condition = condition;
	}
	
	public SwappedSlotsPolicy(String expected) {
		this(expected, null);
	}
	
	@Override
	public boolean test(Player player) {
		Optional<BendingPlayer> bPlayer = Optional.ofNullable(BendingPlayer.getBendingPlayer(player));
		if (!bPlayer.isPresent()) return true;
		return !bPlayer.get().getBoundAbilityName().equalsIgnoreCase(expected) && (condition != null ? condition.test(player) : true);
	}

}
