package me.aztl.azutoru.ability.chi.passive;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TimeUtil;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.util.PlayerUtil;

public class Dodge extends ChiAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("HorizontalPush")
	private double horizontal;
	@Attribute("VerticalPush")
	private double vertical;
	
	private boolean chi, air, fire, earth, water, dodged;
	
	public Dodge(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			long cd = bPlayer.getCooldown(getName()) - System.currentTimeMillis();
			ActionBar.sendActionBar(ChatColor.GOLD + "Dodge - " + TimeUtil.formatTime(cd), player);
			return;
		}
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Multi-Elemental.Dodge.Cooldown");
		horizontal = c.getDouble("Abilities.Multi-Elemental.Dodge.HorizontalModifier");
		vertical = c.getDouble("Abilities.Multi-Elemental.Dodge.VerticalModifier");
		chi = c.getBoolean("Abilities.Multi-Elemental.Dodge.Chi");
		air = c.getBoolean("Abilities.Multi-Elemental.Dodge.Air");
		fire = c.getBoolean("Abilities.Multi-Elemental.Dodge.Fire");
		earth = c.getBoolean("Abilities.Multi-Elemental.Dodge.Earth");
		water = c.getBoolean("Abilities.Multi-Elemental.Dodge.Water");
		
		dodged = false;
		
		if (canDodge()) {
			start();
		}
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (!dodged) {
			Location eyeLoc = player.getEyeLocation();
			Location eyeTarget = GeneralMethods.getTargetedLocation(player, 2);
			Vector direction = GeneralMethods.getDirection(eyeTarget, eyeLoc);
			direction.setX(direction.getX() * horizontal);
			direction.setY(direction.getY() * vertical);
			direction.setZ(direction.getZ() * horizontal);
			player.setVelocity(direction);
			
			ParticleEffect.CLOUD.display(eyeLoc, 10, FastMath.random(), 0.2, FastMath.random());
			dodged = true;
			bPlayer.addCooldown(this);
			return;
		}
		player.setFallDistance(0);
		if (PlayerUtil.isOnGround(player) || player.getLocation().getBlock().isLiquid()) {
			remove();
			return;
		}
	}
	
	public boolean canDodge() {
		if (PlayerUtil.isOnGround(player)
				|| player.getLocation().getBlock().isLiquid()
				|| !player.isSneaking())
			return false;
		
		if ((chi && bPlayer.isElementToggled(Element.CHI))
				|| (air && bPlayer.isElementToggled(Element.AIR))
				|| (fire && bPlayer.isElementToggled(Element.FIRE))
				|| (water && bPlayer.isElementToggled(Element.WATER))
				|| (earth && bPlayer.isElementToggled(Element.EARTH)))
			return true;
		
		return false;
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
		return "Dodge";
	}
	
	@Override
	public String getDescription() {
		return "This passive allows a bender or chiblocker to quickly dodge an incoming attack. You do not need to bind this move. It will work on any slot.";
	}
	
	@Override
	public String getInstructions() {
		return "To use, you must be off the ground. Then, hold sneak and right-click (on any slot) on a block in the direction opposite from where you want to go.";
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return true;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Multi-Elemental.Dodge.Enabled");
	}

}
