package me.aztl.azutoru.ability.fire;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;

public class FireDaggers extends FireAbility implements AddonAbility {

	private static enum Dagger {
		LEFT, RIGHT;
	}
	
	private static enum Ability {
		ATTACK, BLOCK;
	}
	
	private long cooldown, duration, blockDuration, usageCooldown;
	private double hitRadius, damage, range, throwSpeed;
	private int maxThrows;
	
	private Dagger activeDagger, lastActiveDagger;
	private Ability activeAbility;
	private Location left, right, location, handLoc;
	private long time;
	public boolean blocking;
	
	public FireDaggers(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.Duration");
		blockDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.BlockDuration");
		usageCooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.UsageCooldown");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.HitRadius");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.Damage");
		range = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.Range");
		throwSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.ThrowSpeed");
		maxThrows = Azutoru.az.getConfig().getInt("Abilities.Fire.FireDaggers.MaxThrows");
		
		blocking = false;
		
		start();
	}
	
	public void onClick() {
		if (bPlayer.isOnCooldown(getName() + "_ATTACK")) {
			return;
		}
		
		if (lastActiveDagger == null) {
			activeDagger = Dagger.RIGHT;
		} else if (lastActiveDagger == Dagger.RIGHT) {
			activeDagger = Dagger.LEFT;
		} else if (lastActiveDagger == Dagger.LEFT) {
			activeDagger = Dagger.RIGHT;
		}
		
		activeAbility = Ability.ATTACK;
		
		bPlayer.addCooldown(getName() + "_ATTACK", usageCooldown);
		maxThrows--;
	}
	
	public void onSneak() {
		if (bPlayer.isOnCooldown(getName() + "_BLOCK")) {
			return;
		}
		
		activeAbility = Ability.BLOCK;
		
		bPlayer.addCooldown(getName() + "_BLOCK", usageCooldown);
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
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		left = AzutoruMethods.getHandPos(player, Hand.LEFT);
		right = AzutoruMethods.getHandPos(player, Hand.RIGHT);
		
		if (left.getBlock().isLiquid() && right.getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (activeAbility != Ability.BLOCK) {
			time = System.currentTimeMillis();
			if (isBlocking()) {
				setBlocking(false);
			}
			
			for (double d = 0; d <= 0.3; d += 0.1) {
				left = GeneralMethods.getLeftSide(left, d).subtract(0, d * 0.6, 0);
				right = GeneralMethods.getRightSide(right, d).subtract(0, d * 0.6, 0);
				playFirebendingParticles(left, 1, 0, 0, 0);
				playFirebendingParticles(right, 1, 0, 0, 0);
			}
			
			if (activeAbility == Ability.ATTACK) {
				progressAttack(activeDagger);
			}
		} else {
			progressBlock();
			if (!isBlocking()) {
				setBlocking(true);
			}
		}
	}
	
	public void progressAttack(Dagger dagger) {
		if (handLoc == null) {
			if (dagger == Dagger.RIGHT) {
				handLoc = right;
			} else if (dagger == Dagger.LEFT) {
				handLoc = left;
			}
		}
		
		if (location == null) {
			location = handLoc.clone();
		}
		
		if (location.distanceSquared(handLoc) > range * range) {
			reset();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			reset();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock()) || location.getBlock().isLiquid()) {
			reset();
			return;
		}
		
		for (int i = 1; i < 9; i++) {
			location.add(GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, range).add(player.getEyeLocation().getDirection())).multiply(0.125).multiply(throwSpeed));
			playFirebendingParticles(location, 1, 0, 0, 0);
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, hitRadius + 1)) {
			if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
				if (e instanceof Player && e instanceof BendingPlayer) {
					Player victim = (Player) e;
					checkForCollisions(victim);
				}
				DamageHandler.damageEntity(e, damage, this);
				reset();
				return;
			}
		}
	}
	
	public void progressBlock() {
		while (System.currentTimeMillis() >= time) {
			left = left.subtract(0, 0.3, 0);
			right = right.subtract(0, 0.3, 0);
			break;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(left, 1)) {
			blockEntitiesAroundPoint(e, left);
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(right, 1)) {
			blockEntitiesAroundPoint(e, right);
		}
		
		for (double d = 0; d <= 0.4; d += 0.1) {
			left = GeneralMethods.getRightSide(left, d).add(0, 0.2, 0);
			right = GeneralMethods.getLeftSide(right, d).add(0, 0.2, 0);
			
			playFirebendingParticles(left, 1, 0, 0, 0);
			playFirebendingParticles(right, 1, 0, 0, 0);
		}
		
		if (!player.isSneaking() || System.currentTimeMillis() > time + blockDuration) {
			reset();
			return;
		}
	}
	
	public void blockEntitiesAroundPoint(Entity entity, Location location) {
		if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
			entity.setFireTicks(40);
			new FireDamageTimer(entity, player);
		} else if (entity instanceof Projectile) {
			entity.remove();
		}
	}
	
	public void checkForCollisions(Player victim) {
		if (hasAbility(victim, FireDaggers.class) && getAbility(victim, FireDaggers.class).isBlocking()) {
			reset();
			getAbility(victim, FireDaggers.class).setBlocking(false);
			return;
		} else if (hasAbility(victim, FireShield.class)) {
			reset();
			return;
		} else if (hasAbility(victim, AirShield.class)) {
			reset();
			return;
		}
	}
	
	public void reset() {
		checkForAmmo();
		handLoc = null;
		location = null;
		lastActiveDagger = activeDagger;
		activeDagger = null;
		activeAbility = null;
	}
	
	public void checkForAmmo() {
		if (maxThrows < 1) {
			remove();
			return;
		}
	}
	
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	
	public boolean isBlocking() {
		return blocking;
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
		if (activeAbility == null) {
			if (activeDagger == Dagger.LEFT) {
				return left;
			} else {
				return right;
			}
		} else if (activeAbility == Ability.ATTACK) {
			return location;
		} else if (activeAbility == Ability.BLOCK) {
			if (activeDagger == Dagger.LEFT) {
				return left;
			} else {
				return right;
			}
		}
		return right;
	}

	@Override
	public String getName() {
		return "FireDaggers";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a firebender to control daggers of flame in their hands.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click to create the daggers. Left-click again to attack with a dagger, and hold sneak to block attacks with your daggers.";
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
		return true;
	}

}
