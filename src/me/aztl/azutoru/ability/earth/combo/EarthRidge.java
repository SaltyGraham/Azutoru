package me.aztl.azutoru.ability.earth.combo;

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
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.ProtectedRegionPolicy;
import me.aztl.azutoru.policy.RangePolicy;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.WorldUtil;

public class EarthRidge extends EarthAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
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

	private TempBlock sourceTempBlock;
	private Location location, origin;
	private Vector direction;
	private List<Location> locations;
	private Set<LivingEntity> affectedEntities;
	private Block sourceBlock;
	private RemovalPolicy policy;
	private BlockFace face;
	private boolean progressing;
	
	public EarthRidge(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		EarthRidge er = getAbility(player, EarthRidge.class);
		if (er != null) {
			if (er.progressing) return;
			er.remove();
		}
		
		if (hasAbility(player, EarthBlast.class))
			getAbility(player, EarthBlast.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.EarthRidge.Cooldown");
		duration = c.getLong("Abilities.Earth.EarthRidge.Duration");
		sourceRange = c.getDouble("Abilities.Earth.EarthRidge.SourceRange");
		range = c.getDouble("Abilities.Earth.EarthRidge.Range");
		damage = c.getDouble("Abilities.Earth.EarthRidge.Damage");
		knockback = c.getDouble("Abilities.Earth.EarthRidge.Knockback");
		knockup = c.getDouble("Abilities.Earth.EarthRidge.Knockup");
		minHeight = c.getInt("Abilities.Earth.EarthRidge.MinHeight");
		maxHeight = c.getInt("Abilities.Earth.EarthRidge.MaxHeight");
		hitRadius = c.getDouble("Abilities.Earth.EarthRidge.HitRadius");
		
		applyModifiers();
		
		sourceBlock = BlockSource.getEarthSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true);
		if (sourceBlock == null) return;
		
		List<Block> targets = player.getLastTwoTargetBlocks(null, (int) sourceRange);
		face = targets.get(1).getFace(targets.get(0));
		
		Material tempMaterial = Material.STONE;
		if (sourceBlock.getType() == Material.STONE)
			tempMaterial = Material.COBBLESTONE;
		sourceTempBlock = new TempBlock(sourceBlock, tempMaterial);
		
		origin = sourceBlock.getLocation().add(MathUtil.getFaceDirection(face));
		location = origin.clone();
		direction = GeneralMethods.getDirection(origin, GeneralMethods.getTargetedLocation(player, range)).normalize();
		locations = new ArrayList<>();
		affectedEntities = new HashSet<>();
		
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ProtectedRegionPolicy(this, () -> location))
					.add(new RangePolicy(range, () -> location))
					.add(new SwappedSlotsPolicy("EarthBlast")).build();
		
		start();
	}
	
	private void applyModifiers() {
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
			direction = player.getEyeLocation().getDirection();
			return;
		}
		
		if (sourceTempBlock != null) {
			sourceTempBlock.revertBlock();
			sourceTempBlock = null;
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
		if (!isEarthbendable(topBlock)) {
			removeWithCooldown();
			return;
		}
		
		int currentHeight = ThreadLocalRandom.current().nextInt(minHeight, maxHeight);
		new RaiseEarth(player, topBlock, face, currentHeight, null);
		
		updateLocations(currentHeight);
		
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
	
	public void onClick() {
		progressing = true;
	}
	
	private void updateLocations(int currentHeight) {
		locations.clear();
		for (int i = 1; i <= currentHeight; i++) {
			Location loc = location.clone().add(MathUtil.getFaceDirection(face).multiply(i));
			locations.add(loc);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		if (sourceTempBlock != null)
			sourceTempBlock.revertBlock();
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
		return "EarthRidge";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows an earthbender to create a moving wall of earth that can deal damage and knockback.";
	}
	
	@Override
	public String getInstructions() {
		return "RaiseEarth (Right-click block) > RaiseEarth (Right-click block) > EarthBlast (Tap sneak) to select a source."
				+ "\nLeft-click to launch the EarthRidge in the direction you're facing."
				+ "\nHold sneak to curve the EarthRidge into the direction you're facing.";
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
		return new EarthRidge(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
		combo.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
		combo.add(new AbilityInformation("EarthBlast", ClickType.SHIFT_DOWN));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.EarthRidge.Enabled");
	}

}
