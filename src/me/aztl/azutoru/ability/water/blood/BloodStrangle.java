package me.aztl.azutoru.ability.water.blood;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
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
	
	private Location location;
	private Map<Entity, Vector> grabbedEntities;
	private long time, init;
	
	public BloodStrangle(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.BloodStrangle.Range");
		grabRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.BloodStrangle.GrabRadius");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.BloodStrangle.Damage");
		undeadMobs = Azutoru.az.getConfig().getBoolean("Abilities.Water.BloodStrangle.CanBendUndeadMobs");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.BloodStrangle.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.BloodStrangle.Duration");
		
		init = System.currentTimeMillis();
		
		if (grabEntities()) {
			start();
		}
	}
	
	public boolean grabEntities() {
		grabbedEntities = new HashMap<>();
		
		for (int i = 1; i < range; i++) {
			location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, grabRadius)) {
				if (e instanceof LivingEntity 
						&& e.getUniqueId() != player.getUniqueId()
						&& (!GeneralMethods.isUndead(e) && !undeadMobs)
						&& !GeneralMethods.isRegionProtectedFromBuild(this, e.getLocation())) {
					if (e instanceof Player && e instanceof BendingPlayer) {
						Player victim = (Player) e;
						BendingPlayer bVictim = (BendingPlayer) victim;
						if (bVictim.canBeBloodbent()
								&& !Commands.invincible.contains(bVictim.getName())) {
							grabbedEntities.put(e, GeneralMethods.getDirection(location, e.getLocation()));
						}
					} else {
						grabbedEntities.put(e, GeneralMethods.getDirection(location, e.getLocation()));
					}
				}
			}
			if (grabbedEntities.size() > 2) {
				break;
			} else if (System.currentTimeMillis() > init + 500) {
				break;
			}
		}
		
		if (grabbedEntities == null || grabbedEntities.isEmpty()) {
			return false;
		}
		
		for (Entity e : grabbedEntities.keySet()) {
			if (grabbedEntities.size() == 1) {
				grabbedEntities.replace(e, new Vector (0, 0, 0));
			}
			DamageHandler.damageEntity(e, 0, this);
			if (e instanceof Player && e instanceof BendingPlayer) {
				BendingPlayer bVictim = BendingPlayer.getBendingPlayer((Player) e);
				bVictim.blockChi();
			}
		}
		
		return true;
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
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
				removal = checkForRemoval(e, removal);
				if (removal.contains(e)) {
					continue;
				}
				
				Location eLoc = e.getLocation();
				Location destination = location.add(grabbedEntities.get(e));
				double dx, dy, dz;
				dx = destination.getX() - eLoc.getX();
				dy = destination.getY() - eLoc.getY();
				dz = destination.getZ() - eLoc.getZ();
				Vector vector = new Vector(dx, dy, dz);
				if (location.distance(eLoc) > 0.5) {
					e.setVelocity(vector.normalize().multiply(0.3));
				} else {
					e.setVelocity(new Vector(0, 0, 0));
				}
				e.setFallDistance(0);
				AirAbility.breakBreathbendingHold(e);
				
				applyEffects((LivingEntity) e);
			}
			for (Entity e : removal) {
				grabbedEntities.remove(e);
			}
		}
	}
	
	public void applyEffects(LivingEntity e) {
		if (time < getStartTime() + 500) {
			e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 2));
		} else if (time >= getStartTime() + 500) {
			e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 500, 2));
			if (new Random().nextInt(10) == 0) {
				DamageHandler.damageEntity(e, damage, this);
			}
		}
	}
	
	public Set<Entity> checkForRemoval(Entity e, Set<Entity> removal) {
		if (e.isDead()) {
			removal.add(e);
		} else if (e instanceof Player) {
			Player victim = (Player) e;
			if (!victim.isOnline()) {
				removal.add(e);
			} else if (victim.getWorld() != player.getWorld()) {
				removal.add(e);
			} else if (victim.getLocation().distanceSquared(player.getLocation()) > range + 5) {
				removal.add(e);
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, victim.getLocation())) {
				removal.add(e);
			}
		}
		return removal;
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
