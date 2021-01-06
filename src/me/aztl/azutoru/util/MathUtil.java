package me.aztl.azutoru.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MathUtil {
	
	public static BlockFace getCardinalDirection(float yaw) {
		if (yaw >= -135 && yaw <= -45) {
			return BlockFace.EAST;
		} else if (yaw >= -45 && yaw <= 45) {
			return BlockFace.SOUTH;
		} else if (yaw >= 45 && yaw <= 135) {
			return BlockFace.WEST;
		} else {
			return BlockFace.NORTH;
		}
	}
	
	public static Vector getFaceDirection(BlockFace face) {
		switch (face) {
		case UP:
			return new Vector(0, 1, 0);
		case DOWN:
			return new Vector(0, -1, 0);
		case NORTH:
			return new Vector(0, 0, -1);
		case EAST:
			return new Vector(1, 0, 0);
		case SOUTH:
			return new Vector(0, 0, 1);
		case WEST:
			return new Vector(-1, 0, 0);
		default:
			return null;
		}
	}
	
	public static List<Location> getLinePoints(Location startLoc, Location endLoc, int steps) {
		List<Location> locations = new ArrayList<>(steps);
		Location diff = endLoc.clone().subtract(startLoc);
		double diffX = diff.getX() / steps;
		double diffY = diff.getY() / steps;
		double diffZ = diff.getZ() / steps;
		Location loc = startLoc.clone();
		for (int i = 0; i < steps; i++) {
			loc.add(diffX, diffY, diffZ);
			locations.add(loc.clone());
		}
		return locations;
	}
	
	public static List<Location> getLinePoints(Player player, Location startLoc, Location endLoc, int steps) {
		List<Location> locations = new ArrayList<>(steps);
		Location diff = endLoc.clone().subtract(startLoc);
		double diffX = diff.getX() / steps;
		double diffY = diff.getY() / steps;
		double diffZ = diff.getZ() / steps;
		Location loc = startLoc.clone();
		for (int i = 0; i < steps; i++) {
			loc.add(diffX, diffY, diffZ);
			loc.setDirection(loc.clone().subtract(player.getEyeLocation()).toVector().normalize());
			locations.add(loc.clone());
		}
		return locations;
	}
	
	public static Location getMidpoint(Location first, Location second) {
		if (!first.getWorld().equals(second.getWorld())) return null;
		return new Location(first.getWorld(), 
				(first.getX() + second.getX()) / 2D, 
				(first.getY() + second.getY()) / 2D, 
				(first.getZ() + second.getZ()) / 2D);
	}
	
	public static Location getModifiedLocation(Location location, float yawDiff) {
		location.setYaw(Location.normalizeYaw(location.getYaw() + yawDiff));
		return location;
	}
	
	public static Location getModifiedLocation(Location location, float yawDiff, float newPitch) {
		Location loc = getModifiedLocation(location.clone(), yawDiff);
		loc.setPitch(newPitch);
		return loc;
	}
	
	public static float getOppositeYaw(float yaw) {
		float opposite = yaw += 180;
		return Location.normalizeYaw(opposite);
	}
	
	public static Vector rotateAroundAxisX(Vector v, double cos, double sin) {
		double y = v.getY() * cos - v.getZ() * sin;
		double z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}
	
    public static Vector rotateAroundAxisY(Vector v, double cos, double sin) {
        double x = v.getX() * cos + v.getZ() * sin;
        double z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }
    
    public static Vector rotateAroundAxisZ(Vector v, double cos, double sin) {
        double x = v.getX() * cos - v.getY() * sin;
        double y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }
    
    public static Vector rotateAroundAxesXZ(Vector v, double degrees) {
    	Vector rotated = new Vector(-v.getZ(), 0, v.getX());
    	double radian = Math.toRadians(degrees);
    	Vector v1 = v.clone().multiply(Math.cos(radian));
    	Vector v2 = v.clone().crossProduct(rotated);
    	v2.multiply(Math.sin(radian));
    	return v1.add(v2);
    }

}
