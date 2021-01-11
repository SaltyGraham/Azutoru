package me.aztl.azutoru.ability.earth.glass;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.util.Shot;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.GlassAbility;

public class GlassShards extends GlassAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	private int remaining;

	private Location location;
	private Vector direction;
	private RemovalPolicy policy;
	private Material glassType;
	private long lastShotTime, timeBetweenShots;
	
	public GlassShards(Player player, boolean rightClick) {
		super(player);
		
		if (!bPlayer.canBend(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.GlassShards.Cooldown");
		sourceRange = c.getDouble("Abilities.Earth.GlassShards.SourceRange");
		remaining = c.getInt("Abilities.Earth.GlassShards.MaxShards");
		duration = c.getLong("Abilities.Earth.GlassShards.Duration");
		damage = c.getDouble("Abilities.Earth.GlassShards.Damage");
		speed = c.getDouble("Abilities.Earth.GlassShards.Speed");
		range = c.getDouble("Abilities.Earth.GlassShards.Range");
		hitRadius = c.getDouble("Abilities.Earth.GlassShards.HitRadius");
		timeBetweenShots = c.getLong("Abilities.Earth.GlassShards.ShotCooldown");
		
		policy = Policies.builder()
					.add(new ExpirationPolicy(duration))
					.add(new UsedAmmoPolicy(() -> remaining, p -> !hasAbility(p, GlassShot.class))).build();
		
		double glassCrackRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.GlassShards.GlassCrackRadius");
		
		Block sourceBlock = player.getTargetBlock(null, (int) sourceRange);
		
		if (sourceBlock == null || !isGlass(sourceBlock)) return;
		
		if (rightClick) {
			for (Block b : GeneralMethods.getBlocksAroundPoint(sourceBlock.getLocation(), glassCrackRadius)) {
				if (isGlass(b)) {
					ParticleEffect.BLOCK_DUST.display(b.getLocation(), 3, FastMath.random(), FastMath.random(), FastMath.random(), b.getType().createBlockData());
					if (isEarthRevertOn()) {
						addTempAirBlock(b);
					} else {
						b.breakNaturally();
					}
				}
			}
			playGlassbendingSound(sourceBlock.getLocation());
			bPlayer.addCooldown(this);
		} else {
			glassType = sourceBlock.getType();
			
			if (isEarthRevertOn()) {
				addTempAirBlock(sourceBlock);
			} else {
				sourceBlock.breakNaturally();
			}
			
			start();
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			bPlayer.addCooldown(this);
			return;
		}
		
		location = player.getEyeLocation();
		direction = location.getDirection();
		Location right = GeneralMethods.getRightSide(player.getLocation().add(0, 1, 0), 1.5);
		Location left = GeneralMethods.getLeftSide(player.getLocation().add(0, 1, 0), 1.5);
		
		displayRing(right, left);
		displayRing(left, right);
			
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playGlassbendingSound(location);
	}
	
	public void displayRing(Location side, Location other) {
		Location loc = player.getLocation().add(0, 1, 0);
		double radius = 1.5;
		for (double a = 0; a <= FastMath.PI * 2; a += FastMath.PI / 8) {
			double x = FastMath.cos(a) * radius;
			double z = FastMath.sin(a) * radius;
			loc.add(x, 0, z);
			double y = -loc.add(0, 1, 0).distance(side) + 1;
			loc.add(0, y, 0);
			ParticleEffect.BLOCK_DUST.display(loc, 1, 0, 0, 0, 1, glassType.createBlockData());
			loc.subtract(x, 0, z);
			double y2 = -loc.add(0, 1, 0).distance(other) + 1;
			loc.subtract(0, y2, 0);
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1)) {
				if (e instanceof LivingEntity && e != player) {
					DamageHandler.damageEntity(e, 1, this);
				}
			}
		}
	}
	
	public void onClick() {
		if (System.currentTimeMillis() >= lastShotTime + timeBetweenShots && remaining > 0) {
			new GlassShot(player, this, location, direction, damage, range, hitRadius, speed, false);
			remaining--;
			lastShotTime = System.currentTimeMillis();
		}
	}
	
	private class GlassShot extends Shot {

		public GlassShot(Player player, Ability ability, Location origin, Vector direction, double damage, double range,
				double hitRadius, double speed, boolean controllable) {
			super(player, ability, origin, direction, damage, range, hitRadius, speed, controllable);
		}
		
		@Override
		protected void progressShot() {
			Material glassType = getAbility(player, GlassShards.class).getGlassType();
			ParticleEffect.BLOCK_DUST.display(location, 4, 0.1, 0.1, 0.1, 5, glassType.createBlockData());
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
				if (e instanceof LivingEntity && e != player) {
					DamageHandler.damageEntity(e, damage, ability);
				}
			}
			
			if (ThreadLocalRandom.current().nextInt(6) == 0)
				GlassAbility.playGlassbendingSound(location);
		}
		
		@Override
		public String getName() {
			return "GlassShards";
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
			return false;
		}
		
	}
	
	public Material getGlassType() {
		return glassType;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	public long getTimeBetweenShots() {
		return timeBetweenShots;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation().add(0, 1, 0);
	}

	@Override
	public String getName() {
		return "GlassShards";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a skilled sandbender to bend shards of glass.";
	}
	
	@Override
	public String getInstructions() {
		return "Tap sneak on a glass block and shards of glass will begin to spin around you. Carry them with you and left-click to shoot them one by one at your target. You can also right-click on a glass block to break it and surrounding glass blocks.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.GlassShards.Enabled");
	}

}
