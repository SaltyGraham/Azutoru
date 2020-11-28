package me.aztl.azutoru.ability.earth.sand.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.ability.earth.sand.DustDevil;

public class DustStepping extends SandAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double horizontal, vertical;
	private int maxSteps, maxDistance;
	
	private Block topBlock;
	
	public DustStepping(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (hasAbility(player, DustDevil.class)) {
			getAbility(player, DustDevil.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.DustStepping.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.DustStepping.Duration");
		horizontal = Azutoru.az.getConfig().getDouble("Abilities.Earth.DustStepping.HorizontalPush");
		vertical = Azutoru.az.getConfig().getDouble("Abilities.Earth.DustStepping.VerticalPush");
		maxSteps = Azutoru.az.getConfig().getInt("Abilities.Earth.DustStepping.MaxSteps");
		maxDistance = Azutoru.az.getConfig().getInt("Abilities.Earth.DustStepping.MaxDistanceFromGround");
		
		applyModifiers();
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), maxDistance);
		if (isEarth(topBlock)) {
			start();
			step();
		}
	}
	
	private void applyModifiers() {
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			horizontal *= 2;
			vertical *= 2;
			maxSteps += 10;
			maxDistance += 20;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (maxSteps == 0) {
			remove();
			return;
		}
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), maxDistance);
		if (!isEarth(topBlock)) {
			remove();
			return;
		}
	}
	
	private void playStepAnimation() {
		Location ground = topBlock.getLocation();
		Location playerLoc = player.getLocation();
		for (Location loc : AzutoruMethods.getLinePoints(player, ground, playerLoc, (int) ground.distance(playerLoc) * 2)) {
			ParticleEffect.BLOCK_DUST.display(loc, 1, 0.2, 0.2, 0.2, 1, topBlock.getType().createBlockData());
		}
	}
	
	public void step() {
		Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();
		direction.setX(direction.getX() * horizontal);
		direction.setY(vertical);
		direction.setZ(direction.getZ() * horizontal);
		
		player.setVelocity(direction);
		
		playStepAnimation();
		
		maxSteps--;
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
		return "DustStepping";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a sandbender to create dust columns that push the bender's feet upwards, acting as steps."
				+ "It can be used as long as the user is not too far above an earthbendable block.";
	}
	
	@Override
	public String getInstructions() {
		return "(Activation) DustDevil (Tap sneak) > DustDevil (Tap sneak) > EarthBlast (Left-click)"
				+ "\nRepeatedly left-click with EarthBlast to keep stepping.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new DustStepping(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("EarthBlast", ClickType.LEFT_CLICK));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustStepping.Enabled")
				&& Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustDevil.Enabled");
	}

}
