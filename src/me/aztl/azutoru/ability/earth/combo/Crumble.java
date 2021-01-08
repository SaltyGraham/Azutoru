package me.aztl.azutoru.ability.earth.combo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.util.MathUtil;

public class Crumble extends EarthAbility implements AddonAbility, ComboAbility {
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double detectionRadius;
	private static double hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.Crumble.HitRadius");
	private static double damage = Azutoru.az.getConfig().getDouble("Abilities.Earth.Crumble.Damage");
	private int maxBlasts;

	private static Set<FallingBlock> blocks = new HashSet<>();
	private Location location;
	private Vector direction;
	
	public Crumble(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.Crumble.Cooldown");
		range = c.getDouble("Abilities.Earth.Crumble.Range");
		detectionRadius = c.getDouble("Abilities.Earth.Crumble.DetectionRadius");
		maxBlasts = c.getInt("Abilities.Earth.Crumble.MaxEarthBlasts");
		
		location = player.getEyeLocation();
		direction = location.getDirection();
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		for (int i = 0; i < range; i++) {
			location.add(direction);
			Block b = location.getBlock();
			
			RaiseEarth re = RaiseEarth.getAffectedBlocks().get(b);
			if (re != null) {
				handleRaiseEarth(re);
				removeWithCooldown();
				return;
			}
			
			TempBlock t = TempBlock.get(b);
			if (t != null && EarthTent.getAffectedBlocks().contains(t)) {
				double tentLength = Azutoru.az.getConfig().getDouble("Abilities.Earth.EarthTent.Length");
				for (Block block : GeneralMethods.getBlocksAroundPoint(location, tentLength * tentLength)) {
					TempBlock tb = TempBlock.get(block);
					if (tb != null && EarthTent.getAffectedBlocks().contains(tb)) {
						playEarthbendingSound(location);
						crumble(block);
						tb.revertBlock();
						EarthTent.removeBlock(tb);
					}
				}
			}
			
			for (EarthSmash es : getAbilities(EarthSmash.class)) {
				if (es.getLocation() == null || es.getLocation().getWorld() != player.getWorld()) continue;
				
				if (es.getLocation().distanceSquared(location) <= detectionRadius * detectionRadius) {
					es.getLocations().forEach(l -> handleEarthSmash(l));
					es.remove();
					removeWithCooldown();
					return;
				}
			}
			
			for (EarthBlast eb : getAbilities(EarthBlast.class)) {
				if (eb.getLocation() == null || eb.getLocation().getWorld() != player.getWorld() || eb.getPlayer() == player) continue;
				
				if (eb.getLocation().distanceSquared(location) <= detectionRadius * detectionRadius) {
					playEarthbendingSound(location);
					Block block = eb.getLocation().getBlock();
					if (isEarthbendable(block))
						crumble(block);
					eb.remove();
					if (--maxBlasts == 0) {
						removeWithCooldown();
						return;
					}
				}
			}
		}
	}
	
	private void handleRaiseEarth(RaiseEarth instance) {
		playEarthbendingSound(location);
		instance.getColumns().forEach(c -> c.getBlocks().forEach(b -> crumble(b)));
		if (instance.getColumns().size() == 1)
			instance.getAdjacentColumns(instance.getColumns().get(0)).forEach(c -> c.getBlocks().forEach(b -> crumble(b)));
		instance.removeAllColumns();
		instance.remove();
	}
	
	private void handleEarthSmash(Location loc) {
		playEarthbendingSound(loc);
		Block b = loc.getBlock();
		if (isEarth(b))
			crumble(b);
	}
	
	private void crumble(Block b) {
		crumble(b, direction);
	}
	
	public static void crumble(Block b, Vector direction) {
		FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(0.5, 0.5, 0.5), b.getType(), b.getBlockData());
		fb.setVelocity(MathUtil.rotateAroundAxesXZ(direction, FastMath.random() * 60 - 20).multiply(0.5));
		fb.setMetadata("Crumble", new FixedMetadataValue(Azutoru.az, 0));
		fb.setHurtEntities(false);
		fb.setDropItem(false);
		blocks.add(fb);
	}
	
	public static void progressAll() {
		Iterator<FallingBlock> it = blocks.iterator();
		while (it.hasNext()) {
			FallingBlock fb = it.next();
			if (fb.isDead()) {
				blocks.remove(fb);
				continue;
			}
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), hitRadius)) {
				if (e instanceof LivingEntity) {
					((LivingEntity) e).damage(damage);
					blocks.remove(fb);
					fb.remove();
				}
			}
		}
	}
	
	public static void addBlock(FallingBlock fb) {
		blocks.add(fb);
	}
	
	@Override
	public void handleCollision(Collision c) {
		if (c.isRemovingFirst()) {
			CoreAbility second = c.getAbilitySecond();
			if (second instanceof RaiseEarth)
				handleRaiseEarth((RaiseEarth) second);
			else if (second instanceof EarthSmash)
				handleEarthSmash(((EarthSmash) second).getLocation());
		}
	}
	
	public void removeWithCooldown() {
		remove();
		bPlayer.addCooldown(this);
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
		return "Crumble";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an earthbender to crash through RaiseEarths, EarthSmashes, EarthRidges, and several EarthBlasts, effectively blocking them."
				+ "\nEarthbenders can also simply hold sneak with EarthSmash to block falling blocks from Crumble or other abilities.";
	}
	
	@Override
	public String getInstructions() {
		return "EarthSmash (Tap sneak) > EarthSmash (Left-click)"
				+ "\n(Blocking) Hold sneak with EarthSmash";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Crumble(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("EarthSmash", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("EarthSmash", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("EarthSmash", ClickType.LEFT_CLICK));
		return combo;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.Crumble.Enabled");
	}

}
