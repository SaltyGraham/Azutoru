package me.aztl.azutoru.ability.fire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TimeUtil;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.air.CloudSurf;
import me.aztl.azutoru.ability.fire.combo.JetBlast;
import me.aztl.azutoru.ability.fire.combo.JetBlaze;
import me.aztl.azutoru.ability.fire.combo.JetStepping;
import me.aztl.azutoru.policy.DamagePolicy;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.PlayerUtil.Hand;

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
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SPEED)
	private double propelSpeed;
	@Attribute(Attribute.DURATION)
	private long propelDuration;
	@Attribute(Attribute.SPEED)
	private double skiSpeed;
	@Attribute(Attribute.SPEED)
	private double skiTurningSpeed;
	@Attribute(Attribute.DURATION)
	private long skiDuration;
	@Attribute(Attribute.SPEED)
	private double hoverSpeed;
	@Attribute(Attribute.SPEED)
	private double driftSpeed;
	@Attribute(Attribute.DURATION)
	private long hoverDuration;
	@Attribute(Attribute.DURATION)
	private long recoveryDuration;
	@Attribute(Attribute.COOLDOWN)
	private long recoveryCooldown;
	private int particleAmount;
	private double particleSpread;
	private double length;
	private double onSlotModifier;
	@Attribute(Attribute.DAMAGE + "Threshold")
	private double damageThreshold;
	private double blastSpeedMod;
	private double blazeSpeedMod;
	@Attribute(Attribute.RADIUS)
	private double blazeHitRadius;
	@Attribute(Attribute.DAMAGE)
	private double blazeDamage;
	@Attribute(Attribute.FIRE_TICK)
	private int blazeFireTicks;
	private boolean skiEnabled, hoverEnabled, driftEnabled, recoveryEnabled;

	private List<Entity> affectedEntities;
	private Location location, origin, right, left;
	private Vector direction;
	private BossBar topBar, bottomBar;
	private RemovalPolicy policy;
	private JetState state;
	private JetCombo combo;
	private long time;
	private float yaw, pitch, initFlySpeed;
	private boolean avatarState, isOnSlot, recovery, canFly, isFlying;
	
	public FireJet(Player player, ClickType clickType) {
		super(player);
		
		if (hasAbility(player, FireJet.class)
				&& !hasAbility(player, JetBlast.class)
				&& !hasAbility(player, JetBlaze.class)) {
			FireJet fj = getAbility(player, FireJet.class);
			if (clickType == ClickType.LEFT_CLICK)
				fj.onLeftClick();
			else if (clickType == ClickType.RIGHT_CLICK)
				fj.onRightClick();
			else
				fj.onSneak();
			return;
		}
		
		if (!bPlayer.canBendIgnoreCooldowns(this) || hasAbility(player, JetStepping.class)) return;
		
		if (hasAbility(player, AirSpout.class))
			getAbility(player, AirSpout.class).remove();
		
		if (hasAbility(player, CloudSurf.class))
			getAbility(player, CloudSurf.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Fire.FireJet.Cooldown");
		duration = c.getLong("Abilities.Fire.FireJet.Duration");
		particleAmount = c.getInt("Abilities.Fire.FireJet.ParticleAmount");
		particleSpread = c.getDouble("Abilities.Fire.FireJet.ParticleSpread");
		length = c.getDouble("Abilities.Fire.FireJet.ParticleLength");
		onSlotModifier = c.getDouble("Abilities.Fire.FireJet.OnSlotModifier");
		damageThreshold = c.getDouble("Abilities.Fire.FireJet.DamageThreshold");
		propelSpeed = c.getDouble("Abilities.Fire.FireJet.Propel.Speed");
		propelDuration = c.getLong("Abilities.Fire.FireJet.Propel.Duration");
		skiEnabled = c.getBoolean("Abilities.Fire.FireJet.Ski.Enabled");
		skiSpeed = c.getDouble("Abilities.Fire.FireJet.Ski.Speed");
		skiDuration = c.getLong("Abilities.Fire.FireJet.Ski.Duration");
		skiTurningSpeed = c.getDouble("Abilities.Fire.FireJet.Ski.TurningSpeed");
		hoverEnabled = c.getBoolean("Abilities.Fire.FireJet.Hover.Enabled");
		hoverSpeed = c.getDouble("Abilities.Fire.FireJet.Hover.Speed");
		hoverDuration = c.getLong("Abilities.Fire.FireJet.Hover.Duration");
		recoveryEnabled = c.getBoolean("Abilities.Fire.FireJet.Hover.Recovery.Enabled");
		recoveryDuration = c.getLong("Abilities.Fire.FireJet.Hover.Recovery.Duration");
		recoveryCooldown = c.getLong("Abilities.Fire.FireJet.Hover.Recovery.Cooldown");
		driftEnabled = c.getBoolean("Abilities.Fire.FireJet.Hover.Drift.Enabled");
		driftSpeed = c.getDouble("Abilities.Fire.FireJet.Hover.Drift.Speed");
		
		blastSpeedMod = c.getDouble("Abilities.Fire.JetBlast.SpeedModifier");
		
		blazeSpeedMod = c.getDouble("Abilities.Fire.JetBlaze.SpeedModifier");
		blazeHitRadius = c.getDouble("Abilities.Fire.JetBlaze.ParticleHitRadius");
		blazeFireTicks = c.getInt("Abilities.Fire.JetBlaze.FireTicks");
		blazeDamage = c.getDouble("Abilities.Fire.JetBlaze.Damage");
		
		applyModifiers();
		
		origin = player.getLocation();
		location = origin.clone();
		direction = player.getEyeLocation().getDirection().clone().normalize().multiply(propelSpeed);
		time = System.currentTimeMillis();
		affectedEntities = new ArrayList<>();
		canFly = player.getAllowFlight();
		isFlying = player.isFlying();
		initFlySpeed = player.getFlySpeed();
		
		policy = Policies.builder()
					.add(Policies.IN_LIQUID)
					.add(new DamagePolicy(damageThreshold, () -> this.player.getHealth(), p -> !BendingPlayer.getBendingPlayer(p).isAvatarState()))
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ExpirationPolicy(duration)).build();
		
		Block b = origin.getBlock();
		if (b.isLiquid()) return;
		
		switch (clickType) {
		case LEFT_CLICK:
			if (bPlayer.isOnCooldown(this)) return;
			state = JetState.PROPELLING;
			break;
		case RIGHT_CLICK:
			if (bPlayer.isOnCooldown(this)) return;
			state = JetState.SKIING;
			break;
		case SHIFT_DOWN:
			if (PlayerUtil.isOnGround(player)) return;
			if (bPlayer.isOnCooldown(this)) {
				if (!recoveryEnabled) return;
				if (bPlayer.isOnCooldown(getName() + "Recovery")) {
					long recoveryCd = bPlayer.getCooldown(getName() + "Recovery") - System.currentTimeMillis();
					ActionBar.sendActionBar(ChatColor.RED + "FireJet Recovery - " + TimeUtil.formatTime(recoveryCd), player);
					return;
				}
				state = JetState.HOVERING;
				duration = recoveryDuration;
				recovery = true;
			} else
				state = JetState.HOVERING;
			break;
		default:
			return;
		}
		combo = JetCombo.NONE;
		
		initializeBars();
		
		if (!recovery) {
			flightHandler.createInstance(player, getName());
			PlayerUtil.allowFlight(player);
			player.setFlySpeed(0);
		}
		
		if (state == JetState.PROPELLING) {
			player.setVelocity(direction.clone().multiply(1.5));
			if (isAir(b.getType()))
				createTempFire(b.getLocation());
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
			if (progress < 0) progress = 0;
			topBar.setProgress(progress);
		}
		
		if (getModeDuration() <= 0) {
			bottomBar.setProgress(1);
		} else {
			double progress = (double) getModeTimeLeft() / (double) getModeDuration();
			if (progress < 0) progress = 0;
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
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || policy.test(player)) {
			removeWithCooldown();
			return;
		}
		
		if (avatarState && !bPlayer.isAvatarState()) {
			removeWithCooldown();
			return;
		}
		
		isOnSlot = bPlayer.getBoundAbilityName().equals(getName());
		
		if (hasAbility(player, JetBlast.class)) {
			combo = JetCombo.BLAST;
			state = JetState.PROPELLING;
		} else if (hasAbility(player, JetBlaze.class)) {
			combo = JetCombo.BLAZE;
			state = JetState.PROPELLING;
		} else {
			combo = JetCombo.NONE;
		}
		
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playFirebendingSound(location);
		
		updateBars();
		
		location = player.getLocation();
		right = PlayerUtil.getHandPos(player, Hand.RIGHT);
		left = PlayerUtil.getHandPos(player, Hand.LEFT);
		yaw = MathUtil.getOppositeYaw(location.getYaw());
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
	
	private void propel() {
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
			
			Location r = MathUtil.getModifiedLocation(right, 150, pitch);
			Location l = MathUtil.getModifiedLocation(left, -150, pitch);
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
	
	private void ski() {
		if (skiDuration > 0 && System.currentTimeMillis() > time + skiDuration) {
			removeWithCooldown();
			return;
		}
		
		double timeFactor = getTimeFactor(skiDuration);
		double speed = skiSpeed;
		if (isOnSlot) {
			speed = speed * onSlotModifier;
			
			Location r = MathUtil.getModifiedLocation(right, 150, pitch);
			Location l = MathUtil.getModifiedLocation(left, -150, pitch);
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
		if (topBlock == null)
			loc.setPitch(10);
		
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
	
	private void hover() {
		if ((hoverDuration > 0 && System.currentTimeMillis() > time + hoverDuration)
				|| PlayerUtil.isOnGround(player)) {
			removeWithCooldown();
			return;
		}
		
		Location loc = location.clone();
		loc.setPitch(90);
		Vector dir = loc.getDirection();
		displayJet(loc, dir, 1, 0.1);
		
		if (isOnSlot) {
			Location r = right.clone();
			r.setPitch(90);
			Location l = left.clone();
			l.setPitch(90);
			Vector rv = r.getDirection();
			Vector lv = l.getDirection();
			displayJets(r, l, rv, lv, 1, 0.25);
		}
		
		if (!player.isSneaking() || (player.isSneaking() && !isOnSlot)) {
			player.setVelocity(player.getVelocity().clone().multiply(hoverSpeed));
		} else if (driftEnabled && !recovery && isOnSlot) {
			double timeFactor = getTimeFactor(hoverDuration);
			double speed = driftSpeed;
			if (isOnSlot)
				speed *= onSlotModifier;
			Location target = GeneralMethods.getTargetedLocation(player, 1);
			direction = GeneralMethods.getDirection(target, player.getEyeLocation()).multiply(speed * timeFactor);
			player.setVelocity(direction);
		}
	}
	
	private void displayJet(Location loc, Vector dir, double length, double stepSize) {
		for (double d = 0; d <= length; d += stepSize) {
			Block b = loc.getBlock();
			if (GeneralMethods.isSolid(b) || b.isLiquid()) break;
			particle(loc);
			loc.add(dir);
			if (combo == JetCombo.BLAZE)
				damage(loc);
		}
	}
	
	private void displayJets(Location r, Location l, Vector rv, Vector lv, double length, double stepSize) {
		for (double d = 0; d <= length; d += stepSize) {
			if (GeneralMethods.isSolid(r.getBlock()) || r.getBlock().isLiquid()
					|| GeneralMethods.isSolid(l.getBlock()) || l.getBlock().isLiquid())
				continue;
			
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
			if (e instanceof LivingEntity && e != player && !affectedEntities.contains(e)) {
				DamageHandler.damageEntity(e, blazeDamage, getAbility(player, JetBlaze.class));
				e.setFireTicks(blazeFireTicks);
				new FireDamageTimer(e, player);
				affectedEntities.add(e);
			}
		}
	}
	
	private double getTimeFactor(long currentDuration) {
		if (currentDuration <= 0) return 1;
		return bPlayer.isAvatarState() ? 1 : 1 - (System.currentTimeMillis() - time) / (1.5 * currentDuration);
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
		if (combo == JetCombo.BLAST)
			getAbility(player, JetBlast.class).remove();
		else if (combo == JetCombo.BLAZE)
			getAbility(player, JetBlaze.class).remove();
		
		affectedEntities.clear();
		
		if (topBar != null)
			topBar.removeAll();
		if (bottomBar != null)
			bottomBar.removeAll();
		
		if (!recovery) {
			flightHandler.removeInstance(player, getName());
			PlayerUtil.removeFlight(player, canFly, isFlying);
			player.setFlySpeed(initFlySpeed);
		}
	}
	
	public void removeWithCooldown() {
		remove();
		if (recovery)
			bPlayer.addCooldown(getName() + "Recovery", recoveryCooldown);
		else
			bPlayer.addCooldown(this);
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
		if (combo != JetCombo.NONE) return;
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
		
		if (previous != state)
			time = System.currentTimeMillis();
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
