package me.aztl.azutoru.util.rope;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

import me.aztl.azutoru.util.MathUtil;

/**
 * A Versatile, or versatile rope, is a more specific implementation of a Rope
 * with more functions geared towards multiabilities.
 */
public class Versatile extends Rope {
	
	public static final float ROTATION = 10;
	
	private Entity grabbedEntity;
	private Location handLoc;
	
	public Versatile(Location start, Vector direction, int length, int maxLength, double interval, double speed) {
		super(start, direction, length, maxLength, interval, speed);
	}

	public Versatile(Versatile other) {
		super(other);
	}
	
	/**
	 * Updates the points on the Versatile with a new starting location without applying rope physics.
	 * Versatile must be locked.
	 * @param start - the new starting location
	 */
	public void adjust(Location start) {
		if (!locked) return;
		setStartLocation(start);
		setDirection(GeneralMethods.getDirection(start, getEndLocation()).normalize());
		points = MathUtil.getLinePoints(start, getEndLocation(), length);
		if (cutPolicy != null) {
			Iterator<Location> it = points.stream().filter(cutPolicy).iterator();
			while (it.hasNext()) {
				Location point = it.next();
				remove(point);
			}
		}
	}
	
	/**
	 * Rotates a Versatile (spinning the rope) by adjusting the yaw
	 * @param start - the new starting location of the Versatile
	 */
	public void spin(Location start) {
		float yaw = start.getYaw() + ROTATION;
		start.setYaw(Location.normalizeYaw(yaw));
		setHandLocation(start);
		recalculate(start);
	}
	
	/**
	 * Drags the entity attached to the endpoint of the Versatile
	 */
	public void drag() {
		Vector dir = GeneralMethods.getDirection(grabbedEntity.getLocation(), getEndLocation());
		if (dir.length() <= 0.5)
			dir = new Vector();
		
		grabbedEntity.setVelocity(dir);
	}
	
	public boolean hasEntity() {
		return grabbedEntity != null && !grabbedEntity.isDead() && grabbedEntity.isValid();
	}
	
	public Entity getGrabbedEntity() {
		return grabbedEntity;
	}
	
	public void setGrabbedEntity(Entity grabbedEntity) {
		this.grabbedEntity = grabbedEntity;
	}
	
	public Location getHandLocation() {
		return handLoc;
	}
	
	public void setHandLocation(Location handLoc) {
		this.handLoc = handLoc;
	}

}
