package me.aztl.azutoru.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Rope {
	
	private Location start;
	private Vector direction;
	private int length;
	private double interval;
	
	private List<Location> points;
	
	public Rope(Location start, Vector direction, int length, double interval) {
		this.start = start;
		this.direction = direction;
		this.length = length;
		this.interval = interval;
		this.points = new ArrayList<>();
	}
	
	public Rope(Location start, Vector direction, int length) {
		this(start, direction, length, 1);
	}
	
	public Rope(Rope original) {
		this(original.getStartLocation(), original.getDirection(), original.getLength(), 1);
	}
	
	public void recalculate() {
		// This is where the physics will happen
	}
	
	public void add(List<Location> locations) {
		points.addAll(locations);
	}
	
	public void add(Location start, Vector direction, int length) {
		Location location = start.clone();
		List<Location> locations = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			locations.add(location);
			location.add(direction.multiply(interval));
		}
		add(locations);
	}
	
	public Location getPrevious(Location currentLoc) {
		int index = points.indexOf(currentLoc);
		return points.get(index - 1);
	}
	
	public Location getNext(Location currentLoc) {
		int index = points.indexOf(currentLoc);
		return points.get(index + 1);
	}
	
	public Location getStartLocation() {
		return start;
	}
	
	public void setStartLocation(Location start) {
		this.start = start;
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
			for (int i = 0; i < this.length; i++) {
				if (i > length) {
					add(points.get(points.size() - 1), direction, this.length);
					break;
				}
			}
		} else if (this.length > length) {
			for (int i = 0; i < length; i++) {
				if (i > this.length) {
					points.remove(i);
				}
			}
		}
		
		this.length = length;		
	}
	
	public double getInterval() {
		return interval;
	}
	
	public List<Location> getLocations() {
		return points;
	}

}
