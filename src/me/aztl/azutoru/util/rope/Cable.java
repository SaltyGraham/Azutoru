package me.aztl.azutoru.util.rope;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * A type of Versatile Rope designed for MetalCables
 */
public class Cable extends Versatile {
	
	public static enum CableAbility {
		SLAM,
		SPIN,
		GRAPPLE,
		GRAPPLE_PULL,
		GRAB,
		GRAB_PULL,
		RETRACT;
	}
	
	private CableAbility ability;

	public Cable(Location start, Vector direction, int length, int maxLength, double interval, double speed) {
		super(start, direction, length, maxLength, interval, speed);
	}
	
	public Cable(Cable other) {
		super(other);
	}
	
	public CableAbility getAbility() {
		return ability;
	}
	
	public void setAbility(CableAbility ability) {
		this.ability = ability;
	}

}
