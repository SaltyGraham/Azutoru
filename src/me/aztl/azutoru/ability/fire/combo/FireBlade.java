package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.ability.fire.FireDaggers;

public class FireBlade extends FireAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	private float maxAngle;
	
	private Location startLoc, endLoc;
	private int id = 0;
	private HashMap<Integer, Location> locations;
	private HashMap<Integer, Vector> directions;
	private List<Location> locList;
	private boolean setup, progressing, dagger;
	private int counter, startDistance, arcSteps;
	
	public FireBlade(Player player, double range, double damage, double speed, int startDistance, int arcSteps, boolean dagger) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, FireBlade.class)) {
			return;
		}
		
		if (hasAbility(player, FireShield.class)) {
			getAbility(player, FireShield.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireBlade.Cooldown");
		this.speed = speed;
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireBlade.HitRadius");
		this.damage = damage;
		this.range = range;
		maxAngle = Azutoru.az.getConfig().getInt("Abilities.Fire.FireBlade.MaxAngle");
		this.startDistance = startDistance;
		this.arcSteps = arcSteps;
		this.dagger = dagger;
		
		applyModifiers();
		
		startLoc = GeneralMethods.getTargetedLocation(player, startDistance);
		locations = new HashMap<>();
		directions = new HashMap<>();
		locList = new ArrayList<>();
		// The following adjusts the maximum duration based on the range and speed.
		// It is meant to be used as a check to make sure the ability removes.
		duration = (long) (200 + range * (50 / speed));
		
		start();
		bPlayer.addCooldown(this);
	}
	
	public FireBlade(Player player) {
		this(player, 
				Azutoru.az.getConfig().getDouble("Abilities.Fire.FireBlade.Range"), 
				Azutoru.az.getConfig().getDouble("Abilities.Fire.FireBlade.Damage"),
				Azutoru.az.getConfig().getDouble("Abilities.Fire.FireBlade.Speed"),
				3,
				(int) Azutoru.az.getConfig().getDouble("Abilities.Fire.FireBlade.Range") * 3, 
				false);
	}
	
	private void applyModifiers() {
		if (dagger) {
			cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.UsageCooldown");
			hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.HitRadius");
		}
		
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			damage *= BlueFireAbility.getDamageFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			damage = getDayFactor(damage);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			damage *= 2;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!setup) {
			if (System.currentTimeMillis() < getStartTime() + 200) {
				
				endLoc = GeneralMethods.getTargetedLocation(player, startDistance);
				
				if (Math.abs(endLoc.getYaw() - startLoc.getYaw()) >= maxAngle) {
					setup = true;
				}
				if (Math.abs(endLoc.getPitch() - startLoc.getPitch()) >= maxAngle) {
					setup = true;
				}
				
				return;
			} else setup = true;
		}
		
		if (!progressing) {
			List<Location> linePoints = new ArrayList<Location>();
			linePoints = AzutoruMethods.getLinePoints(player, startLoc, endLoc, arcSteps);
			for (Location loc : linePoints) {
				locations.put(id, loc);
				directions.put(id, loc.getDirection());
				id++;
			}
			progressing = true;
		} else {
			if (locations.isEmpty()) {
				remove();
				return;
			}
			
			locList.clear();
			
			for (Integer i : locations.keySet()) {
				updateLocations(locations.get(i));
				
				Block b = locations.get(i).getBlock();
				if (GeneralMethods.isSolid(b) || b.isLiquid()) {
					continue;
				}
				
				if (GeneralMethods.checkDiagonalWall(locations.get(i), directions.get(i))) {
					continue;
				}
				
				if (locations.get(i).distanceSquared(startLoc) > range * range) {
					remove();
					return;
				}
				
				if (dagger) {
					for (int index = 0; index < 2; index++) {
						locations.get(i).add(directions.get(i).clone().multiply(speed / 2));
						playFirebendingParticles(locations.get(i), 1, 0.2, 0.2, 0.2);
					}
				} else {
					locations.get(i).add(directions.get(i).clone().multiply(speed));
					playFirebendingParticles(locations.get(i), 1, 0.2, 0.2, 0.2);
				}
				
				if (counter % 6 == 0) {
					playFirebendingSound(locations.get(i));
				}
				counter++;
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(locations.get(i), hitRadius)) {
					if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
						DamageHandler.damageEntity(e, damage, (hasAbility(player, FireDaggers.class) ? getAbility(player, FireDaggers.class) : this));
						remove();
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		locations.clear();
		directions.clear();
		locList.clear();
	}
	
	private void updateLocations(Location loc) {
		locList.add(loc);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return startLoc;
	}
	
	@Override
	public List<Location> getLocations() {
		return locList;
	}

	@Override
	public String getName() {
		return "FireBlade";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a firebender to send forth a blade of flame that damages players and mobs";
	}
	
	@Override
	public String getInstructions() {
		return "FireShield (Tap sneak) > FireShield (Tap sneak) > FireShield (Left-click)";
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
	public Object createNewComboInstance(Player player) {
		return new FireBlade(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
		return combo;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireBlade.Enabled");
	}

}
