package me.aztl.azutoru.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
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
	
	public static List<Location> getCirclePoints(Location location, double radius, int steps) {
		List<Location> locations = new ArrayList<>();
		for (int i = 0; i < 360; i += 360 / steps)
			locations.add(location.add(radius * FastMath.cos(i), 0, radius * FastMath.sin(i)));
		return locations;
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
    	double radian = FastMath.toRadians(degrees);
    	Vector v1 = v.clone().multiply(FastMath.cos(radian));
    	Vector v2 = v.clone().crossProduct(rotated);
    	v2.multiply(FastMath.sin(radian));
    	return v1.add(v2);
    }
    
    // Vector3D methods
    
	/**
	 * Create an arc by combining {@link #rotate(Vector3D, Rotation, int)} and {@link #rotateInverse(Vector3D, Rotation, int)}.
	 * Amount of rays will be rounded up to the nearest odd number. Minimum value is 3.
	 * @param start the starting point
	 * @param rotation the rotation to use
	 * @param lines the amount of vectors to return, must be an odd number, minimum 3
	 * @return a list comprising of all the directions for this arc
	 * @author Moros
	 */
    public static Collection<Vector3D> createArc(Vector3D start, Rotation rotation, int lines) {
		lines = FastMath.max(3, lines);
		if (lines % 2 == 0) lines++;
		int half = (lines - 1) / 2;
		Collection<Vector3D> arc = new ArrayList<>(lines);
		arc.add(start);
		arc.addAll(rotate(start, rotation, half));
		arc.addAll(rotateInverse(start, rotation, half));
		return arc;
	}
    
	/**
	 * Repeat a rotation on a specific vector.
	 * @param start the starting point
	 * @param rotation the rotation to use
	 * @param times the amount of times to repeat the rotation
	 * @return a list comprising of all the directions for this arc
	 * @author Moros
	 */
    public static Collection<Vector3D> rotate(Vector3D start, Rotation rotation, int times) {
    	Collection<Vector3D> arc = new ArrayList<>();
    	double[] vector = start.toArray();
    	for (int i = 0; i < times; i++) {
    		rotation.applyTo(vector, vector);
    		arc.add(new Vector3D(vector));
    	}
    	return arc;
    }
    
	/**
	 * Inversely repeat a rotation on a specific vector.
	 * @author Moros
	 * @see #rotate(Vector3D, Rotation, int)
	 */
    public static Collection<Vector3D> rotateInverse(Vector3D start, Rotation rotation, int times) {
    	Collection<Vector3D> arc = new ArrayList<>();
    	double[] vector = start.toArray();
    	for (int i = 0; i < times; i++) {
    		rotation.applyInverseTo(vector, vector);
    		arc.add(new Vector3D(vector));
    	}
    	return arc;
    }
    
    /**
     * @author Moros
     */
    public static Vector orthogonal(Vector axis, double radians, double length) {
    	Vector3D v = toVector3D(axis);
    	double[] orthogonal = new Vector3D(v.getY(), -v.getX(), 0).normalize().scalarMultiply(length).toArray();
    	Rotation rotation = new Rotation(v, radians, RotationConvention.VECTOR_OPERATOR);
    	rotation.applyTo(orthogonal, orthogonal);
    	return fromVector3D(new Vector3D(orthogonal));
    }
    
    public static Vector fromVector3D(Vector3D v) {
    	return new Vector(v.getX(), v.getY(), v.getZ());
    }
    
    public static Vector3D toVector3D(Vector v) {
    	return new Vector3D(v.getX(), v.getY(), v.getZ());
    }

}
