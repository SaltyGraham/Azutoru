package me.aztl.azutoru.ability.fire.lightning;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LightningAbility;

import me.aztl.azutoru.Azutoru;

public class Electrify extends LightningAbility implements AddonAbility {

	private long cooldown, duration;
	@SuppressWarnings("unused")
	private double damage, range, hitRadius, speed;
	
	private World world;
	private Location location;
	private Vector direction;
	
	public Electrify(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.Electrify.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.Electrify.Duration");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.Electrify.Damage");
		range = Azutoru.az.getConfig().getDouble("Abilities.Fire.Electrify.Range");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.Electrify.HitRadius");
		
		world = player.getWorld();
		location = player.getEyeLocation();
		direction = location.getDirection();
		
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(location.getBlock()) && !isMetal(location.getBlock())) {
			remove();
			return;
		} else if (isMetal(location.getBlock()) || isWater(location.getBlock())) {
			progressArc();
		} else {
			for (int i = 0; i < 3; i++) {
				location.add(direction.multiply(speed / 3));
				playLightningbendingParticle(location, 0.3, 0.3, 0.3);
			}
		}
	}
	
	public void progressArc() {
		
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
		return "Electrify";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a lightningbender to quickly generate a short pulse of electricity, "
				+ "which can be used to harm and stun entities. It can also generate redstone power "
				+ "and be placed in/on conductors such as metal and water.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click to aim your pulse of electricity.";
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
		return false;
	}

}
