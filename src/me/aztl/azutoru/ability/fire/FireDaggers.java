package me.aztl.azutoru.ability.fire;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.fire.combo.FireBlade;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.UsedAmmoPolicy;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.PlayerUtil.Hand;

public class FireDaggers extends FireAbility implements AddonAbility {

	private static enum Dagger {
		LEFT, RIGHT;
	}
	
	private static enum Ability {
		ATTACK, BLOCK;
	}
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.DURATION)
	private long blockDuration;
	@Attribute(Attribute.COOLDOWN)
	private long usageCooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double throwSpeed;
	private int maxThrows;
	
	private Location left, right;
	private RemovalPolicy policy;
	private Dagger activeDagger, lastActiveDagger;
	private Ability activeAbility;
	private long time;
	public boolean blocking;
	
	public FireDaggers(Player player) {
		super(player);
		
		if (hasAbility(player, FireDaggers.class)) {
			FireDaggers fd = getAbility(player, FireDaggers.class);
			if (player.isSneaking())
				fd.onJumpSneakClick();
			else
				fd.onClick();
			return;
		}
		
		if (!bPlayer.canBend(this)) return;
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Fire.FireDaggers.Cooldown");
		duration = c.getLong("Abilities.Fire.FireDaggers.Duration");
		blockDuration = c.getLong("Abilities.Fire.FireDaggers.BlockDuration");
		usageCooldown = c.getLong("Abilities.Fire.FireDaggers.UsageCooldown");
		damage = c.getDouble("Abilities.Fire.FireDaggers.Damage");
		range = c.getDouble("Abilities.Fire.FireDaggers.Range");
		throwSpeed = c.getDouble("Abilities.Fire.FireDaggers.ThrowSpeed");
		maxThrows = c.getInt("Abilities.Fire.FireDaggers.MaxThrows");
		
		applyModifiers();
		
		blocking = false;
		policy = Policies.builder()
					.add(Policies.IN_LIQUID)
					.add(new ExpirationPolicy(duration))
					.add(new UsedAmmoPolicy(() -> maxThrows)).build();
		
		start();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			duration *= BlueFireAbility.getRangeFactor();
			damage *= BlueFireAbility.getDamageFactor();
			range *= BlueFireAbility.getRangeFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			duration = (long) getDayFactor(duration);
			range = getDayFactor(range);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			damage *= 2;
			range *= 2;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBend(this) || policy.test(player)) {
			remove();
			return;
		}
		
		left = PlayerUtil.getHandPos(player, Hand.LEFT);
		right = PlayerUtil.getHandPos(player, Hand.RIGHT);
		
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playFirebendingSound(right);
		
		if (activeAbility != Ability.BLOCK) {
			time = System.currentTimeMillis();
			if (isBlocking())
				setBlocking(false);
			
			for (double d = 0; d <= 0.3; d += 0.1) {
				left = GeneralMethods.getLeftSide(left, d).subtract(0, d * 0.6, 0);
				right = GeneralMethods.getRightSide(right, d).subtract(0, d * 0.6, 0);
				playFirebendingParticles(left, 1, 0, 0, 0);
				playFirebendingParticles(right, 1, 0, 0, 0);
			}
		} else {
			progressBlock();
			if (!isBlocking())
				setBlocking(true);
		}
	}
	
	private void progressBlock() {
		if (System.currentTimeMillis() >= time) {
			left = left.subtract(0, 0.3, 0);
			right = right.subtract(0, 0.3, 0);
		}
		
		GeneralMethods.getEntitiesAroundPoint(left, 1).forEach(e -> blockEntitiesAroundPoint(e, left));
		GeneralMethods.getEntitiesAroundPoint(right, 1).forEach(e -> blockEntitiesAroundPoint(e, right));
		
		for (double d = 0; d <= 0.4; d += 0.1) {
			left = GeneralMethods.getRightSide(left, d).add(0, 0.2, 0);
			right = GeneralMethods.getLeftSide(right, d).add(0, 0.2, 0);
			
			playFirebendingParticles(left, 1, 0, 0, 0);
			playFirebendingParticles(right, 1, 0, 0, 0);
		}
		
		if (!player.isSneaking() || System.currentTimeMillis() > time + blockDuration) {
			reset();
			return;
		}
	}
	
	private void blockEntitiesAroundPoint(Entity entity, Location location) {
		if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
			entity.setFireTicks(40);
			new FireDamageTimer(entity, player);
		} else if (entity instanceof Projectile) {
			entity.remove();
		}
	}
	
	public void reset() {
		checkForAmmo();
		lastActiveDagger = activeDagger;
		activeDagger = null;
		activeAbility = null;
	}
	
	private void checkForAmmo() {
		if (maxThrows < 1) {
			remove();
			return;
		}
	}
	
	// Normal clicking shoots a dagger
	public void onClick() {
		if (bPlayer.isOnCooldown(getName() + "_ATTACK")) return;
		new FireBlade(player, range, damage, throwSpeed, 1, 35, true);
		activeDagger = lastActiveDagger == Dagger.RIGHT ? Dagger.LEFT : Dagger.RIGHT;
		bPlayer.addCooldown(getName() + "_ATTACK", usageCooldown);
		maxThrows--;
	}
	
	// Sneaking while on the ground blocks attacks
	public void onSneak() {
		if (!PlayerUtil.isOnGround(player)
				|| bPlayer.isOnCooldown(getName() + "_BLOCK"))
			return;
		activeAbility = Ability.BLOCK;
		bPlayer.addCooldown(getName() + "_BLOCK", usageCooldown);
	}
	
	// Sneaking midair gives the player the option to either shoot FireBlast or FireBlade from their feet
	
	// Releasing sneak midair shoots a FireBlast
	public void onJumpReleaseSneak() {
		if (PlayerUtil.isOnGround(player)
				|| hasAbility(player, FireBlade.class)
				|| hasAbility(player, FireJet.class))
			return;
		new FireBlast(player);
	}
	
	// Sneak-clicking midair shoots a FireBlade
	public void onJumpSneakClick() {
		if (PlayerUtil.isOnGround(player)
				|| hasAbility(player, FireBlade.class)
				|| hasAbility(player, FireJet.class))
			return;
		new FireBlade(player);
	}
	
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	
	public boolean isBlocking() {
		return blocking;
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
		return right;
	}

	@Override
	public String getName() {
		return "FireDaggers";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a firebender to control daggers of flame in their hands.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click to create the daggers. Left-click again to attack with a dagger, and hold sneak to block attacks with your daggers. Tap sneak midair to send out a FireBlast, and sneak-click midair to send out a FireBlade.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireDaggers.Enabled");
	}

}
