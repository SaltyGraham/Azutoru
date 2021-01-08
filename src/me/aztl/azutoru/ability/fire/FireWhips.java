package me.aztl.azutoru.ability.fire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SolidLiquidPolicy;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.PlayerUtil.Hand;
import me.aztl.azutoru.util.rope.Rope;

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
	private Predicate<Location> cutPolicy;
	private RemovalPolicy policy;
	
	public FireWhips(Player player) {
		super(player);
		
		if (hasAbility(player, FireWhips.class)) {
			getAbility(player, FireWhips.class).onClick();
			return;
		}
		
		if (!bPlayer.canBend(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Fire.FireWhips.Cooldown");
		duration = c.getLong("Abilities.Fire.FireWhips.Duration");
		hitRadius = c.getDouble("Abilities.Fire.FireWhips.HitRadius");
		knockback = c.getDouble("Abilities.Fire.FireWhips.Knockback");
		knockup = c.getDouble("Abilities.Fire.FireWhips.Knockup");
		damage = c.getDouble("Abilities.Fire.FireWhips.Damage");
		maxLength = c.getDouble("Abilities.Fire.FireWhips.MaxLength") * 2;
		speed = c.getDouble("Abilities.Fire.FireWhips.Speed");
		attackSpeed = c.getDouble("Abilities.Fire.FireWhips.AttackSpeed");
		requireConstantMotion = c.getBoolean("Abilities.Fire.FireWhips.RequireConstantMotion");

		cutPolicy = Rope.STANDARD_CUT_POLICY;
		rightLoc = PlayerUtil.getHandPos(player, Hand.RIGHT).setDirection(player.getEyeLocation().getDirection());
		right = new Rope(rightLoc, player.getEyeLocation().getDirection(), 5, 60, 0.5, 1);
		right.setCutPolicy(cutPolicy);
		time = System.currentTimeMillis();
		target = GeneralMethods.getTargetedLocation(player, 5);
		
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ExpirationPolicy(duration))
					.add(new ProtectedRegionPolicy(this, () -> right.getEndLocation()))
					.add(new SolidLiquidPolicy(() -> rightLoc, () -> rightLoc.getDirection())).build();
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (right != null) {
			rightLoc = PlayerUtil.getHandPos(player, Hand.RIGHT).setDirection(player.getEyeLocation().getDirection());
			revertSpeed(right);
			right.recalculate(rightLoc);
			right.getLocations().forEach(l -> display(l));
		}
		
		if (left != null) {
			leftLoc = PlayerUtil.getHandPos(player, Hand.LEFT).setDirection(player.getEyeLocation().getDirection());
			revertSpeed(left);
			left.recalculate(leftLoc);
			left.getLocations().forEach(l -> display(l));
		} else if (player.isSneaking()) {
			leftLoc = PlayerUtil.getHandPos(player, Hand.LEFT).setDirection(player.getEyeLocation().getDirection());
			left = new Rope(leftLoc, player.getEyeLocation().getDirection(), 5, 60, 0.5, 1);
			left.setCutPolicy(cutPolicy);
		}
		
		double cursorMovement = GeneralMethods.getDirection(GeneralMethods.getTargetedLocation(player, 5), target).length();
		if (cursorMovement >= 2) {
			if (right != null)
				extend(right);
			if (left != null)
				extend(left);
		}
		
		if (requireConstantMotion) {
			if (cursorMovement <= 0.05) {
				player.sendMessage("shorten whip");
				if (right != null)
					shorten(right);
				if (left != null)
					shorten(left);
			}
		}
		target = GeneralMethods.getTargetedLocation(player, 5);
	}
	
	private void display(Location loc) {
		if (isAir(loc.getBlock().getType())) {
			playFirebendingParticles(loc, 1, 0, 0, 0);
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, hitRadius)) {
				if (e != player) {
					Vector velocity = GeneralMethods.getDirection(loc, e.getLocation());
					velocity.multiply(knockback).setY(velocity.getY() * knockup);
					e.setVelocity(velocity);
					if (e instanceof LivingEntity) {
						DamageHandler.damageEntity(e, damage, this);
						new HorizontalVelocityTracker(e, player, 200, this);
					}
				}
			}
		}
	}
	
	private void revertSpeed(Rope rope) {
		if (FastMath.abs(rope.getSpeed() - 3) <= 0.1 && System.currentTimeMillis() - time >= 2000)
			rope.setSpeed(1);
	}
	
	private void extend(Rope rope) {
		if (rope.getLength() <= rope.getMaxLength())
			rope.setLength(rope.getLength() + 1);
	}
	
	private void shorten(Rope rope) {
		if (rope.getLength() > 5 && ThreadLocalRandom.current().nextBoolean())
			rope.setLength(rope.getLength() - 1);
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
		List<Location> total = new ArrayList<>(right.getLocations());
		if (left != null)
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
