package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
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

import me.aztl.azutoru.Azutoru;

public class AirCushion extends AirAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double range, radius, speed;
	
	private Location location, origin, center;
	private Vector direction;
	private Block topBlock;
	
	public AirCushion(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.AirCushion.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.AirCushion.Duration");
		range = Azutoru.az.getConfig().getDouble("Abilities.Air.AirCushion.Range");
		radius = Azutoru.az.getConfig().getDouble("Abilities.Air.AirCushion.Radius");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Air.AirCushion.Speed");
		
		origin = player.getEyeLocation();
		location = origin.clone();
		direction = location.getDirection().multiply(speed);
		
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		if (location.distanceSquared(origin) > range * range) {
			if (topBlock == null) {
				topBlock = GeneralMethods.getTopBlock(location, 15);
			} else {
				location = topBlock.getLocation();
				direction.multiply(0);
				formCushion(location);
			}
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		for (int i = 0; i < 5; i++) {
			if (GeneralMethods.isSolid(location.getBlock())) {
				formCushion(location);
				break;
			} else {
				location.add(direction);
				getAirbendingParticles().display(location, 3, Math.random(), 0.2, Math.random());
			}
		}
	}
	
	public void formCushion(Location location) {
		if (isTransparent(location.getBlock())) {
			center = location.clone();
		} else {
			for (int i = 0; i < 15; i++) {
				if (isTransparent(location.getBlock().getRelative(BlockFace.UP, i))) {
					center = location.getBlock().getRelative(BlockFace.UP, i).getLocation();
					break;
				}
			}
		}
		for (Block b : GeneralMethods.getBlocksAroundPoint(center, radius)) {
			if (GeneralMethods.isSolid(b.getRelative(BlockFace.DOWN))) {
				getAirbendingParticles().display(b.getLocation(), 1, Math.random(), 0.2, Math.random());
			}
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(b.getLocation(), 1)) {
				if (e instanceof LivingEntity) {
					e.setFallDistance(0);
				}
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
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
		return "AirCushion";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an airbender to prevent others' fall damage by placing a protective ring of air on the ground."
				+ "It is useful for teamwork with benders who don't have easy ways to prevent fall damage.";
	}
	
	@Override
	public String getInstructions() {
		return "AirShield (Left-click three times while looking towards a good ground destination for your air cushion)";
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
		return new AirCushion(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("AirShield", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("AirShield", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("AirShield", ClickType.LEFT_CLICK));
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
