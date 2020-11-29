package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;
import java.util.List;

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
import me.aztl.azutoru.AzutoruMethods;

public class AirWake extends AirAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double range, speed, knockback, knockup, damage, hitRadius;
	private int particleAmount, particleSpread;
	
	private Location location, origin;
	private Vector direction;
	private double counter = 0;
	
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
		particleAmount = Azutoru.az.getConfig().getInt("Abilities.Air.AirWake.ParticleAmount");
		particleSpread = Azutoru.az.getConfig().getInt("Abilities.Air.AirWake.ParticleSpread");
		
		location = player.getEyeLocation();
		origin = location.clone();
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
		
		if (counter % 6 == 0) {
			playAirbendingSound(location);
		}
		counter++;
		
		for (int i = 0; i < 2; i++) {
			if (!isTransparent(location.getBlock().getRelative(BlockFace.DOWN, i))) {
				location.setPitch(0);
				Vector newDirection = location.getDirection().multiply(speed);
				location.add(newDirection);
			} else {
				location.add(direction);
			}
			
		}
		
		displayWake(location, 0.5, 1.375);
		displayWake(location.clone().add(0, 1, 0), 0.5, 0.5);
		displayWake(GeneralMethods.getLeftSide(location, 0.75), 0.125, 0.5);
		displayWake(GeneralMethods.getRightSide(location, 0.75), 0.125, 0.5);
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
			if (entity.getUniqueId() != player.getUniqueId()) {
				Vector travelVec = direction.clone().multiply(knockback).setY(direction.getY() * knockup);
				entity.setVelocity(travelVec);
				if (entity instanceof LivingEntity) {
					DamageHandler.damageEntity(entity, damage, this);
					remove();
					return;
				}
			}
		}
	}
	
	private void displayWake(Location location, double width, double height) {
		List<Location> vertices = new ArrayList<>();
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				vertices.add(GeneralMethods.getLeftSide(location, width * i).add(0, height * j, 0));
			}
		}
		
		Location bottomRight = vertices.get(0);
		Location topRight = vertices.get(1);
		Location bottomLeft = vertices.get(2);
		Location topLeft = vertices.get(3);
		
		for (Location loc : AzutoruMethods.getLinePoints(player, bottomRight, topRight, particleSpread)) {
			getAirbendingParticles().display(loc, particleAmount);
		}
		for (Location loc : AzutoruMethods.getLinePoints(player, topRight, topLeft, particleSpread)) {
			getAirbendingParticles().display(loc, particleAmount);
		}
		for (Location loc : AzutoruMethods.getLinePoints(player, topLeft, bottomLeft, particleSpread)) {
			getAirbendingParticles().display(loc, particleAmount);
		}
		for (Location loc : AzutoruMethods.getLinePoints(player, bottomLeft, bottomRight, particleSpread)) {
			getAirbendingParticles().display(loc, particleAmount);
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
	public double getCollisionRadius() {
		return hitRadius;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Air.AirWake.Enabled");
	}

}
