package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;

public class WaterSlash extends WaterAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double sourceRange, speed, hitRadius, damage, range;
	
	private Location startLoc, endLoc;
	private int id = 0;
	private HashMap<Integer, Location> locations;
	private HashMap<Integer, Vector> directions;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	private boolean clicked, setup, progressing;
	private long time;
	
	public WaterSlash(Player player, boolean sourced) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, WaterSlash.class)) {
			return;
		}
		
		if (hasAbility(player, Torrent.class)) {
			getAbility(player, Torrent.class).remove();
		}
		
		if (hasAbility(player, WaterManipulation.class)) {
			getAbility(player, WaterManipulation.class).remove();
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSlash.Cooldown");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSlash.SourceRange");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSlash.Speed");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSlash.HitRadius");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSlash.Damage");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterSlash.Range");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSlash.Duration");
		
		applyModifiers();
		
		locations = new HashMap<Integer, Location>();
		directions = new HashMap<Integer, Vector>();
		affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();
		
		if (sourced) {
			Block sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_UP, true, true, true, true, true);
			if (sourceBlock != null) {
				start();
			}
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
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (!clicked) {
			if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("torrent")) {
				remove();
				return;
			}
			ParticleEffect.WATER_SPLASH.display(AzutoruMethods.getHandPos(player, Hand.RIGHT), 2);
			return;
		}
		
		if (!setup) {
			if (System.currentTimeMillis() < time + 200) {
				
				endLoc = GeneralMethods.getTargetedLocation(player, 3);

				if (Math.abs(endLoc.getYaw() - startLoc.getYaw()) >= 50) {
					setup = true;
				}
				if (Math.abs(endLoc.getPitch() - startLoc.getPitch()) >= 50) {
					setup = true;
				}
				
				return;
			} else setup = true;
		}
		
		if (!progressing) {
			List<Location> linePoints = new ArrayList<Location>();
			linePoints = AzutoruMethods.getLinePoints(player, startLoc, endLoc, (int) range * 3);
			for (Location loc : linePoints) {
				locations.put(id, loc);
				directions.put(id, loc.getDirection());
				id++;
			}
			progressing = true;
		} else {
			if (locations.isEmpty()) {
				remove();
				return;
			}
			
			AzutoruMethods.revertBlocks(affectedBlocks);
			for (Integer i : locations.keySet()) {
				Block b = locations.get(i).getBlock();
				if (GeneralMethods.isSolid(b) || ElementalAbility.isLava(b)) {
					continue;
				} else if (WaterAbility.isWater(b) && !TempBlock.isTempBlock(b)) {
					AzutoruMethods.displayWaterBubble(locations.get(i));
				}
				
				if (locations.get(i).distanceSquared(startLoc) > range * range) {
					remove();
					return;
				}
				
				locations.get(i).add(directions.get(i).clone().multiply(speed));
				TempBlock tb = new TempBlock(b, Material.WATER);
				tb.setRevertTime(50);
				affectedBlocks.put(b, tb);
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(locations.get(i), hitRadius)) {
					if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
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
		AzutoruMethods.revertBlocks(affectedBlocks);
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
		return true;
	}

}
