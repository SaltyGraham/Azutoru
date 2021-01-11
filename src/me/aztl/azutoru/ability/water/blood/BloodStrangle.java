package me.aztl.azutoru.ability.water.blood;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;

public class BloodStrangle extends BloodAbility implements AddonAbility {

	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double grabRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private boolean undeadMobs;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;

	private Map<Entity, Vector> grabbedEntities;
	private Location location;
	private RemovalPolicy policy;
	private long time, init;
	
	public BloodStrangle(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		range = c.getDouble("Abilities.Water.BloodStrangle.Range");
		grabRadius = c.getDouble("Abilities.Water.BloodStrangle.GrabRadius");
		damage = c.getDouble("Abilities.Water.BloodStrangle.Damage");
		undeadMobs = c.getBoolean("Abilities.Water.BloodStrangle.CanBendUndeadMobs");
		cooldown = c.getLong("Abilities.Water.BloodStrangle.Cooldown");
		duration = c.getLong("Abilities.Water.BloodStrangle.Duration");
		
		init = System.currentTimeMillis();
		policy = Policies.builder()
					.add(new SneakingPolicy(ProhibitedState.NOT_SNEAKING))
					.add(new ExpirationPolicy(duration))
					.add(new ProtectedRegionPolicy(this, () -> location)).build();
		
		if (grabEntities())
			start();
	}
	
	public boolean grabEntities() {
		grabbedEntities = new HashMap<>();
		
		for (int i = 1; i < range; i++) {
			location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, grabRadius)) {
				if (e instanceof LivingEntity && e != player
						&& (!GeneralMethods.isUndead(e) && !undeadMobs)
						&& !GeneralMethods.isRegionProtectedFromBuild(this, e.getLocation())) {
					if (e instanceof Player) {
						BendingPlayer bVictim = BendingPlayer.getBendingPlayer((Player) e);
						if (bVictim != null && bVictim.canBeBloodbent()
								&& !Commands.invincible.contains(bVictim.getName())) {
							grabbedEntities.put(e, GeneralMethods.getDirection(location, e.getLocation()));
						}
					} else {
						grabbedEntities.put(e, GeneralMethods.getDirection(location, e.getLocation()));
					}
				}
			}
			if (grabbedEntities.size() > 2 || System.currentTimeMillis() > init + 500) break;
		}
		
		if (grabbedEntities == null || grabbedEntities.isEmpty()) return false;
		
		for (Entity e : grabbedEntities.keySet()) {
			if (grabbedEntities.size() == 1)
				grabbedEntities.replace(e, new Vector());
			DamageHandler.damageEntity(e, 0, this);
			if (e instanceof Player) {
				BendingPlayer bVictim = BendingPlayer.getBendingPlayer((Player) e);
				if (bVictim != null)
					bVictim.blockChi();
			}
		}
		
		return true;
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			return;
		}
		
		location = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
		time = System.currentTimeMillis();
		
		if (grabbedEntities == null || grabbedEntities.isEmpty()) {
			remove();
			return;
		} else {
			Set<Entity> removal = new HashSet<>();
			for (Entity e : grabbedEntities.keySet()) {
				if (shouldRemove(e)) {
					removal.add(e);
					continue;
				}
				
				Location eLoc = e.getLocation();
				Location destination = location.add(grabbedEntities.get(e));
				double dx = destination.getX() - eLoc.getX();
				double dy = destination.getY() - eLoc.getY();
				double dz = destination.getZ() - eLoc.getZ();
				Vector v = new Vector(dx, dy, dz);
				e.setVelocity((location.distanceSquared(eLoc) > 0.5 * 0.5) ? v.normalize().multiply(0.3) : new Vector());
				e.setFallDistance(0);
				AirAbility.breakBreathbendingHold(e);
				if (e instanceof LivingEntity)
					applyEffects((LivingEntity) e);
			}
			for (Entity e : removal) {
				grabbedEntities.remove(e);
				if (e instanceof Player) {
					BendingPlayer bVictim = BendingPlayer.getBendingPlayer((Player) e);
					if (bVictim != null) bVictim.unblockChi();
				}
			}
		}
	}
	
	public void applyEffects(LivingEntity le) {
		if (time < getStartTime() + 500) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 2));
		} else if (time >= getStartTime() + 500) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 500, 2));
			if (ThreadLocalRandom.current().nextInt(10) == 0) {
				DamageHandler.damageEntity(le, damage, this);
			}
		}
	}
	
	public boolean shouldRemove(Entity e) {
		if (e.isDead())
			return true;
		else if (e instanceof Player) {
			Player victim = (Player) e;
			return !victim.isOnline()
					|| victim.getWorld() != player.getWorld()
					|| victim.getLocation().distanceSquared(player.getLocation()) > range + 5
					|| GeneralMethods.isRegionProtectedFromBuild(this, victim.getLocation());
		}
		return false;
	}
	
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
		return location;
	}

	@Override
	public String getName() {
		return "BloodStrangle";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a bloodbender to control multiple entities at once and manipulate the blood in their bodies to gradually strangle them to death.";
	}
	
	@Override
	public String getInstructions() {
		return "Hold sneak while looking at bloodbendable targets.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.BloodStrangle.Enabled");
	}

}
