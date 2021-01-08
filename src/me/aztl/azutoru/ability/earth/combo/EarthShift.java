package me.aztl.azutoru.ability.earth.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;

public class EarthShift extends EarthAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;
	
	private Location target;
	
	public EarthShift(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.EarthShift.Cooldown");
		range = c.getDouble("Abilities.Earth.EarthShift.Range") - 6;
		speed = c.getDouble("Abilities.Earth.EarthShift.Speed");
		
		target = player.getTargetBlock(null, (int) range).getLocation();
		if (!isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))
				|| !isEarthbendable(target.getBlock()))
			return;
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(target, 6)) {
			Block b = e.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (isEarthbendable(b) && e != player) {
				if (e instanceof LivingEntity)
					new HorizontalVelocityTracker(e, player, 200, this);
				Vector direction = GeneralMethods.getDirection(target, e.getLocation()).multiply(speed).setY(0);
				e.setVelocity(direction);
				remove();
				return;
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		return;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return target;
	}

	@Override
	public String getName() {
		return "EarthShift";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an earthbender to shift the earth beneath someone's feet, displacing them and putting them off balance.";
	}
	
	@Override
	public String getInstructions() {
		return "EarthBlast (Hold sneak) > Shockwave (Release sneak)";
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
	public Object createNewComboInstance(Player player) {
		return new EarthShift(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("EarthBlast", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Shockwave", ClickType.SHIFT_UP));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.EarthShift.Enabled");
	}

}
