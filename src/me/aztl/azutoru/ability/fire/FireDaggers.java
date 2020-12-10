package me.aztl.azutoru.ability.fire;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;
import me.aztl.azutoru.ability.fire.combo.FireBlade;

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
	
	private Dagger activeDagger, lastActiveDagger;
	private Ability activeAbility;
	private Location left, right, location;
	private long time;
	public boolean blocking;
	private int counter;
	
	public FireDaggers(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.Duration");
		blockDuration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.BlockDuration");
		usageCooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireDaggers.UsageCooldown");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.Damage");
		range = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.Range");
		throwSpeed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireDaggers.ThrowSpeed");
		maxThrows = Azutoru.az.getConfig().getInt("Abilities.Fire.FireDaggers.MaxThrows");
		
		applyModifiers();
		
		blocking = false;
		
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
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		left = AzutoruMethods.getHandPos(player, Hand.LEFT);
		right = AzutoruMethods.getHandPos(player, Hand.RIGHT);
		
		if (left.getBlock().isLiquid() && right.getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (counter % 6 == 0) {
			playFirebendingSound(right);
		}
		counter++;
		
		if (activeAbility != Ability.BLOCK) {
			time = System.currentTimeMillis();
			if (isBlocking()) {
				setBlocking(false);
			}
			
			for (double d = 0; d <= 0.3; d += 0.1) {
				left = GeneralMethods.getLeftSide(left, d).subtract(0, d * 0.6, 0);
				right = GeneralMethods.getRightSide(right, d).subtract(0, d * 0.6, 0);
				playFirebendingParticles(left, 1, 0, 0, 0);
				playFirebendingParticles(right, 1, 0, 0, 0);
			}
		} else {
			progressBlock();
			if (!isBlocking()) {
				setBlocking(true);
			}
		}
	}
	
	private void progressBlock() {
		while (System.currentTimeMillis() >= time) {
			left = left.subtract(0, 0.3, 0);
			right = right.subtract(0, 0.3, 0);
			break;
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(left, 1)) {
			blockEntitiesAroundPoint(e, left);
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(right, 1)) {
			blockEntitiesAroundPoint(e, right);
		}
		
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
		location = null;
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
		if (bPlayer.isOnCooldown(getName() + "_ATTACK")) {
			return;
		}
		
		if (lastActiveDagger == null) {
			activeDagger = Dagger.RIGHT;
		} else if (lastActiveDagger == Dagger.RIGHT) {
			activeDagger = Dagger.LEFT;
		} else if (lastActiveDagger == Dagger.LEFT) {
			activeDagger = Dagger.RIGHT;
		}
		
		new FireBlade(player, range, damage, throwSpeed, 1, 35, true);
		
		bPlayer.addCooldown(getName() + "_ATTACK", usageCooldown);
		maxThrows--;
	}
	
	// Sneaking while on the ground blocks attacks
	public void onSneak() {
		if (!AzutoruMethods.isOnGround(player)) {
			return;
		}
		if (bPlayer.isOnCooldown(getName() + "_BLOCK")) {
			return;
		}
		
		activeAbility = Ability.BLOCK;
		
		bPlayer.addCooldown(getName() + "_BLOCK", usageCooldown);
	}
	
	// Sneaking midair gives the player the option to either shoot FireBlast or FireBlade from their feet
	// Releasing sneak midair shoots a FireBlast
	public void onJumpReleaseSneak() {
		if (AzutoruMethods.isOnGround(player)) {
			return;
		}
		if (hasAbility(player, FireBlade.class) || hasAbility(player, FireJet.class)) {
			return;
		}
		new FireBlast(player);
	}
	
	// Sneak-clicking midair shoots a FireBlade
	public void onJumpSneakClick() {
		if (AzutoruMethods.isOnGround(player)) {
			return;
		}
		if (hasAbility(player, FireBlast.class) || hasAbility(player, FireJet.class)) {
			return;
		}
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
		if (activeAbility == null) {
			if (activeDagger == Dagger.LEFT) {
				return left;
			} else {
				return right;
			}
		} else if (activeAbility == Ability.ATTACK) {
			return location;
		} else if (activeAbility == Ability.BLOCK) {
			if (activeDagger == Dagger.LEFT) {
				return left;
			} else {
				return right;
			}
		}
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
