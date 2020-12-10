package me.aztl.azutoru.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

public class Rope {
	
	private Location start;
	private Vector direction;
	private int length;
	private int maxLength;
	private double interval;
	private double speed;
	private Predicate<Location> removalPolicy;
	private List<Location> points;

	public Rope(Location start, Vector direction, int length, int maxLength, double interval, double speed, Predicate<Location> removalPolicy) {
		this.start = start;
		this.direction = direction;
		this.length = 0;
		this.maxLength = maxLength;
		this.interval = interval;
		this.speed = speed;
		this.removalPolicy = removalPolicy;
		this.points = new ArrayList<>();
		add(start, direction, length);
	}

	public void recalculate(Location start) {
		setStartLocation(start);
		setDirection(start.getDirection());
		if (!points.isEmpty()) {
			List<Location> destinations = new ArrayList<>();
			destinations.add(start);

			for (int i = 1; i <= this.length; ++i) {
				Location loc = start.clone().add(start.getDirection().clone().multiply(i * interval));
				destinations.add(loc);
			}

			double magnitude = GeneralMethods.getDirection(points.get(4), destinations.get(4)).length();
			if (magnitude >= 2 && length <= maxLength) {
				setLength(length + 1);
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

			List<Location> removal = new ArrayList<>();
			Consumer<Location> remove = l -> {
				for (Location point : points) {
					if (points.indexOf(point) >= points.indexOf(l) && points.indexOf(point) > 4) {
						removal.add(point);
					}
				}
			};
			points.stream().filter(removalPolicy).forEach(remove);
			remove(removal);
		}
	}

	public void add(List<Location> locations) {
		points.addAll(locations);
		length += locations.size();
	}

	public void add(Location start, Vector direction, int length) {
		List<Location> locations = new ArrayList<>();

		for (int i = 0; i < length; ++i) {
			Location loc = start.clone().add(direction.clone().multiply(i * interval));
			locations.add(loc);
		}

		this.add(locations);
	}

	public void remove(List<Location> locations) {
		points.removeAll(locations);
		length = points.size();
	}

	public void remove(Location location) {
		points.remove(location);
		length = points.size();
	}

	public Location getPrevious(Location currentLoc) {
		int index = points.indexOf(currentLoc);
		return index == 0 ? points.get(index) : points.get(index - 1);
	}

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

	public void setLength(int length) {
		if (this.length < length) {
			add(getEndLocation().add(direction.multiply(interval)), direction, length - this.length);
		} else if (this.length > length) {
			points.removeIf(loc -> points.indexOf(loc) >= length);
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

	public List<Location> getLocations() {
		return points;
	}
	
}