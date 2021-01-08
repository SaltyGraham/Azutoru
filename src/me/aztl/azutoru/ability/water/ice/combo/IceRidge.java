package me.aztl.azutoru.ability.water.ice.combo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
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
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RangePolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.WorldUtil;

public class IceRidge extends IceAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	private long revertTime;
	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	private int minHeight;
	private int maxHeight;

	private List<Location> locations;
	private Set<LivingEntity> affectedEntities;
	private Location location, origin;
	private Vector direction;
	private Block sourceBlock;
	private RemovalPolicy policy;
	private BlockFace face;
	private boolean progressing;
	
	public IceRidge(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		IceRidge ir = getAbility(player, IceRidge.class);
		if (ir != null) {
			if (ir.progressing) return;
			ir.remove();
		}
		
		if (hasAbility(player, IceSpikeBlast.class))
			getAbility(player, IceSpikeBlast.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Water.IceRidge.Cooldown");
		duration = c.getLong("Abilities.Water.IceRidge.Duration");
		sourceRange = c.getDouble("Abilities.Water.IceRidge.SourceRange");
		range = c.getDouble("Abilities.Water.IceRidge.Range");
		damage = c.getDouble("Abilities.Water.IceRidge.Damage");
		knockback = c.getDouble("Abilities.Water.IceRidge.Knockback");
		knockup = c.getDouble("Abilities.Water.IceRidge.Knockup");
		minHeight = c.getInt("Abilities.Water.IceRidge.MinHeight");
		maxHeight = c.getInt("Abilities.Water.IceRidge.MaxHeight");
		hitRadius = c.getDouble("Abilities.Water.IceRidge.HitRadius");
		revertTime = c.getLong("Abilities.Water.IceRidge.RevertTime");
		
		applyModifiers();
		
		sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, true, false, true, false);
		if (sourceBlock == null) return;
		
		List<Block> targets = player.getLastTwoTargetBlocks(null, (int) sourceRange);
		face = targets.get(1).getFace(targets.get(0));
		
		origin = sourceBlock.getLocation().add(MathUtil.getFaceDirection(face));
		location = origin.clone();
		direction = GeneralMethods.getDirection(origin, GeneralMethods.getTargetedLocation(player, range)).normalize();
		locations = new ArrayList<>();
		affectedEntities = new HashSet<>();
		
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> player.getWorld()))
					.add(new ProtectedRegionPolicy(this, () -> location))
					.add(new RangePolicy(range, origin, () -> location)).build();
		
		start();
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			duration = (long) getNightFactor(duration);
			sourceRange = getNightFactor(sourceRange);
			range = getNightFactor(range);
			damage = getNightFactor(damage);
			knockback = getNightFactor(knockback);
			knockup = getNightFactor(knockup);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			duration *= 2;
			sourceRange *= 1.5;
			range *= 1.5;
			damage *= 1.25;
			knockback *= 1.25;
			knockup *= 1.25;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			removeWithCooldown();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			if (progressing)
				bPlayer.addCooldown(this);
			return;
		}
		
		if (!progressing) {
			if (player.getLocation().distanceSquared(origin) >= sourceRange * sourceRange) {
				remove();
				return;
			}
			
			playFocusWaterEffect(sourceBlock);
			direction = player.getEyeLocation().getDirection();
			return;
		}
		
		if (player.isSneaking())
			direction.add(player.getEyeLocation().getDirection().multiply(0.5));
		
		if (face == BlockFace.UP || face == BlockFace.DOWN)
			direction.setY(0).normalize();
		else if (face == BlockFace.NORTH || face == BlockFace.SOUTH)
			direction.setZ(0).normalize();
		else if (face == BlockFace.EAST || face == BlockFace.WEST)
			direction.setX(0).normalize();
		
		location.add(direction);
		
		if (!isTransparent(location.getBlock()))
			location.add(MathUtil.getFaceDirection(face));
		
		Block topBlock = WorldUtil.getTopBlock(location, face.getOppositeFace(), 3);
		if (!isIce(topBlock) && !isWater(topBlock) && !isSnow(topBlock) 
				&& !isAir(topBlock.getType()) && !WorldUtil.isIgnoredPlant(topBlock)) {
			removeWithCooldown();
			return;
		}
		
		int currentHeight = ThreadLocalRandom.current().nextInt(minHeight, maxHeight);
		addBlocks(topBlock, face, currentHeight);
		updateLocations(currentHeight);
		
		if (ThreadLocalRandom.current().nextInt(6) == 0)
			playIcebendingSound(location);
		
		for (Location loc : locations) {
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, hitRadius)) {
				if (e != player) {
					Vector travelVec = direction.clone().multiply(knockback).setY(direction.getY() * knockup);
					e.setVelocity(travelVec);
					if (e instanceof LivingEntity && !affectedEntities.contains((LivingEntity) e)) {
						DamageHandler.damageEntity(e, damage, this);
						new HorizontalVelocityTracker(e, player, 200, this);
						affectedEntities.add((LivingEntity) e);
					}
				}
			}
		}
	}
	
	private void addBlocks(Block topBlock, BlockFace face, int height) {
		for (int i = 0; i <= height; i++) {
			Block b = topBlock.getRelative(face, i);
			if (isAir(b.getType()) || isWater(b)) {
				TempBlock tb = new TempBlock(b, Material.ICE);
				tb.setRevertTime(revertTime);
				addWaterbendableTempBlock(tb);
				tb.setRevertTask(() -> removeWaterbendableTempBlock(tb));
			}
		}
	}
	
	private void updateLocations(int currentHeight) {
		locations.clear();
		for (int i = 1; i <= currentHeight; i++) {
			Location loc = location.clone().add(MathUtil.getFaceDirection(face).multiply(i));
			locations.add(loc);
		}
	}
	
	public void onClick() {
		progressing = true;
	}
	
	@Override
	public void remove() {
		super.remove();
		affectedEntities.clear();
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
		return "IceRidge";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows an icebender to create a moving wall of ice that can deal damage and knockback.";
	}
	
	@Override
	public String getInstructions() {
		return "Torrent (Tap sneak) > Torrent (Tap sneak) > IceSpike (Tap sneak) to select a source > Left-click to shoot";
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	@Override
	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new IceRidge(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("IceSpike", ClickType.SHIFT_DOWN));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.IceRidge.Enabled");
	}

}
