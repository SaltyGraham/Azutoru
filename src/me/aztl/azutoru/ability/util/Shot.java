package me.aztl.azutoru.ability.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.IceAbility;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RangePolicy;
import me.aztl.azutoru.policy.RemovalPolicy;

/**
 * A Shot is a singular shot that other abilities can fire.
 * It is used for multi-shot abilities such as IceShots
 * and needs several parameters to function.
 */
public abstract class Shot extends ElementalAbility implements AddonAbility {

	protected double damage, range, hitRadius, speed;
	
	protected Location location, origin;
	protected Vector direction;
	protected Ability ability;
	protected RemovalPolicy policy;
	protected boolean controllable;
	
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
			
			progressShot();
		}
	}
	
	protected abstract void progressShot();
	
	protected boolean canPlaceWaterBlock(Block b) {
		return isWater(b) || isIce(b) || isAir(b.getType());
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
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
	public String getAuthor() {
		return Azutoru.az.dev();
	}
	
	@Override
	public String getVersion() {
		return Azutoru.az.version();
	}
	
	@Override
	public void load() {}
	
	@Override
	public void stop() {}

}
