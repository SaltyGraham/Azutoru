package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.SwappedSlotsPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.PlayerUtil.Hand;
import me.aztl.azutoru.util.WorldUtil;

public class WaterSlash extends WaterAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	private float maxAngle;
	
	private Map<Integer, Location> locations;
	private Map<Integer, Vector> directions;
	private List<Location> locList;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	private Location startLoc, endLoc;
	private RemovalPolicy policy;
	private boolean clicked, setup, progressing;
	private int id = 0;
	private long time;
	
	public WaterSlash(Player player, boolean sourced) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, WaterSlash.class)) return;
		
		if (hasAbility(player, Torrent.class))
			getAbility(player, Torrent.class).remove();
		
		if (hasAbility(player, WaterManipulation.class))
			getAbility(player, WaterManipulation.class).remove();
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Water.WaterSlash.Cooldown");
		sourceRange = c.getDouble("Abilities.Water.WaterSlash.SourceRange");
		speed = c.getDouble("Abilities.Water.WaterSlash.Speed");
		hitRadius = c.getDouble("Abilities.Water.WaterSlash.HitRadius");
		damage = c.getDouble("Abilities.Water.WaterSlash.Damage");
		range = c.getDouble("Abilities.Water.WaterSlash.Range");
		duration = c.getLong("Abilities.Water.WaterSlash.Duration");
		maxAngle = c.getInt("Abilities.Water.WaterSlash.MaxAngle");
		
		applyModifiers();
		
		locations = new HashMap<>();
		directions = new HashMap<>();
		affectedBlocks = new ConcurrentHashMap<>();
		locList = new ArrayList<>();
		
		policy = Policies.builder()
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ExpirationPolicy(duration))
					.add(new SwappedSlotsPolicy("Torrent", p -> !clicked)).build();
		
		if (sourced) {
			Block sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_UP, true, true, true, true, true);
			if (sourceBlock != null)
				start();
		} else {
			clicked = true;
			start();
		}
	}
	
	private void applyModifiers() {
		if (isNight(player.getWorld())) {
			cooldown -= ((long) getNightFactor(cooldown) - cooldown);
			damage = getNightFactor(damage);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown /= 2;
			damage *= 2;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || policy.test(player)) {
			remove();
			return;
		}
		
		if (!clicked) {
			ParticleEffect.WATER_SPLASH.display(PlayerUtil.getHandPos(player, Hand.RIGHT), 2);
			return;
		}
		
		if (!setup) {
			if (System.currentTimeMillis() < time + 200) {
				endLoc = GeneralMethods.getTargetedLocation(player, 3);
				if (FastMath.abs(endLoc.getYaw() - startLoc.getYaw()) >= maxAngle
						|| FastMath.abs(endLoc.getPitch() - startLoc.getPitch()) >= maxAngle)
					setup = true;
				return;
			} else setup = true;
		}
		
		if (!progressing) {
			for (Location loc : MathUtil.getLinePoints(player, startLoc, endLoc, (int) range * 3)) {
				locations.put(id, loc);
				directions.put(id, loc.getDirection());
				id++;
			}
			progressing = true;
		} else {
			if (locations.isEmpty() || locations.values().stream().allMatch(l -> GeneralMethods.isSolid(l.getBlock()))) {
				remove();
				return;
			}
			
			WorldUtil.revertBlocks(affectedBlocks);
			locList.clear();
			
			for (Integer i : locations.keySet()) {
				Block b = locations.get(i).getBlock();
				if (GeneralMethods.isSolid(b) || isLava(b)
						|| GeneralMethods.checkDiagonalWall(locations.get(i), directions.get(i))
						|| GeneralMethods.isRegionProtectedFromBuild(this, locations.get(i))) {
					continue;
				}
				
				if (isWater(b) && !TempBlock.isTempBlock(b))
					WorldUtil.displayWaterBubble(locations.get(i));
				
				if (locations.get(i).distanceSquared(startLoc) > range * range) {
					remove();
					return;
				}
				
				locations.get(i).add(directions.get(i).clone().multiply(speed));
				TempBlock tb = new TempBlock(b, Material.WATER);
				tb.setRevertTime(50);
				affectedBlocks.put(b, tb);
				locList.add(locations.get(i));
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(locations.get(i), hitRadius)) {
					if (e instanceof LivingEntity && e != player) {
						DamageHandler.damageEntity(e, damage, this);
						remove();
						return;
					}
				}
			}
		}
	}
	
	public void remove() {
		super.remove();
		WorldUtil.revertBlocks(affectedBlocks);
		affectedBlocks.clear();
		bPlayer.addCooldown(this);
	}
	
	public void onClick() {
		clicked = true;
		time = System.currentTimeMillis();
		startLoc = GeneralMethods.getTargetedLocation(player, 3);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return startLoc;
	}
	
	@Override
	public List<Location> getLocations() {
		return locList;
	}

	@Override
	public String getName() {
		return "WaterSlash";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a waterbender to send forth a quick-moving blade of water that damages players and mobs.";
	}
	
	@Override
	public String getInstructions() {
		return "WaterManipulation (Tap sneak) > WaterManipulation (Hold sneak) > Torrent (Release sneak) > Torrent (Left-click and drag mouse across screen)";
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
	public Object createNewComboInstance(Player player) {
		return new WaterSlash(player, true);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Water.WaterSlash.Enabled");
	}

}
