package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
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
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;

public class FireStreams extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private double damage, range, hitRadius, speed, explosionRadius;
	
	private Location location, origin;
	private Vector direction;
	private double rotation;
	private int counter;
	
	public FireStreams(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (hasAbility(player, FireShield.class)) {
			getAbility(player, FireShield.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireStreams.Cooldown");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireStreams.Damage");
		range = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireStreams.Range");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireStreams.HitRadius");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireStreams.Speed");
		explosionRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireStreams.ExplosionRadius");
		
		applyModifiers();
		
		location = player.getEyeLocation();
		origin = location.clone();
		direction = location.getDirection();
		rotation = 0;
		counter = 0;
		
		start();
	}
	
	public void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			damage *= BlueFireAbility.getDamageFactor();
			range *= BlueFireAbility.getRangeFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			range = getDayFactor(range);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			damage *= 2;
			range *= 1.25;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("fireshield")) {
			remove();
			return;
		}
		
		if (location.distanceSquared(origin) > range * range) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock()) || location.getBlock().isLiquid()) {
			explode();
			remove();
			return;
		}
		
		if (player.isSneaking()) {
			direction.add(player.getEyeLocation().getDirection().multiply(0.5));
		} else {
			direction = origin.getDirection();
		}
		
		for (int i = 0; i < 3; i++) {
			location.add(direction.multiply(speed / 3));
			
			playFirebendingParticles(location, 3, 0.2, 0.2, 0.2);
			
			for (int j = 0; j < 2; j++) {
				Vector ortho = GeneralMethods.getOrthogonalVector(direction, rotation + 180 * j, 1);
				Location helixLoc = location.clone().add(ortho);
				playFirebendingParticles(helixLoc, 1, 0, 0, 0);
			}
			
			rotation += 10;
			
			if (counter % 6 == 0) {
				playFirebendingSound(location);
			}
			
			counter++;
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
				if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
					explode();
					remove();
					return;
				}
			}
		}
	}
	
	public void explode() {
		ParticleEffect.EXPLOSION_HUGE.display(location, 1);
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
		
		for (Block b : GeneralMethods.getBlocksAroundPoint(location, explosionRadius)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())) {
				continue;
			}
			
			createTempFire(b.getLocation());
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, explosionRadius)) {
			if (e instanceof LivingEntity) {
				DamageHandler.damageEntity(e, damage, this);
				double knockback = 1 / (0.1 + e.getLocation().distance(location));
				e.setVelocity(GeneralMethods.getDirection(location, e.getLocation().add(0, 1, 0)).multiply(knockback));
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "FireStreams";
	}
	
	@Override
	public String getDescription() {
		return "Demonstrated by Zuko during his fight with Aang in the Crystal Catacombs, "
				+ "this combo allows a firebender to create a long tendril of intertwined "
				+ "streams of flame that has a high damage potential.";
	}
	
	@Override
	public String getInstructions() {
		return "FireShield (Tap sneak) > FireShield (Tap sneak) > Blaze (Tap sneak) > FireShield (Left-click). "
				+ "Hold sneak to change the direction of the streams.";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FireStreams(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
		return combo;
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
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
	public boolean isEnabled() {
		return true;
	}

}
