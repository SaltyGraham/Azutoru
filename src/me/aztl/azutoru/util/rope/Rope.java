package me.aztl.azutoru.util.rope;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

/**
 * Rope framework: A representation of a physical rope, whip, cable, or tentacle, with several points along a line.
 */
public class Rope implements Cloneable {
	
	protected Location start;
	protected Vector direction;
	protected int length;
	protected int maxLength;
	protected double interval;
	protected double speed;
	protected boolean locked;
	protected Predicate<Location> cutPolicy;
	protected List<Location> points;
	
	/**
	 * A removal policy that removes points from the Rope if they are in a solid or liquid block.
	 */
	public static final Predicate<Location> STANDARD_CUT_POLICY = loc -> loc != null && (GeneralMethods.isSolid(loc.getBlock()) || loc.getBlock().isLiquid());

	/**
	 * Creates a new Rope from a start Location into a given direction.
	 * @param start - the Location to start the Rope from. Can be adjusted with {@link #setStartLocation(Location)}
	 * @param direction - the Vector direction to extend the Rope into (initially). Can be adjusted with {@link #setDirection(Vector)}
	 * @param length - the initial number of Locations the Rope will contain. Can be adjusted with {@link #setLength(int, Focus)}
	 * @param maxLength - the maximum length of the Rope. Can be adjusted with {@link #setMaxLength(int)}
	 * @param interval - the space between each Location
	 * @param speed - the movement speed of the Rope. Can be adjusted with {@link #setSpeed(double)}
	 * @param cutPolicy - the conditions for the removal of one of the Locations (and all Locations after it)
	 */
	public Rope(Location start, Vector direction, int length, int maxLength, double interval, double speed) {
		this.start = start;
		this.direction = direction;
		this.length = 0;
		this.maxLength = maxLength;
		this.interval = interval;
		this.speed = speed;
		this.locked = false;
		this.points = new ArrayList<>(length);
		add(start, direction, length);
	}
	
	/**
	 * Creates a new Rope with all the elements of another.
	 * @param other - the Rope to copy
	 */
	public Rope(Rope other) {
		this.start = other.start.clone();
		this.direction = other.direction.clone();
		this.length = other.length;
		this.maxLength = other.maxLength;
		this.interval = other.interval;
		this.speed = other.speed;
		this.cutPolicy = other.cutPolicy;
		this.locked = other.locked;
		this.points = new ArrayList<>(other.points);
	}

	/**
	 * Updates the points on the Rope in a manner consistent with rope physics.
	 * Rope must be unlocked.
	 * @param start - the new starting location of the Rope
	 */
	public void recalculate(Location start) {
		if (locked) return;
		setStartLocation(start);
		setDirection(start.getDirection());
		
		List<Location> destinations = new ArrayList<>(points.size());
		destinations.add(start);

		for (int i = 1; i <= this.length; i++) {
			Location loc = start.clone().add(start.getDirection().clone().multiply(i * interval));
			destinations.add(loc);
		}
		
		for (Location point : points) {
			if (points.indexOf(point) == 0) continue;
			Location destination = destinations.get(points.indexOf(point));
			Vector dir = GeneralMethods.getDirection(point, destination);
			double mod = 1D / (double) (points.indexOf(point) - points.indexOf(start));
			if (points.indexOf(point) != 1 && points.indexOf(point) != 2)
				mod *= this.speed;

			point.add(dir.clone().multiply(mod));
		}

		if (cutPolicy != null) {
			List<Location> removal = points.stream().filter(cutPolicy).collect(Collectors.toList());
			if (!removal.isEmpty())
				remove(removal.get(0));
		}
	}

	/**
	 * Adds a number of new points to the Rope
	 * @param start - the starting Location
	 * @param direction - the Vector direction to add along
	 * @param length - the number of new points
	 */
	public void add(Location start, Vector direction, int length) {
		List<Location> locations = new ArrayList<>(length);

		for (int i = 0; i < length; ++i) {
			Location loc = start.clone().add(direction.clone().multiply(i * interval));
			locations.add(loc);
		}

		add(locations);
	}
	
	/**
	 * Adds a List of Locations to the Rope
	 * @param locations - the List<Location> to add
	 */
	public void add(List<Location> locations) {
		points.addAll(locations);
		length += locations.size();
	}
	
	/**
	 * Adds a number of new points to the beginning of the list of points
	 * @param start - the starting Location
	 * @param direction - the Vector direction to add along
	 * @param length - the number of new points
	 */
	public void addFirst(Location start, Vector direction, int length) {
		List<Location> locations = new ArrayList<>(length);
		
		for (int i = 0; i < length; i++) {
			Location loc = start.clone().add(direction.clone().multiply(i * interval));
			locations.add(loc);
		}
		
		locations.forEach(loc -> points.add(0, loc));
	}

	/**
	 * Safely removes a Location from the Rope, as well as any Location after it
	 * @param location - the Location to remove
	 */
	public void remove(Location location) {
		points.removeIf(point -> {
			return points.indexOf(point) >= points.indexOf(location) && points.indexOf(point) > 4;
		});
		length = points.size();
	}

	/**
	 * Removes a List of Locations from the Rope
	 * @param locations
	 */
	public void remove(List<Location> locations) {
		points.removeAll(locations);
		length = points.size();
	}

	/**
	 * Gets the location in the Rope immediately preceding the parameter
	 */
	public Location getPrevious(Location currentLoc) {
		int index = points.indexOf(currentLoc);
		return index == 0 ? points.get(index) : points.get(index - 1);
	}

	/**
	 * Gets the location in the Rope immediately succeeding the parameter
	 */
	public Location getNext(Location currentLoc) {
		int index = points.indexOf(currentLoc);
		return index == points.size() - 1 ? points.get(index) : points.get(index + 1);
	}
	public Location getStartLocation() {
		return start;
	}

	public void setStartLocation(Location start) {
		this.start = start;
		points.set(0, start);
	}

	public Location getEndLocation() {
		return points.get(points.size() - 1);
	}

	public void setEndLocation(Location end) {
		points.set(points.size() - 1, end);
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public int getLength() {
		return length;
	}
	
	/**
	 * Adjusts the length of the Rope (number of points) by adding or removing points at the end
	 * @param length - the new length of the Rope
	 */
	public void setLength(int length) {
		setLength(length, Focus.END);
	}

	/**
	 * Adjusts the length of the Rope (number of points) by adding or removing points at the focal point
	 * @param length - the new length of the Rope
	 * @param focalPoint - the end of the Rope at which to add/remove points
	 */
	public void setLength(int length, Focus focalPoint) {
		if (this.length < length) {
			if (focalPoint == Focus.START) {
				Vector opposite = new Vector(direction.getX() * -1, direction.getY() * -1, direction.getZ() * -1);
				addFirst(start.clone().add(opposite.clone().multiply(interval)), opposite, length - this.length);
			} else {
				add(getEndLocation().clone().add(direction.clone().multiply(interval)), direction, length - this.length);
			}
		} else if (this.length > length) {
			if (focalPoint == Focus.START) {
				points.removeIf(loc -> points.indexOf(loc) <= (this.length - length));
			} else {
				points.removeIf(loc -> points.indexOf(loc) >= length);
			}
			this.start = points.get(0);
			this.length = points.size();
		}
	}
	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public double getInterval() {
		return interval;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public Predicate<Location> getCutPolicy() {
		return cutPolicy;
	}
	
	public void setCutPolicy(Predicate<Location> cutPolicy) {
		this.cutPolicy = cutPolicy;
	}

	/**
	 * Gets the List of Locations that make up the Rope
	 */
	public List<Location> getLocations() {
		return points;
	}
	
	@Override
	public Rope clone() {
		return new Rope(this);
	}
	
	/**
	 * Checks if the Rope is equal to another by comparing their start locations, end locations, and length.
	 * @param other - another Rope to compare
	 */
	public boolean equals(Rope other) {
		return this.getStartLocation().equals(other.getStartLocation())
				&& this.getEndLocation().equals(other.getEndLocation())
				&& this.getLength() == other.getLength();
	}
	
	/**
	 * The focal point of the Rope, either {@link #START} or {@link #END}.
	 * <br>
	 * {@link #START} refers to the closest part of the Rope, with an index of 0.
	 * <br>
	 * {@link #END} refers to the farthest part of the Rope, or {@link Rope#getEndLocation()}.
	 * @see Rope#setLength(int, Focus)
	 */
	public static enum Focus {
		START, END;
	}
	
}