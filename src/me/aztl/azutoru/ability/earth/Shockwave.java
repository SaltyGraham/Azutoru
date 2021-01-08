package me.aztl.azutoru.ability.earth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.util.Line;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SneakingPolicy;
import me.aztl.azutoru.policy.SneakingPolicy.ProhibitedState;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;

public class Shockwave extends EarthAbility implements AddonAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double arcRange;
	@Attribute(Attribute.RANGE)
	private double ringRange;
	@Attribute("FallThreshold")
	private float fallThreshold;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.SPEED)
	private double arcSpeed;
	@Attribute(Attribute.SPEED)
	private double ringSpeed;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	
	private List<Ripple> ripples = new ArrayList<>();
	private Set<Block> affectedBlocks = new HashSet<>();
	private Set<Entity> affectedEntities = new HashSet<>();
	private RemovalPolicy policy;
	private boolean released;

	public Shockwave(Player player, boolean fall) {
		super(player);
		
		if (!bPlayer.canBend(this) || hasAbility(player, Shockwave.class)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.Shockwave.Cooldown");
		arcRange = c.getDouble("Abilities.Earth.Shockwave.Arc.Range");
		arcSpeed = c.getDouble("Abilities.Earth.Shockwave.Arc.Speed");
		ringRange = c.getDouble("Abilities.Earth.Shockwave.Ring.Range");
		ringSpeed = c.getDouble("Abilities.Earth.Shockwave.Ring.Speed");
		fallThreshold = c.getInt("Abilities.Earth.Shockwave.FallThreshold");
		chargeTime = c.getLong("Abilities.Earth.Shockwave.ChargeTime");
		damage = c.getDouble("Abilities.Earth.Shockwave.Damage");
		knockback = c.getDouble("Abilities.Earth.Shockwave.Knockback");
		knockup = c.getDouble("Abilities.Earth.Shockwave.Knockup");
		hitRadius = c.getDouble("Abilities.Earth.Shockwave.HitRadius");
		
		policy = Policies.builder()
					.add(new SneakingPolicy(ProhibitedState.NOT_SNEAKING, p -> !isCharged()))
					.add(new UsedAmmoPolicy(() -> ripples.size(), p -> released)).build();
		
		if (fall) {
			if (player.getFallDistance() < fallThreshold) return;
			release(false);
			return;
		}
		
		start();
	}

	@Override
	public void progress() {
		if (!released) {
			if (isCharged()) {
				ParticleEffect.SMOKE_NORMAL.display(player.getEyeLocation().add(player.getEyeLocation().getDirection()), 1);
				if (!player.isSneaking()) {
					release(false);
				}
			}
			return;
		}
		
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			return;
		}
	}
	
	public boolean isCharged() {
		return System.currentTimeMillis() > getStartTime() + chargeTime;
	}
	
	public void onClick() {
		if (!released && isCharged())
			release(true);
	}
	
	private void release(boolean arc) {
		if (!PlayerUtil.isOnGround(player)) return;
		released = true;
		double range = arc ? arcRange : ringRange;
		double speed = arc ? arcSpeed : ringSpeed;
		double angle = FastMath.PI / (3 * range);
		Location loc = player.getLocation().add(0, 0.5, 0);
		Vector3D dir3d = MathUtil.toVector3D(loc.getDirection().setY(0).normalize());
		Rotation rotation = new Rotation(Vector3D.PLUS_J, angle, RotationConvention.VECTOR_OPERATOR);
		
		if (arc) {
			MathUtil.createArc(dir3d, rotation, NumberConversions.ceil(range / 2))
				.forEach(v -> ripples.add(new Ripple(player, loc, MathUtil.fromVector3D(v), speed, range)));
		} else {
			MathUtil.rotate(dir3d, rotation, NumberConversions.ceil(range * 6))
				.forEach(v -> ripples.add(new Ripple(player, loc, MathUtil.fromVector3D(v), speed, range)));
		}
	}
	
	private class Ripple extends Line {

		public Ripple(Player player, Location origin, Vector direction, double speed, double range) {
			super(player, origin, direction, speed, range);
		}

		@Override
		protected boolean isValidBlock(Block block) {
			return isEarth(block);
		}

		@Override
		protected void render(Block block) {
			if (affectedBlocks.contains(block)) return;
			affectedBlocks.add(block);
			double y = FastMath.min(0.35, 0.1 + location.distance(origin) / (1.5 * range));
			Vector velocity = new Vector(0, y, 0);
			FallingBlock fb = GeneralMethods.spawnFallingBlock(block.getRelative(BlockFace.UP).getLocation(), block.getType(), block.getBlockData());
			fb.setVelocity(velocity);
			fb.setMetadata("Crumble", new FixedMetadataValue(Azutoru.az, 0));
			fb.setHurtEntities(false);
			fb.setDropItem(false);
			if (ThreadLocalRandom.current().nextInt(6) == 0)
				playEarthbendingSound(block.getLocation());
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(block.getLocation(), hitRadius)) {
				if (affectedEntities.contains(e) || e == player) continue;
				affectedEntities.add(e);
				Vector travelVec = GeneralMethods.getDirection(location, e.getLocation()).normalize();
				travelVec.setY(travelVec.getY() * knockup).multiply(knockback);
				e.setVelocity(travelVec);
				if (e instanceof LivingEntity) {
					DamageHandler.damageEntity(e, damage, getAbility(player, Shockwave.class));
				}
			}
		}
		
		@Override
		public void remove() {
			super.remove();
			ripples.remove(this);
		}

		@Override
		public long getCooldown() {
			return 0;
		}

		@Override
		public boolean isExplosiveAbility() {
			return false;
		}

		@Override
		public boolean isHarmlessAbility() {
			return false;
		}

		@Override
		public boolean isIgniteAbility() {
			return false;
		}

		@Override
		public boolean isSneakAbility() {
			return true;
		}
		
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "Shockwave";
	}
	
	@Override
	public String getDescription() {
		return ConfigManager.languageConfig.get().getString("Abilities.Earth.Shockwave.Description");
	}
	
	@Override
	public String getInstructions() {
		return ConfigManager.languageConfig.get().getString("Abilities.Earth.Shockwave.Instructions");
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.Shockwave.Enabled");
	}

}
