package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

import me.aztl.azutoru.Azutoru;

public class RazorRings extends WaterAbility implements AddonAbility, ComboAbility {

	private double damage, range, sourceRange, speed, radiusIncreaseRate;
	private long cooldown, duration, timeBetweenShots;
	private int remainingRings;
	
	private Location origin;
	private long lastShotTime;
	private int lastProjectileId = 0;
	private HashMap<Integer, Location> locations, deadProjectiles;
	private HashMap<Integer, Vector> directions;
	private HashMap<Integer, ArrayList<TempBlock>> affectedBlocks;
	private HashMap<Integer, Double> radiusMap;

	public RazorRings(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this) || !bPlayer.canBendIgnoreCooldowns(getAbility("Surge")) || bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (hasAbility(player, RazorRings.class)) {
			RazorRings rr = getAbility(player, RazorRings.class);
			
			if (rr.getRemainingRings() == 0 || System.currentTimeMillis() < rr.getLastShotTime() + rr.getTimeBetweenShots()) {
				return;
			}
			
			if (rr.getRemainingRings() == 1) {
				bPlayer.addCooldown(rr);
			}
			
			int projectileId = rr.getLastProjectileId() + 1;
			Location loc = rr.origin.clone();
			loc.setYaw(player.getLocation().getYaw());
			
			rr.getParticleLocations().put(projectileId, loc);
			rr.getDirections().put(projectileId, player.getLocation().getDirection().setY(0));
			rr.setLastShotTime(System.currentTimeMillis());
			rr.setRemainingRings(rr.getRemainingRings() - 1);
			rr.setAffectedBlocks(projectileId, new ArrayList<TempBlock>());
			rr.setRadiusMap(projectileId, 0);
			rr.setLastProjectileId(projectileId);
			
		} else {
			
			setFields();
			
			if (!setOrigin())
				return;
			
			start();
			
			SurgeWave surgeWave = getAbility(player, SurgeWave.class);
			if (surgeWave != null) {
				surgeWave.remove();
			}
		}
	}
	
	public boolean setOrigin() {
	
		Block source = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, true, false, false, false);
		
		if (source != null) {
			origin = source.getLocation().add(0.5, 1, 0.5);
			
			return true;
		}
		
		return false;
	}

	private void setFields() {

		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.RazorRings.Damage");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.RazorRings.Cooldown");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.RazorRings.Range");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.RazorRings.SourceRange");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.RazorRings.Speed");
		remainingRings = Azutoru.az.getConfig().getInt("Abilities.Water.RazorRings.RingsCount");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.RazorRings.Duration");
		timeBetweenShots = Azutoru.az.getConfig().getLong("Abilities.Water.RazorRings.ShotCooldown");
		radiusIncreaseRate = Azutoru.az.getConfig().getDouble("Abilities.Water.RazorRings.RadiusIncreaseRate");
		
		lastShotTime = getStartTime();
		
		deadProjectiles = new HashMap<Integer, Location>();
		locations = new HashMap<Integer, Location>();
		directions = new HashMap<Integer, Vector>();
		affectedBlocks = new HashMap<Integer, ArrayList<TempBlock>>();
		radiusMap = new HashMap<Integer, Double>();
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || !player.isSneaking()) {
			remove();
			bPlayer.addCooldown(this);
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, origin)) {
			remove();
			return;
		}
		
		if (player.getLocation().distanceSquared(origin) > sourceRange * sourceRange) {
			remove();
			bPlayer.addCooldown(this);
			return;
		}
		
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 10);
		if (isWater(topBlock) && !TempBlock.isTempBlock(topBlock)) {
			origin = topBlock.getLocation().add(0.5, 1, 0.5);
		}
		
		ParticleEffect.WATER_SPLASH.display(origin, 5, 1+ Math.random(), Math.random(), 1+ Math.random(), 10);
		
		playWaterbendingSound(origin);
		
		progressProjectiles();
		
	}
	
	public void progressProjectiles() {
		SurgeWave surgeWave = getAbility(player, SurgeWave.class);
		if (surgeWave != null) {
			surgeWave.remove();
			return;
		}
		
		if (remainingRings == 0 && locations.isEmpty()) {
			remove();
			return;
		}
		
		for (Integer i : locations.keySet()) {
			
			drawCardioid(i, locations.get(i), radiusMap.get(i));
			radiusMap.put(i, radiusMap.get(i) + radiusIncreaseRate);
			
			if (GeneralMethods.isRegionProtectedFromBuild(this, locations.get(i))) {
				remove();
				return;
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(locations.get(i), 1.5)) {
				if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId())) {
					DamageHandler.damageEntity(e, damage, this);
				}
			}
			
			locations.get(i).add(directions.get(i).clone().multiply(speed));
			if (locations.get(i).distance(origin) > range) {
				deadProjectiles.put(i, locations.get(i));
			}
			
			if (GeneralMethods.isSolid(locations.get(i).getBlock()) && !locations.get(i).getBlock().getType().toString().contains("LEAVES")) {
				deadProjectiles.put(i, locations.get(i));
			}
			
			if (locations.get(i).getBlock().getType().toString().contains("LEAVES")) {
				new PlantRegrowth(player, locations.get(i).getBlock());
				locations.get(i).getBlock().setType(Material.AIR);
			}
			
			if (System.currentTimeMillis() > getStartTime() + duration) {
				bPlayer.addCooldown(this);
				deadProjectiles.put(i, locations.get(i));
				remove();
			}
		}
		
		for(Integer i : deadProjectiles.keySet()) {
			locations.remove(i);
			directions.remove(i);
			for (TempBlock tb : affectedBlocks.get(i))
				tb.revertBlock();
			affectedBlocks.remove(i);
		}
		
		deadProjectiles.clear();
	}
	
	public void drawCardioid(int id, Location loc, double radius) {
		double angle;
		int sign;
		double value;
		double a;
		double x;
		double y;
		double z;
		
		for (TempBlock tb : affectedBlocks.get(id))
			tb.revertBlock();
		affectedBlocks.get(id).clear();
		
		for (int i = 0; i <= 180; i += 1) {
			sign = 1;
			angle = Math.toRadians(i);
			value = radius * Math.cos(angle) * (1 - Math.cos(angle));
			a = loc.getYaw() - 90;
			if (a < 0) {
				a = -a;
				sign = -1;
			}
			a = Math.toRadians(a);
			x = Math.cos(a) * value * -1;
			y = radius * Math.sin(angle) * (1 - Math.cos(angle));
			z = Math.sin(a) * value * -1;
			loc.add(x, y, sign * z);
			
			TempBlock tb = new TempBlock(loc.getBlock(), Material.WATER);
			affectedBlocks.get(id).add(tb);
			
			loc.subtract(x, y, sign * z);
		}
	}

	public void setRadiusMap(int id, double radius) {
		radiusMap.put(id, radius);
	}
	
	public void setAffectedBlocks(int id, ArrayList<TempBlock> tempBlocks) {
		affectedBlocks.put(id, tempBlocks);
	}
	
	public ArrayList<TempBlock> getAffectedBlocks(int id) {
		return affectedBlocks.get(id);
	}
	
	public int getRemainingRings() {
		return this.remainingRings;
	}
	
	public void setRemainingRings(int remainingRings) {
		this.remainingRings = remainingRings;
	}
	
	public int getLastProjectileId() {
		return this.lastProjectileId;
	}
	
	public void setLastProjectileId(int id) {
		this.lastProjectileId = id;
	}
	
	public long getLastShotTime() {
		return this.lastShotTime;
	}
	
	public void setLastShotTime(long time) {
		this.lastShotTime = time;
	}
	
	public long getTimeBetweenShots() {
		return this.timeBetweenShots;
	}
	
	public HashMap<Integer, Location> getParticleLocations() {
		return this.locations;
	}
	
	public HashMap<Integer, Vector> getDirections() {
		return this.directions;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return origin;
	}

	@Override
	public String getName() {
		return "RazorRings";
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
		return new RazorRings(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("WaterManipulation", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Surge", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Surge", ClickType.SHIFT_DOWN));
		return combo;
	}

	@Override
	public String getAuthor() {
		return "Aztl & Hiro3";
	}

	@Override
	public String getVersion() {
		return Azutoru.az.version();
	}
	
	@Override
	public String getDescription() {
		return "Send forth sharp rings of water that can cut through certain materials and harm your enemies.";
	}
	
	@Override
	public String getInstructions() {
		return "WaterManipulation (Tap sneak) > WaterManipulation (Hold sneak) > Surge (Release sneak) > Surge (Hold sneak) > Surge (Click multiple times)";
	}
	
	@Override
	public void remove() {
		super.remove();
		for (ArrayList<TempBlock> tempBlocks : affectedBlocks.values()) {
			for (TempBlock tb : tempBlocks) {
				tb.revertBlock();
			}
		}
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