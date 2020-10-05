package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;

import me.aztl.azutoru.Azutoru;

public class AirWake extends AirAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double range, speed, knockback, knockup, damage, hitRadius;
	
	private Location location, origin, head, lArm, rArm;
	private Vector direction;
	
	public AirWake(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.AirWake.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.AirWake.Duration");
		range = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.Range");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.Speed");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.Knockback");
		knockup = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.Knockup");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.Damage");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Air.AirWake.HitRadius");
		
		location = player.getLocation();
		origin = location.clone();
		head = location.clone().add(0, 1, 0);
		direction = location.getDirection().multiply(speed);
		
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (location.distanceSquared(origin) > range * range) {
			remove();
			return;
		}
		
		for (int i = 0; i < 2; i++) {
			if (!isTransparent(location.getBlock().getRelative(BlockFace.DOWN, i))) {
				location.setPitch(0);
				Vector newdirection = location.getDirection().multiply(speed);
				location.add(newdirection);
				head.add(newdirection);
			} else {
				location.add(direction);
				head.add(direction);
			}
		}
		
		lArm = GeneralMethods.getLeftSide(location, 1.5);
		rArm = GeneralMethods.getRightSide(location, 1.5);
		displayBody();
		displayHead();
		displayLeftArm();
		displayRightArm();
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
			if (entity.getUniqueId() != player.getUniqueId()) {
				Vector travelVec = GeneralMethods.getDirection(location, location.add(location.getDirection()));
				entity.setVelocity(travelVec.setY(knockup).multiply(knockback));
				if (entity instanceof LivingEntity) {
					DamageHandler.damageEntity(entity, damage, this);
					remove();
					return;
				}
			}
		}
	}
	
	public void displayBody() {
		Location loc = location.clone().add(0, 0.5, 0);
		for (double i = 0; i <= Math.PI; i += Math.PI / 7) {
			double radius = Math.sin(i);
			double y = Math.cos(i) * 2;
			for (double a = 0; a < Math.PI * 2; a += Math.PI / 7) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
				loc.add(x, y, z);
				getAirbendingParticles().display(loc, 1);
				loc.subtract(x, y, z);
			}
		}
	}
	
	public void displayHead() {
		Location loc = head.clone().add(0, 1, 0);
		for (double i = 0; i <= Math.PI; i += Math.PI / 5) {
			double radius = Math.sin(i) / 2;
			double y = Math.cos(i);
			for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
				loc.add(x, y, z);
				getAirbendingParticles().display(loc, 1);
				loc.subtract(x, y, z);
			}
		}
	}
	
	public void displayLeftArm() {
		Location loc = lArm.clone();
		for (double i = 0; i <= Math.PI; i += Math.PI / 5) {
			double radius = Math.sin(i) / 2;
			double y = Math.cos(i) * 2;
			for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
				loc.add(x, y, z);
				getAirbendingParticles().display(loc, 1);
				loc.subtract(x, y, z);
			}
		}
	}
	
	public void displayRightArm() {
		Location loc = rArm.clone();
		for (double i = 0; i <= Math.PI; i += Math.PI / 5) {
			double radius = Math.sin(i) / 2;
			double y = Math.cos(i) * 2;
			for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
				double x = Math.cos(a) * radius;
				double z = Math.sin(a) * radius;
				loc.add(x, y, z);
				getAirbendingParticles().display(loc, 1);
				loc.subtract(x, y, z);
			}
		}
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
		return "AirWake";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an airbender to create an aerokinetic duplicate of themselves and launch it towards an opponent. It has high concussive force and can do damage as well.";
	}
	
	@Override
	public String getInstructions() {
		return "AirShield (Tap sneak) > AirBurst (Left-click)";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new AirWake(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirShield", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirBurst", ClickType.LEFT_CLICK));
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
		return true;
	}

}
