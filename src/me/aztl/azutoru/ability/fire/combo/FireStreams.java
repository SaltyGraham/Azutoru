package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
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
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RangePolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SolidLiquidPolicy;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;

public class FireStreams extends FireAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RADIUS)
	private double explosionRadius;
	private double particleSpread;
	@Attribute(Attribute.RADIUS)
	private double helixRadius;
	private double helixParticleSpread;
	private int particleAmount;
	private int helixParticleAmount;
	
	private Location location, origin;
	private Vector direction;
	private RemovalPolicy policy;
	private double rotation;
	
	public FireStreams(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		if (hasAbility(player, FireShield.class))
			getAbility(player, FireShield.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Fire.FireStreams.Cooldown");
		damage = c.getDouble("Abilities.Fire.FireStreams.Damage");
		range = c.getDouble("Abilities.Fire.FireStreams.Range");
		hitRadius = c.getDouble("Abilities.Fire.FireStreams.HitRadius");
		speed = c.getDouble("Abilities.Fire.FireStreams.Speed");
		explosionRadius = c.getDouble("Abilities.Fire.FireStreams.ExplosionRadius");
		particleAmount = c.getInt("Abilities.Fire.FireStreams.ParticleAmount");
		particleSpread = c.getDouble("Abilities.Fire.FireStreams.ParticleSpread");
		helixRadius = c.getDouble("Abilities.Fire.FireStreams.Helix.Radius");
		helixParticleAmount = c.getInt("Abilities.Fire.FireStreams.Helix.ParticleAmount");
		helixParticleSpread = c.getDouble("Abilities.Fire.FireStreams.Helix.ParticleSpread");
		
		applyModifiers();
		
		location = player.getEyeLocation();
		origin = location.clone();
		direction = location.getDirection();
		rotation = 0;
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new RangePolicy(range, origin, () -> location))
					.add(new ProtectedRegionPolicy(this, () -> location))
					.add(new SolidLiquidPolicy(() -> location, () -> direction))
					.add(new SwappedSlotsPolicy("FireShield")).build();
		
		start();
	}
	
	private void applyModifiers() {
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
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			explode();
			remove();
			return;
		}
		
		if (player.isSneaking())
			direction.add(player.getEyeLocation().getDirection().multiply(0.5)).normalize().multiply(speed);
		
		for (int i = 0; i < 3; i++) {
			location.add(direction.clone().multiply(speed / 3));
			
			playFirebendingParticles(location, particleAmount, particleSpread, particleSpread, particleSpread);
			
			for (int j = 0; j < 2; j++) {
				Location helixLoc = location.clone().add(GeneralMethods.getOrthogonalVector(direction, rotation + 180 * j, helixRadius));
				playFirebendingParticles(helixLoc, helixParticleAmount, helixParticleSpread, helixParticleSpread, helixParticleSpread);
			}
			rotation += 10;
			
			if (ThreadLocalRandom.current().nextInt(6) == 0)
				playFirebendingSound(location);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
				if (e != player) {
					explode();
					remove();
					return;
				}
			}
		}
	}
	
	private void explode() {
		ParticleEffect.EXPLOSION_HUGE.display(location, 1);
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
		
		for (Block b : GeneralMethods.getBlocksAroundPoint(location, explosionRadius)) {
			if (!GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation()))
				createTempFire(b.getLocation());
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, explosionRadius)) {
			double knockback = 1 / (0.1 + e.getLocation().distance(location));
			e.setVelocity(GeneralMethods.getDirection(location, e.getLocation().add(0, 1, 0)).multiply(knockback));
			if (e instanceof LivingEntity) {
				DamageHandler.damageEntity(e, damage, this);
				new HorizontalVelocityTracker(e, player, 200, this);
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireStreams.Enabled");
	}

}
