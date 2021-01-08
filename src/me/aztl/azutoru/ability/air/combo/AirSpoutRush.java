package me.aztl.azutoru.ability.air.combo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.util.PlayerUtil;

public class AirSpoutRush extends AirAbility implements AddonAbility, ComboAbility {

	private static final Integer[] DIRECTIONS = { 0, 1, 2, 3, 5, 6, 7, 8 };
	
	private int angle;
	private long animTime;
	private long interval;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.HEIGHT)
	private double height;
	private float initFlySpeed;
	@Attribute(Attribute.SPEED)
	private float speedModifier;
	
	private List<Location> locations;
	private boolean canFly;
	
	public AirSpoutRush(Player player) {
		super(player);
		
		if (hasAbility(player, AirSpout.class))
			getAbility(player, AirSpout.class).remove();
		else return;
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		angle = 0;
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Air.AirSpoutRush.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Air.AirSpoutRush.Duration");
		animTime = System.currentTimeMillis();
		interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Air.AirSpout.Interval");
		height = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.AirSpout.Height");
		initFlySpeed = player.getFlySpeed();
		speedModifier = 2;
		
		locations = new ArrayList<>();
		canFly = player.getAllowFlight();
		
		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			return;
		}
		
		flightHandler.createInstance(player, getName());
		
		if (bPlayer.isAvatarState()) {
			height = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSpout.Height");
			cooldown = 0;
			duration = 0;
		}
		
		start();
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		} else if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		Block eyeBlock = player.getEyeLocation().getBlock();
		if (ElementalAbility.isWater(eyeBlock) || GeneralMethods.isSolid(eyeBlock)) {
			remove();
			return;
		}
		
		player.setFallDistance(0);
		player.setFlySpeed(initFlySpeed * speedModifier);
		player.getVelocity().setY(0.001);
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		if (ThreadLocalRandom.current().nextInt(4) == 0)
			playAirbendingSound(player.getLocation());
		
		Block block = getGround();
		if (block != null) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				PlayerUtil.removeFlight(player, canFly, false);
			} else {
				PlayerUtil.allowFlight(player);
			}
			rotateAirColumn(block);
		} else {
			remove();
		}
	}
	
	private void rotateAirColumn(Block block) {
		if (!player.getWorld().equals(block.getWorld())) {
			return;
		}
		if (System.currentTimeMillis() >= animTime + interval) {
			animTime = System.currentTimeMillis();
			Location location = block.getLocation();
			Location playerLoc = player.getLocation();
			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());
			
			int index = angle;
			double dy = Math.min(playerLoc.getY() - block.getY(), height);
			angle = angle >= DIRECTIONS.length ? 0 : angle + 1;
			
			locations.clear();
			for (int i = 1; i <= dy; i++) {
				index = index >= DIRECTIONS.length ? 0 : index + 1;
				Location effectLoc2 = new Location(location.getWorld(),location.getX(), block.getY() + i, location.getZ());
				playAirbendingParticles(effectLoc2, 3, 0.4F, 0.4F, 0.4F);
				locations.add(effectLoc2);
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		flightHandler.removeInstance(player, getName());
		player.setFlySpeed(initFlySpeed);
		PlayerUtil.removeFlight(player, canFly, false);
		new AirSpout(player);
	}

	private boolean isWithinMaxSpoutHeight(double threshold) {
		Block ground = getGround();
		if (ground == null) {
			return false;
		}
		double playerHeight = player.getLocation().getY();
		if (playerHeight > ground.getLocation().getY() + height + threshold) {
			return false;
		}
		return true;
	}
	
	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || ElementalAbility.isWater(block)) {
				return block;
			}
		}
		return null;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}
	
	@Override
	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public String getName() {
		return "AirSpoutRush";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an airbender to accelerate their AirSpout and move much faster than normal for a brief period of time.";
	}
	
	@Override
	public String getInstructions() {
		return "AirSpout (Tap sneak) > AirSpout (Tap sneak) > AirBurst (Left-click)";
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
		return new AirSpoutRush(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("AirSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirBurst", ClickType.LEFT_CLICK));
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
		boolean enabled = Azutoru.az.getConfig().getBoolean("Abilities.Air.AirSpoutRush.Enabled")
				&& ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.AirSpout.Enabled");
		return enabled;
	}

}
