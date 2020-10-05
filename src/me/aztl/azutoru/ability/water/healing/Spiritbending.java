package me.aztl.azutoru.ability.water.healing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Spiritbending extends HealingAbility implements AddonAbility {

	public static enum State {
		TOWARD_TARGET, ENCIRCLE, PACIFY, CORRUPT;
	}
	
	private long cooldown, duration;
	private double range, grabRadius, sourceRange;
	
	private Entity target;
	private Block sourceBlock;
	private State state;
	private Location location;
	@SuppressWarnings("unused")
	private DustOptions color;
	
	public Spiritbending(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		state = State.TOWARD_TARGET;
		
		if (prepare()) {
			start();
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		switch (state) {
		case TOWARD_TARGET:
			if (GeneralMethods.isSolid(location.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				remove();
				return;
			}
			
			for (int i = 0; i < 3; i++) {
				location.add(GeneralMethods.getDirection(location, target.getLocation()));
				ParticleEffect.WATER_SPLASH.display(location, 1);
			}
			
			if (target.getLocation().distance(location) < 2) {
				state = State.ENCIRCLE;
			}
			
			break;
		case ENCIRCLE:
			
			color = new DustOptions(Color.fromRGB(17, 98, 171), 1);
			break;
		case PACIFY:
			
			color = new DustOptions(Color.fromRGB(242, 235, 69), 1);
			break;
		case CORRUPT:
			
			color = new DustOptions(Color.fromRGB(153, 69, 242), 1);
			break;
		default:
			break;
		}
	}
	
	public boolean prepare() {
		boolean targetSelected = false;
		for (int i = 0; i < range; i++) {
			Location targetedLocation = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(targetedLocation, grabRadius)) {
				if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
					target = e;
					break;
				}
			}
			if (target != null) {
				targetSelected = true;
				break;
			}
		}
		
		if (targetSelected) {			
			for (Block b : GeneralMethods.getBlocksAroundPoint(target.getLocation(), sourceRange)) {
				if (isWater(b) && GeneralMethods.isTransparent(b.getRelative(BlockFace.UP))) {
					sourceBlock = b;
					location = sourceBlock.getLocation();
					break;
				}
			}
			
			if (sourceBlock != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public void displayWaterParticle(Location loc) {
		
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "Spiritbending";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
	}

	@Override
	public String getAuthor() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}

}
