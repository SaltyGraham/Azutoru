package me.aztl.azutoru.ability.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.glass.GlassShards;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RangePolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.util.GlassAbility;

/**
 * A Shot is a singular shot that other abilities can fire.
 * It is used for multi-shot abilities such as IceShots
 * and needs several parameters to function.
 */
public class Shot extends ElementalAbility implements AddonAbility {

	private double damage, range, hitRadius, speed;
	
	private Location location, origin;
	private Vector direction;
	private Ability ability;
	private RemovalPolicy policy;
	private boolean controllable;
	private double counter;
	
	public Shot(Player player, Ability ability, Location origin, Vector direction,
			double damage, double range, double hitRadius, double speed, boolean controllable) {
		
		super(player);
		
		this.origin = origin;
		this.direction = direction;
		this.damage = damage;
		this.range = range;
		this.hitRadius = hitRadius;
		this.speed = speed;
		this.ability = ability;
		this.controllable = controllable;
		
		location = origin.clone();
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ProtectedRegionPolicy(this, () -> location))
					.add(new RangePolicy(this.range, this.origin, () -> location))
					.build();
		
		start();
	}

	@Override
	public void progress() {
		if (bPlayer.canBendIgnoreCooldowns(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock())
				&& !(ability instanceof IceAbility && isIce(location.getBlock()))) {
			remove();
			return;
		}
		
		if (controllable) {
			direction = player.getEyeLocation().getDirection();
		}
		
		for (int i = 0; i < 4; i++) {
			location = location.add(direction.clone().multiply(speed / 4));
			
			if (ability instanceof IceAbility) {
				progressIce();
			} else if (ability instanceof GlassAbility) {
				progressGlass();
			}
		}
		
		counter++;
	}
	
	public void progressIce() {
		ParticleEffect.SNOW_SHOVEL.display(location, 5, Math.random(), Math.random(), Math.random(), 0.05);
		new TempBlock(location.getBlock(), Material.ICE).setRevertTime(10);
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
			if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
				DamageHandler.damageEntity(e, damage, ability);
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 2);
				new TempPotionEffect((LivingEntity) e, effect);
				remove();
				return;
			}
		}
		
		if (!canPlaceWaterBlock(location.getBlock())) {
			remove();
			return;
		}
		
		if (counter % 6 == 0) {
			IceAbility.playIcebendingSound(location);
		}
	}
	
	public void progressGlass() {
		Material glassType = getAbility(player, GlassShards.class).getGlassType();
		
		ParticleEffect.BLOCK_DUST.display(location, 4, 0.1, 0.1, 0.1, 5, glassType.createBlockData());
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
			if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
				DamageHandler.damageEntity(entity, damage, ability);
			}
		}
		
		if (counter % 6 == 0) {
			GlassAbility.playGlassbendingSound(location);
		}
	}
	
	private boolean canPlaceWaterBlock(Block b) {
		return isWater(b) || isIce(b) || isAir(b.getType());
	}

	@Override
	public boolean isSneakAbility() {
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
	public boolean isExplosiveAbility() {
		return false;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "Shot";
	}

	@Override
	public Element getElement() {
		return Element.AVATAR;
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

}
