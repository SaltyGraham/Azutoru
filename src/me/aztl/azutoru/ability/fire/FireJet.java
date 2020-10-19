package me.aztl.azutoru.ability.fire;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TimeUtil;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;
import me.aztl.azutoru.ability.fire.combo.JetBlast;
import me.aztl.azutoru.ability.fire.combo.JetBlaze;
import me.aztl.azutoru.ability.fire.combo.JetStepping;

public class FireJet extends FireAbility implements AddonAbility {

	public static enum JetState {
		PROPELLING, // Standard propulsion, looking where you go
		SKIING, // Skiing along the ground with a limited turning speed
		HOVERING; // Hovering midair
	}
	
	public static enum JetCombo {
		NONE, // Not using a combo
		BLAST, // Using JetBlast
		BLAZE; // Using JetBlaze
	}
	
	private long cooldown, duration, time;
	private double propelSpeed;
	private long propelDuration;
	private double skiSpeed, skiTurningSpeed;
	private long skiDuration;
	private double hoverSpeed, driftSpeed;
	private long hoverDuration, recoveryDuration, recoveryCooldown;
	private boolean skiEnabled, hoverEnabled, driftEnabled, recoveryEnabled;
	private int particleAmount;
	private double particleSpread, length;
	private double onSlotModifier;
	private double health, damageThreshold;
	private double blastSpeedMod, blazeSpeedMod;
	private double blazeHitRadius, blazeDamage;
	private int blazeFireTicks;
	private float yaw, pitch, initFlySpeed;
	
	private Location location, origin, right, left;
	private Vector direction;
	private int counter = 0;
	private World world;
	private boolean avatarState, isOnSlot, recovery;
	private JetState state;
	private JetCombo combo;
	private ArrayList<Entity> affectedEntities;
	private BossBar topBar, bottomBar;
	
	public FireJet(Player player, ClickType clickType) {
		super(player);
		
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			return;
		}
		
		if (hasAbility(player, JetStepping.class)) {
			return;
		}
		
		if (hasAbility(player, AirSpout.class)) {
			getAbility(player, AirSpout.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Duration");
		particleAmount = Azutoru.az.getConfig().getInt("Abilities.Fire.FireJet.ParticleAmount");
		particleSpread = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.ParticleSpread");
		length = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.ParticleLength");
		onSlotModifier = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.OnSlotModifier");
		damageThreshold = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.DamageThreshold");
		propelSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.Propel.Speed");
		propelDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Propel.Duration");
		skiEnabled = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Ski.Enabled");
		skiSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.Ski.Speed");
		skiDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Ski.Duration");
		skiTurningSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.Ski.TurningSpeed");
		hoverEnabled = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Hover.Enabled");
		hoverSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.Hover.Speed");
		hoverDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Hover.Duration");
		recoveryEnabled = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Hover.Recovery.Enabled");
		recoveryDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Hover.Recovery.Duration");
		recoveryCooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireJet.Hover.Recovery.Cooldown");
		driftEnabled = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Hover.Drift.Enabled");
		driftSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireJet.Hover.Drift.Speed");
		
		blastSpeedMod = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetBlast.SpeedModifier");
		
		blazeSpeedMod = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetBlaze.SpeedModifier");
		blazeHitRadius = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetBlaze.ParticleHitRadius");
		blazeFireTicks = Azutoru.az.getConfig().getInt("Abilities.Fire.JetBlaze.FireTicks");
		blazeDamage = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetBlaze.Damage");
		
		applyModifiers();
		
		origin = player.getLocation();
		location = origin.clone();
		direction = player.getEyeLocation().getDirection().clone().normalize().multiply(propelSpeed);
		time = System.currentTimeMillis();
		health = player.getHealth();
		world = player.getWorld();
		affectedEntities = new ArrayList<>();
		initFlySpeed = player.getFlySpeed();
		
		Block b = origin.getBlock();
		if (b.isLiquid()) {
			return;
		}
		
		switch (clickType) {
		case LEFT_CLICK:
			if (bPlayer.isOnCooldown(this)) {
				return;
			}
			state = JetState.PROPELLING;
			break;
		case RIGHT_CLICK:
			if (bPlayer.isOnCooldown(this)) {
				return;
			}
			state = JetState.SKIING;
			break;
		case SHIFT_DOWN:
			if (AzutoruMethods.isOnGround(player)) {
				return;
			}
			if (bPlayer.isOnCooldown(this)) {
				if (!recoveryEnabled) {
					return;
				}
				if (bPlayer.isOnCooldown(getName() + "Recovery")) {
					long recoveryCd = bPlayer.getCooldown(getName() + "Recovery") - System.currentTimeMillis();
					ActionBar.sendActionBar(ChatColor.RED + "FireJet Recovery - " + TimeUtil.formatTime(recoveryCd), player);
					return;
				}
				state = JetState.HOVERING;
				duration = recoveryDuration;
				recovery = true;
			} else {
				state = JetState.HOVERING;
			}
			break;
		default:
			break;
		}
		combo = JetCombo.NONE;
		
		initializeBars();
		
		if (!recovery) {
			flightHandler.createInstance(player, getName());
			AzutoruMethods.allowFlight(player);
			player.setFlySpeed(0);
		}
		
		if (state == JetState.PROPELLING) {
			player.setVelocity(direction);
			if (isAir(b.getType())) {
				createTempFire(b.getLocation());
			}
		}
		start();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			propelSpeed *= BlueFireAbility.getRangeFactor();
			skiSpeed *= BlueFireAbility.getRangeFactor();
			driftSpeed *= BlueFireAbility.getRangeFactor();
			blazeDamage *= BlueFireAbility.getDamageFactor();
		}
		
		if (isDay(player.getWorld())) {
			propelSpeed = getDayFactor(propelSpeed);
			skiSpeed = getDayFactor(skiSpeed);
			driftSpeed = getDayFactor(driftSpeed);
			blazeDamage = getDayFactor(blazeDamage);
		}
		
		if (bPlayer.isAvatarState()) {
			avatarState = true;
			cooldown = 0;
			duration = 0;
			propelDuration = 0;
			skiDuration = 0;
			hoverDuration = 0;
			propelSpeed *= 1.25;
			skiSpeed *= 1.25;
			driftSpeed *= 1.25;
		}
	}
	
	private void initializeBars() {
		topBar = Bukkit.createBossBar(ChatColor.RED + "Duration", BarColor.RED, BarStyle.SOLID);
		topBar.addPlayer(player);
		
		bottomBar = Bukkit.createBossBar(ChatColor.DARK_PURPLE + "Propel Duration", BarColor.PURPLE, BarStyle.SOLID);
		bottomBar.addPlayer(player);
		
		updateBars();
	}
	
	private void updateBars() {
		if (duration <= 0) {
			topBar.setProgress(1);
		} else {
			double progress = (double) getTimeLeft() / (double) duration;
			if (progress < 0) {
				progress = 0;
			}
			topBar.setProgress(progress);
		}
		
		if (getModeDuration() <= 0) {
			bottomBar.setProgress(1);
		} else {
			double progress = (double) getModeTimeLeft() / (double) getModeDuration();
			if (progress < 0) {
				progress = 0;
			}
			bottomBar.setProgress(progress);
		}
		
		switch (state) {
		case PROPELLING:
			bottomBar.setTitle(ChatColor.DARK_PURPLE + "Propel Duration");
			bottomBar.setColor(BarColor.PURPLE);
			break;
		case SKIING:
			bottomBar.setTitle(ChatColor.LIGHT_PURPLE + "Ski Duration");
			bottomBar.setColor(BarColor.PINK);
			break;
		case HOVERING:
			bottomBar.setTitle(ChatColor.YELLOW + "Hover Duration");
			bottomBar.setColor(BarColor.YELLOW);
			break;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			removeWithCooldown();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			removeWithCooldown();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			removeWithCooldown();
			return;
		}
		
		if (!bPlayer.isAvatarState() && damageThreshold > 0 && player.getHealth() <= health - damageThreshold) {
			removeWithCooldown();
			return;
		}
		
		if (avatarState && !bPlayer.isAvatarState()) {
			removeWithCooldown();
			return;
		}
		
		if (player.getLocation().getBlock().isLiquid()) {
			removeWithCooldown();
			return;
		}
		
		if (bPlayer.getBoundAbilityName().equals(getName())) {
			isOnSlot = true;
		} else {
			isOnSlot = false;
		}
		
		if (hasAbility(player, JetBlast.class)) {
			combo = JetCombo.BLAST;
			state = JetState.PROPELLING;
		} else if (hasAbility(player, JetBlaze.class)) {
			combo = JetCombo.BLAZE;
			state = JetState.PROPELLING;
		} else {
			combo = JetCombo.NONE;
		}
		
		if (counter % 6 == 0) {
			playFirebendingSound(location);
		}
		counter++;
		
		updateBars();
		
		location = player.getLocation();
		right = AzutoruMethods.getHandPos(player, Hand.RIGHT);
		left = AzutoruMethods.getHandPos(player, Hand.LEFT);
		yaw = AzutoruMethods.getOppositeYaw(location.getYaw());
		pitch = -1 * location.getPitch();
		
		player.setFallDistance(0);
		
		switch (state) {
		case PROPELLING:
			propel();
			break;
		case SKIING:
			ski();
			break;
		case HOVERING:
			hover();
			break;
		default:
			break;
		}
	}
	
	public void propel() {
		if (propelDuration > 0 && System.currentTimeMillis() > time + propelDuration) {
			removeWithCooldown();
			return;
		}
		
		Location loc = location.clone();
		loc.setPitch(pitch);
		loc.setYaw(yaw);
		Vector dir = loc.getDirection();
		
		displayJet(loc, dir, length, 0.5);
		
		double timeFactor = getTimeFactor(propelDuration);
		double speed = propelSpeed;
		if (isOnSlot) {
			speed *= onSlotModifier;
			
			Location r = getJetAngles(right, 150, pitch);
			Location l = getJetAngles(left, -150, pitch);
			Vector rv = r.getDirection();
			Vector lv = l.getDirection();
			displayJets(r, l, rv, lv, length, 0.5);
		}
		if (combo == JetCombo.BLAST) {
			speed *= blastSpeedMod;
		} else if (combo == JetCombo.BLAZE) {
			speed *= blazeSpeedMod;
		}
		
		direction = player.getEyeLocation().getDirection().clone().normalize().multiply(speed * timeFactor);
		player.setVelocity(direction);
	}
	
	public void ski() {
		if (skiDuration > 0 && System.currentTimeMillis() > time + skiDuration) {
			removeWithCooldown();
			return;
		}
		
		double timeFactor = getTimeFactor(skiDuration);
		double speed = skiSpeed;
		if (isOnSlot) {
			speed = speed * onSlotModifier;
			
			Location r = getJetAngles(right, 150, pitch);
			Location l = getJetAngles(left, -150, pitch);
			Vector rv = r.getDirection().multiply(skiTurningSpeed);
			Vector lv = l.getDirection().multiply(skiTurningSpeed);
			displayJets(r, l, rv, lv, length, 0.5);
		}
		if (combo == JetCombo.BLAST) {
			speed *= blastSpeedMod;
		} else if (combo == JetCombo.BLAZE) {
			speed *= blazeSpeedMod;
		}
		
		Location loc = location.clone();
		
		Block topBlock = GeneralMethods.getTopBlock(location, 3);
		if (topBlock == null) {
			loc.setPitch(10);
		}
		
		direction.add(loc.getDirection().clone().normalize().multiply(skiTurningSpeed)).normalize().multiply(speed * timeFactor);
		
		if (topBlock != null) {
			double groundHeight = topBlock.getY() + 2.2;
			double playerHeight = location.getY();
			double displacement = groundHeight - playerHeight;
			
			direction.setY(displacement * 0.25);
		}
		
		player.setVelocity(direction);
		
		if (player.getVelocity().length() < skiSpeed * 0.2) {
			removeWithCooldown();
			return;
		}

		loc.setPitch(-15);
		loc.setYaw(yaw);
		Vector dir = loc.getDirection();
		
		displayJet(loc, dir, length, 0.5);
	}
	
	public void hover() {
		if (hoverDuration > 0 && System.currentTimeMillis() > time + hoverDuration) {
			removeWithCooldown();
			return;
		}
		
		if (AzutoruMethods.isOnGround(player)) {
			removeWithCooldown();
			return;
		}
		
		Location loc = location.clone();
		loc.setPitch(90);
		Vector dir = loc.getDirection();
		displayJet(loc, dir, 1, 0.25);
		
		if (isOnSlot) {
			Location r = right.clone();
			r.setPitch(90);
			Location l = left.clone();
			l.setPitch(90);
			Vector rv = r.getDirection();
			Vector lv = l.getDirection();
			displayJets(r, l, rv, lv, 1, 0.25);
		}
		
		if (!player.isSneaking() || (player.isSneaking() && !bPlayer.getBoundAbilityName().equals(getName()))) {
			player.setVelocity(player.getVelocity().clone().multiply(hoverSpeed));
			
		} else if (driftEnabled && !recovery && bPlayer.getBoundAbilityName().equals(getName())) {
			double timeFactor = getTimeFactor(hoverDuration);
			double speed = driftSpeed;
			
			if (isOnSlot) {
				speed *= onSlotModifier;
			}
			
			Location target = GeneralMethods.getTargetedLocation(player, 1);
			direction = GeneralMethods.getDirection(target, player.getEyeLocation()).multiply(speed * timeFactor);
			player.setVelocity(direction);
		}
	}
	
	public void displayJet(Location loc, Vector dir, double length, double stepSize) {
		for (double d = 0; d <= length; d += stepSize) {
			Block b = loc.getBlock();
			if (GeneralMethods.isSolid(b) || b.isLiquid()) {
				break;
			}
			
			particle(loc);
			
			loc.add(dir);
			
			if (combo == JetCombo.BLAZE) {
				damage(loc);
			}
		}
	}
	
	public void displayJets(Location r, Location l, Vector rv, Vector lv, double length, double stepSize) {
		for (double d = 0; d <= length; d += stepSize) {
			Block br = r.getBlock();
			if (GeneralMethods.isSolid(br) || br.isLiquid()) {
				continue;
			}
			
			Block bl = l.getBlock();
			if (GeneralMethods.isSolid(bl) || bl.isLiquid()) {
				continue;
			}
			
			particle(r);
			particle(l);
			
			r.add(rv);
			l.add(lv);
			
			if (combo == JetCombo.BLAZE) {
				damage(r);
				damage(l);
			}
		}
	}
	
	private void particle(Location loc) {
		playFirebendingParticles(loc, particleAmount, particleSpread, particleSpread, particleSpread);
	}
	
	private void damage(Location loc) {
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, blazeHitRadius)) {
			if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
				if (!affectedEntities.contains(e)) {
					DamageHandler.damageEntity(e, blazeDamage, getAbility(player, JetBlaze.class));
					e.setFireTicks(blazeFireTicks);
					new FireDamageTimer(e, player);
					affectedEntities.add(e);
				}
			}
		}
	}
	
	public Location getJetAngles(Location location, float yawDiff, float pitch) {
		Location loc = location.clone();
		float yaw = loc.getYaw() + yawDiff;
		if (yaw < 0) {
			yaw += 360;
		} else if (yaw > 360) {
			yaw -= 360;
		}
		
		loc.setYaw(yaw);
		loc.setPitch(pitch);
		
		return loc;
	}
	
	public double getTimeFactor(long currentDuration) {
		if (currentDuration <= 0) {
			return 1;
		}
		double timeFactor;
		if (bPlayer.isAvatarState()) {
			timeFactor = 1;
		} else {
			timeFactor = 1 - (System.currentTimeMillis() - time) / (1.5 * currentDuration);
		}
		return timeFactor;
	}
	
	private long getTimeLeft() {
		return getStartTime() + duration - System.currentTimeMillis();
	}
	
	private long getModeTimeLeft() {
		return time + getModeDuration() - System.currentTimeMillis();
	}
	
	private long getModeDuration() {
		switch (state) {
		case PROPELLING:
			return propelDuration;
		case SKIING:
			return skiDuration;
		case HOVERING:
			return hoverDuration;
		}
		return duration;
	}
	
	@Override
	public void remove() {
		super.remove();
		affectedEntities.clear();
		if (topBar != null) {
			topBar.removeAll();
		}
		if (bottomBar != null) {
			bottomBar.removeAll();
		}
		if (!recovery) {
			flightHandler.removeInstance(player, getName());
			AzutoruMethods.removeFlight(player);
			player.setFlySpeed(initFlySpeed);
		}
	}
	
	public void removeWithCooldown() {
		remove();
		if (recovery) {
			bPlayer.addCooldown(getName() + "Recovery", recoveryCooldown);
		} else {
			bPlayer.addCooldown(this);
		}
	}
	
	public void onLeftClick() {
		if (player.isSneaking()) {
			removeWithCooldown();
			return;
		}
		switchJetState(ClickType.LEFT_CLICK);
	}
	
	public void onRightClick() {
		switchJetState(ClickType.RIGHT_CLICK);
	}
	
	public void onSneak() {
		switchJetState(ClickType.SHIFT_DOWN);
	}
	
	public void switchJetState(ClickType clickType) {
		if (combo != JetCombo.NONE) {
			return;
		}
		JetState previous = state;
		
		switch (state) {
		case PROPELLING:
			if (hoverEnabled && (clickType == ClickType.LEFT_CLICK || clickType == ClickType.SHIFT_DOWN)) {
				state = JetState.HOVERING;
				break;
			} else if (skiEnabled && clickType == ClickType.RIGHT_CLICK) {
				state = JetState.SKIING;
				break;
			}
		case SKIING:
			if (hoverEnabled && (clickType == ClickType.LEFT_CLICK || clickType == ClickType.SHIFT_DOWN)) {
				state = JetState.HOVERING;
				break;
			} else if (clickType == ClickType.RIGHT_CLICK) {
				state = JetState.PROPELLING;
				break;
			}
		case HOVERING:
			if (recovery) { // If activated by the recovery move, don't let them change states
				return;
			}
			if (clickType == ClickType.LEFT_CLICK) {
				state = JetState.PROPELLING;
				break;
			} else if (skiEnabled && clickType == ClickType.RIGHT_CLICK) {
				state = JetState.SKIING;
				break;
			}
		default:
			break;
		}
		
		if (previous != state) {
			time = System.currentTimeMillis();
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		return "FireJet";
	}
	
	@Override
	public String getDescription() {
		return "The core mobility move for all firebenders, this ability allows talented firebenders to "
				+ "propel themselves through the air with the force of their fire. The jet is faster when "
				+ "the firebender is on the FireJet slot, using both their hands and feet.";
	}
	
	@Override
	public String getInstructions() {
		return "FireJet users can activate the different modes at the beginning of the ability "
				+ "or while the ability is in use. "
				+ "\n(Activation) Left-click to start Propelling. Right-click on a block to start Skiing. Sneak to start Hovering/Drifting."
				+ "\n(Propel) Look in the direction you want to go. Left-click or sneak to switch to Hover/Drift. Right-click on a block to switch to Ski."
				+ "\n(Ski) Look in the direction you want to go. Left-click or sneak to switch to Hover/Drift. Right-click on a block to switch to Propel."
				+ "\n(Hover) Hover mid-air. Hold sneak to push yourself with Drift. Left-click to switch to Propel. Right-click on a block to switch to Ski."
				+ "\n(Drift) Hold sneak to push yourself in the opposite direction. Release sneak to Hover."
				+ "\n(Recovery) Hold sneak to soften your landing even when FireJet is on cooldown.";
	}

	@Override
	public Location getLocation() {
		return origin;
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
	
	@Override
	public boolean isEnabled() {
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Enabled");
	}

}
