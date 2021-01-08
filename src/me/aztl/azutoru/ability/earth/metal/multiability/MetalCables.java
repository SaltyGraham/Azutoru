package me.aztl.azutoru.ability.earth.metal.multiability;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.PlayerUtil.Hand;
import me.aztl.azutoru.util.rope.Cable;
import me.aztl.azutoru.util.rope.Cable.CableAbility;

public class MetalCables extends MetalAbility implements AddonAbility, MultiAbility {
	
	// General
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SPEED)
	private double cableLaunchSpeed;
	@Attribute(Attribute.SPEED)
	private double cableSwingSpeed;
	@Attribute(Attribute.SPEED)
	private double cableWhipSpeed;
	@Attribute(Attribute.DURATION)
	private double whipDuration;
	@Attribute(Attribute.RANGE)
	private int maxCableLength;
	private int maxUses;
	
	// Slam
	@Attribute(Attribute.RANGE)
	private int slamRange;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	
	// Grapple
	@Attribute(Attribute.SPEED)
	private double grapplePullSpeed;
	
	// Grab
	@Attribute(Attribute.SPEED)
	private double grabPullSpeed;
	@Attribute(Attribute.RADIUS)
	private double grabRadius;
	@Attribute(Attribute.SPEED)
	private double entityThrowSpeed;
	
	// Leap
	@Attribute(Attribute.SPEED)
	private double leapSpeed;
	
	// Retract
	@Attribute(Attribute.SPEED)
	private double retractSpeed;

	private Cable left, right;
	private Location leftLoc, rightLoc;
	private Vector direction;
	private RemovalPolicy policy;
	private long time;
	
	public MetalCables(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		MetalCables mc = getAbility(player, MetalCables.class);
		if (mc != null) {
			mc.activate(player.getInventory().getHeldItemSlot(), type);
			return;
		}
		
		if (type == ClickType.LEFT_CLICK) {
			MultiAbilityManager.bindMultiAbility(player, "MetalCables");
			FileConfiguration c = Azutoru.az.getConfig();
			
			// General
			cooldown = c.getLong("Abilities.Earth.MetalCables.Cooldown");
			duration = c.getLong("Abilities.Earth.MetalCables.Duration");
			cableSwingSpeed = c.getDouble("Abilities.Earth.MetalCables.SwingSpeed");
			cableLaunchSpeed = c.getDouble("Abilities.Earth.MetalCables.LaunchSpeed");
			cableWhipSpeed = c.getDouble("Abilities.Earth.MetalCables.WhipSpeed");
			whipDuration = c.getDouble("Abilities.Earth.MetalCables.WhipDuration");
			maxCableLength = c.getInt("Abilities.Earth.MetalCables.MaxRange") * 2;
			maxUses = c.getInt("Abilities.Earth.MetalCables.MaxUses");
			
			// Slam
			slamRange = c.getInt("Abilities.Earth.MetalCables.Slam.Range");
			hitRadius = c.getDouble("Abilities.Earth.MetalCables.Slam.HitRadius");
			damage = c.getDouble("Abilities.Earth.MetalCables.Slam.Damage");
			knockback = c.getDouble("Abilities.Earth.MetalCables.Slam.Knockback");
			knockup = c.getDouble("Abilities.Earth.MetalCables.Slam.Knockup");
			
			// Grapple
			grapplePullSpeed = c.getDouble("Abilities.Earth.MetalCables.Grapple.PullSpeed");
			
			// Grab
			grabPullSpeed = c.getDouble("Abilities.Earth.MetalCables.Grab.PullSpeed");
			grabRadius = c.getDouble("Abilities.Earth.MetalCables.Grab.GrabRadius");
			entityThrowSpeed = c.getDouble("Abilities.Earth.MetalCables.Grab.EntityThrowSpeed");
			
			// Leap
			leapSpeed = c.getDouble("Abilities.Earth.MetalCables.Leap.Push");
			
			// Retract
			retractSpeed = c.getDouble("Abilities.Earth.MetalCables.Retract.Speed");
			
			leftLoc = PlayerUtil.getHandPos(player, Hand.LEFT);
			rightLoc = PlayerUtil.getHandPos(player, Hand.RIGHT);
			direction = player.getEyeLocation().getDirection();
			
			policy = Policies.builder()
						.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
						.add(new ExpirationPolicy(duration))
						.add(new UsedAmmoPolicy(() -> maxUses)).build();
			
			start();
		}
	}
	
	public void activate(int slot, ClickType type) {
		switch (slot) {
		case 0: // "Slam Left"
			if (left == null) {
				left = create(leftLoc);
				left.setLength(slamRange);
			} else
				unlock(left);
			if (type == ClickType.LEFT_CLICK) {
				left.setAbility(CableAbility.SLAM);
				whip(left);
			} else if (type == ClickType.SHIFT_DOWN) {
				left.setAbility(CableAbility.SPIN);
			}
			break;
		case 1: // "Slam Right"
			if (right == null) {
				right = create(rightLoc);
				right.setLength(slamRange);
			} else
				unlock(right);
			if (type == ClickType.LEFT_CLICK) {
				right.setAbility(CableAbility.SLAM);
				whip(right);
			} else if (type == ClickType.SHIFT_DOWN) {
				right.setAbility(CableAbility.SPIN);
			}
			break;
		case 2: // "Grapple Left"
			if (left == null)
				left = create(leftLoc);
			else
				unlock(left);
			if (type == ClickType.LEFT_CLICK) {
				left.setAbility(CableAbility.GRAPPLE);
				whip(left);
			} else if (type == ClickType.SHIFT_DOWN) {
				left.setAbility(CableAbility.GRAPPLE_PULL);
			}
			break;
		case 3: // "Grapple Right"
			if (right == null)
				right = create(rightLoc);
			else
				unlock(right);
			if (type == ClickType.LEFT_CLICK) {
				right.setAbility(CableAbility.GRAPPLE);
				whip(right);
			} else if (type == ClickType.SHIFT_DOWN) {
				right.setAbility(CableAbility.GRAPPLE_PULL);
			}
			break;
		case 4: // "Grab Left"
			if (left == null)
				left = create(leftLoc);
			else {
				unlock(left);
				if (type == ClickType.LEFT_CLICK)
					whip(left);
			}
			if (type == ClickType.LEFT_CLICK) {
				left.setAbility(CableAbility.GRAB);
			} else if (type == ClickType.SHIFT_DOWN) {
				left.setAbility(CableAbility.GRAB_PULL);
			}
			if (left.hasEntity())
				grabThrow(left);
			break;
		case 5: // "Grab Right"
			if (right == null)
				right = create(rightLoc);
			else {
				unlock(right);
				whip(right);
			}
			if (type == ClickType.LEFT_CLICK) {
				right.setAbility(CableAbility.GRAB);
			} else if (type == ClickType.SHIFT_DOWN) {
				right.setAbility(CableAbility.GRAB_PULL);
			}
			if (right.hasEntity())
				grabThrow(right);
			break;
		case 6: // "Leap"
			leap();
			break;
		case 7: // "Retract"
			if (type == ClickType.LEFT_CLICK) {
				remove();
				return;
			}
			break;
		default:
			break;
		}
		maxUses--;
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (!MultiAbilityManager.hasMultiAbilityBound(player, "MetalCables")) {
			remove();
			return;
		}
		
		if (player.getInventory().getHeldItemSlot() == 7) {
			retract(left);
			retract(right);
		}
		
		direction = player.getEyeLocation().getDirection();
		leftLoc = PlayerUtil.getHandPos(player, Hand.LEFT).setDirection(direction);
		rightLoc = PlayerUtil.getHandPos(player, Hand.RIGHT).setDirection(direction);
		
		if (left != null) {
			if (left.getAbility() != CableAbility.SPIN)
				left.setHandLocation(leftLoc);
			
			if (left.getSpeed() == cableWhipSpeed && System.currentTimeMillis() > time + whipDuration)
				left.setSpeed(cableSwingSpeed);
			
			switch (left.getAbility()) {
			case SLAM:
				slam(left);
				break;
			case SPIN:
				spin(left);
				break;
			case GRAPPLE:
				grapple(left);
				break;
			case GRAPPLE_PULL:
				grapplePull();
				break;
			case GRAB:
				grab(left);
				break;
			case GRAB_PULL:
				grabPull();
				break;
			default:
				break;
			}
		}
		
		if (right != null) {
			if (right.getAbility() != CableAbility.SPIN)
				right.setHandLocation(rightLoc);
			
			if (right.getSpeed() == cableWhipSpeed && System.currentTimeMillis() > time + whipDuration)
				right.setSpeed(cableSwingSpeed);
			
			switch (right.getAbility()) {
			case SLAM:
				slam(right);
				break;
			case SPIN:
				spin(right);
				break;
			case GRAPPLE:
				grapple(right);
				break;
			case GRAPPLE_PULL:
				if (left != null && left.getAbility() == CableAbility.GRAPPLE_PULL) break;
				grapplePull();
				break;
			case GRAB:
				grab(right);
				break;
			case GRAB_PULL:
				if (left != null && left.getAbility() == CableAbility.GRAB_PULL) break;
				grabPull();
			default:
				break;
			}
		}
	}
	
	private void slam(Cable cable) {
		cable.recalculate(cable.getHandLocation());
		for (Location loc : cable.getLocations()) {
			display(loc);
			hit(loc);
		}
	}
	
	private void spin(Cable cable) {
		cable.spin(cable.getHandLocation());
		for (Location loc : cable.getLocations()) {
			display(loc);
			hit(loc);
		}
	}
	
	private void grapple(Cable cable) {
		if (!cable.isLocked() && GeneralMethods.isSolid(cable.getEndLocation().getBlock())) {
			cable.setLocked(true);
		}
		
		Location hand = cable.getHandLocation();
		
		if (hand.distance(cable.getEndLocation()) > cable.getLength() * cable.getInterval()) {
			Vector stabilizer = GeneralMethods.getDirection(hand, cable.getStartLocation()).multiply(0.25);
			player.setVelocity(stabilizer);
		}
		
		if (!cable.isLocked()) {
			cable.recalculate(hand);
			cable.setLength(cable.getLength() + (int) cableLaunchSpeed);
		} else {
			cable.adjust(hand);
		}
		
		for (Location loc : cable.getLocations())
			display(loc);
	}
	
	private void grapplePull() {
		Vector direction = new Vector();
		if (left != null && left.getAbility() == CableAbility.GRAPPLE_PULL && left.isLocked()
				&& right != null && right.getAbility() == CableAbility.GRAPPLE_PULL && right.isLocked()) {
			direction = GeneralMethods.getDirection(player.getEyeLocation(), MathUtil.getMidpoint(left.getEndLocation(), right.getEndLocation())).normalize();
		} else if (left != null  && left.getAbility() == CableAbility.GRAPPLE_PULL && left.isLocked() 
				&& (right == null || right.getAbility() != CableAbility.GRAPPLE_PULL || !right.isLocked())) {
			direction = GeneralMethods.getDirection(leftLoc, left.getEndLocation()).normalize();
		} else if ((left == null || left.getAbility() != CableAbility.GRAPPLE_PULL || !left.isLocked()) 
				&& right != null && right.getAbility() == CableAbility.GRAPPLE_PULL && right.isLocked()) {
			direction = GeneralMethods.getDirection(rightLoc, right.getEndLocation()).normalize();
		}
		
		player.setVelocity(direction.clone().multiply(grapplePullSpeed).subtract(new Vector(0, 0.1, 0)));
		
		if (left != null)
			grapple(left);
		if (right != null)
			grapple(right);
	}
	
	private void grab(Cable cable) {
		Location hand = cable.getHandLocation();
		cable.recalculate(hand);
		
		for (Location loc : cable.getLocations())
			display(loc);
		
		if (cable.hasEntity()) {
			if (!cable.getGrabbedEntity().getWorld().equals(hand.getWorld()))
				cable.setGrabbedEntity(null);
			else
				cable.drag();
		} else {
			for (Location loc : cable.getLocations()) {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, grabRadius)) {
					cable.setGrabbedEntity(e);
					cable.setLength((int) e.getLocation().distance(hand));
					cable.drag();
					return;
				}
			}
		}
	}
	
	private void grabPull() {
		if (left != null) {
			if (left.getLength() > 4)
				left.setLength(left.getLength() - (int) grabPullSpeed);
			grab(left);
		}
		if (right != null) {
			if (left.getLength() > 4)
				right.setLength(right.getLength() - (int) grabPullSpeed);
			grab(right);
		}
	}
	
	private void grabThrow(Cable cable) {
		Entity grabbed = cable.getGrabbedEntity();
		cable.setGrabbedEntity(null);
		GeneralMethods.setVelocity(grabbed, direction.clone().multiply(entityThrowSpeed));
	}
	
	private void leap() {
		player.setVelocity(direction.clone().multiply(leapSpeed));
	}
	
	private void retract(Cable cable) {
		if (cable != null && cable.getLength() > retractSpeed) {
			cable.setLength(cable.getLength() - (int) retractSpeed);
		}
	}
	
	private void display(Location loc) {
		if (isAir(loc.getBlock().getType())) {
			GeneralMethods.displayColoredParticle("767676", loc);
		}
	}
	
	private void hit(Location loc) {
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, hitRadius)) {
			if (e.getUniqueId() != player.getUniqueId()) {
				Vector velocity = GeneralMethods.getDirection(loc, e.getLocation());
				velocity.multiply(knockback).setY(velocity.getY() * knockup);
				e.setVelocity(velocity);
				if (e instanceof LivingEntity) {
					DamageHandler.damageEntity(e, damage, this);
				}
			}
		}
	}
	
	private Cable create(Location startLoc, int initialLength) {
		return new Cable(startLoc, direction, initialLength, maxCableLength, 0.5, cableSwingSpeed);
	}
	
	private Cable create(Location startLoc) {
		return create(startLoc, 10);
	}
	
	private void whip(Cable cable) {
		if (cable != null) {
			cable.setSpeed(cableWhipSpeed);
			time = System.currentTimeMillis();
		}
	}
	
	private void unlock(Cable cable) {
		if (cable != null && cable.isLocked()) {
			cable.setLocked(false);
			cable.setLength(cable.getLength() * 3 / 5);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		long cooldown = (long) ((double) (System.currentTimeMillis() - getStartTime()) / (double) duration);
		bPlayer.addCooldown(this, cooldown);
		MultiAbilityManager.unbindMultiAbility(player);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "MetalCables";
	}
	
	@Override
	public String getDescription() {
		return "This multiability allows a metalbender to manipulate metal cables that can be used for offense, mobility, and other purposes.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) Left-click to activate."
				+ "\n(Slam) Move cable to attack. Left-click to increase the speed of the whip briefly. Hold sneak to spin the cable(s) around you."
				+ "\n(Grapple) Left-click to grapple a solid surface. Hold sneak to pull you towards the surface."
				+ "\n(Grab) Left-click to attempt to grab an entity. Left-click again to throw the entity. Hold sneak to pull the entity towards you."
				+ "\n(Leap) Left-click to use your cables to jump in the direction you're looking."
				+ "\n(Retract) Hover over the slot to start pulling your cables back, or left-click to end the move entirely.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> abils = new ArrayList<>();
		abils.add(new MultiAbilityInfoSub("Slam Left", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Slam Right", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grapple Left", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grapple Right", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grab Left", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grab Right", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Leap", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Retract", Element.METAL));
		return abils;
	}

	@Override
	public String getAuthor() {
		return Azutoru.az.dev();
	}

	@Override
	public String getVersion() {
		return Azutoru.az.version();
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
	}
	
	@Override
	public boolean isEnabled() {
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.MetalCables.Enabled");
	}

}
