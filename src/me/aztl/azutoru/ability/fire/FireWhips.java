package me.aztl.azutoru.ability.fire;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;
import me.aztl.azutoru.util.Rope;

public class FireWhips extends FireAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double maxLength;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.SPEED)
	private double attackSpeed;
	private boolean requireConstantMotion;
	
	private long time;
	private Location rightLoc, leftLoc, target;
	private Rope right, left;
	private Predicate<Location> removalPolicy;
	private World world;
	
	public FireWhips(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireWhips.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireWhips.Duration");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.HitRadius");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.Knockback");
		knockup = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.Knockup");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.Damage");
		maxLength = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.MaxLength") * 2;
		speed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.Speed");
		attackSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireWhips.AttackSpeed");
		requireConstantMotion = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireWhips.RequireConstantMotion");
		
		removalPolicy = loc -> loc != null && (GeneralMethods.isSolid(loc.getBlock()) || loc.getBlock().isLiquid());
		world = player.getWorld();
		
		rightLoc = AzutoruMethods.getHandPos(player, Hand.RIGHT).setDirection(player.getEyeLocation().getDirection());
		right = new Rope(rightLoc, player.getEyeLocation().getDirection(), 5, 60, 0.5, 1, removalPolicy);
		time = System.currentTimeMillis();
		target = GeneralMethods.getTargetedLocation(player, 5);
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			remove();
			return;
		}
		
		rightLoc = AzutoruMethods.getHandPos(player, Hand.RIGHT).setDirection(player.getEyeLocation().getDirection());
		leftLoc = AzutoruMethods.getHandPos(player, Hand.LEFT).setDirection(player.getEyeLocation().getDirection());
		
		if (GeneralMethods.isSolid(rightLoc.getBlock()) || rightLoc.getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (Math.abs(right.getSpeed() - 3) <= 0.1 && System.currentTimeMillis() - time >= 2000) {
			right.setSpeed(1);
		}
		
		if (requireConstantMotion && GeneralMethods.getTargetedLocation(player, 5).distance(target) <= 0.05) {
			if (right != null && right.getLength() > 5) {
				right.setLength(right.getLength() - 1);
			}
			if (left != null && left.getLength() > 5) {
				left.setLength(left.getLength() - 1);
			}
		}
		
		right.recalculate(rightLoc);
		for (Location loc : right.getLocations()) {
			display(loc);
		}
		
		if (left != null) {
			if (Math.abs(left.getSpeed() - 3) <= 0.1 && System.currentTimeMillis() - time >= 2000) {
				left.setSpeed(1);
			}
			left.recalculate(leftLoc);
			for (Location loc : left.getLocations()) {
				display(loc);
			}
		} else if (player.isSneaking()) {
			left = new Rope(leftLoc, player.getEyeLocation().getDirection(), 5, 60, 0.5, 1, removalPolicy);
		}
		
		if (requireConstantMotion) target = GeneralMethods.getTargetedLocation(player, 5);
	}
	
	private void display(Location loc) {
		if (isAir(loc.getBlock().getType())) {
			playFirebendingParticles(loc, 1, 0, 0, 0);
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
	}
	
	public void onClick() {
		right.setSpeed(3);
		if (left != null)
			left.setSpeed(3);
		time = System.currentTimeMillis();
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getEyeLocation();
	}
	
	@Override
	public List<Location> getLocations() {
		List<Location> total = new ArrayList<>();
		total.addAll(right.getLocations());
		total.addAll(left.getLocations());
		return total;
	}

	@Override
	public String getName() {
		return "FireWhips";
	}
	
	@Override
	public String getDescription() {
		return "Demonstrated by Zuko in the Crystal Catacombs, this ability allows a skilled firebender to create a whip, or multiple whips, of fire. "
				+ "The whips have a tactile quality and can be used to knock back and damage enemies. The quicker the whip travels, the greater the knockback. "
				+ "The whip requires constant movement to stay extended.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) Left-click to create a whip."
				+ "\n(Extension) Move your cursor quickly around you to extend the whip."
				+ "\n(Attack) Left-click again to increase the whip speed and attack."
				+ "\n(Multiple) Tap sneak to create a second whip."
				+ "\n(Removal) Change slots to stop using the whips.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireWhips.Enabled");
	}

}
