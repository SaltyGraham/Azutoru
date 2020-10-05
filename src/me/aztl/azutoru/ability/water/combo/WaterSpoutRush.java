package me.aztl.azutoru.ability.water.combo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import me.aztl.azutoru.Azutoru;

public class WaterSpoutRush extends WaterAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	
	private boolean canBendOnPackedIce, useParticles, useBlockSpiral;
	private int angle;
	private long time, interval;
	private double rotation, height, maxHeight;
	private Block base;
	private TempBlock baseBlock;
	private float initFlySpeed, speedModifier;
	private static Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private List<TempBlock> blocks = new ArrayList<TempBlock>();
	
	public WaterSpoutRush(Player player) {
		super(player);
		
		WaterSpout spout = getAbility(player, WaterSpout.class);
		if (spout == null) {
			return;
		} else {
			spout.remove();
		}
		
		canBendOnPackedIce = ProjectKorra.plugin.getConfig().getStringList("Properties.Water.IceBlocks").contains(Material.PACKED_ICE.toString());
		useParticles = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.WaterSpout.Particles");
		useBlockSpiral = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.WaterSpout.BlockSpiral");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSpoutRush.Cooldown");
		height = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterSpout.Height");
		interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.WaterSpout.Interval") / 8;
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.WaterSpoutRush.Duration");
		maxHeight = getNightFactor(height);
		initFlySpeed = player.getFlySpeed();
		speedModifier = 2;
		
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) -this.getNightFactor(this.height), (int) -this.getNightFactor(this.height));
		if (topBlock == null) {
			topBlock = player.getLocation().getBlock();
		}
		
		if (!isWater(topBlock) && !isIcebendable(topBlock) && !isSnow(topBlock)) {
			return;
		} else if (topBlock.getType() != Material.PACKED_ICE && !canBendOnPackedIce) {
			return;
		}
		
		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(topBlock.getLocation(), heightRemoveThreshold)) {
			return;
		}
		
		flightHandler.createInstance(player, getName());
		allowFlight();
		spoutableWaterHeight(player.getLocation());
		start();
	}
	
	@Override
	public void progress() {
		for (TempBlock tb : blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		} else if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		} else {
			blocks.clear();
			player.setFallDistance(0);
			player.setFlySpeed(initFlySpeed * speedModifier);
			player.getVelocity().setY(0.001);
			if ((new Random()).nextInt(10) == 0) {
				playWaterbendingSound(player.getLocation());
			}
			
			Location location = player.getLocation().clone().add(0, 0.2, 0);
			Block block = location.clone().getBlock();
			double height = spoutableWaterHeight(location);
			
			if (height != -1) {
				location = base.getLocation();
				double heightRemoveThreshold = 2;
				if (!isWithinMaxSpoutHeight(location, heightRemoveThreshold)) {
					bPlayer.addCooldown(this);
					remove();
					return;
				}
				for (int i = 1; i <= height; i++) {
					block = location.clone().add(0, i, 0).getBlock();
					
					if (!TempBlock.isTempBlock(block)) {
						blocks.add(new TempBlock(block, Material.WATER));
						AFFECTED_BLOCKS.put(block, block);
					}
					rotateParticles(block);
				}
				
				displayWaterSpiral(location.clone().add(0.5, 0, 0.5));
				if (player.getLocation().getBlockY() > block.getY()) {
					removeFlight();
				} else {
					allowFlight();
				}
			} else {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		}
	}
	
	private void displayWaterSpiral(Location location) {
		if (!useBlockSpiral) {
			return;
		}
		
		double maxHeight = player.getLocation().getY() - location.getY() - 0.5;
		double height = 0;
		rotation += 0.4;
		int i = 0;
		
		while (height < maxHeight) {
			i += 20;
			height += 0.4;
			double angle = (i * Math.PI / 180);
			double x = 1 * Math.cos(angle + rotation);
			double z = 1 * Math.sin(angle + rotation);
			
			Location loc = location.clone().getBlock().getLocation().add(0.5, 0.5, 0.5);
			loc.add(x, height, z);
			
			Block block = loc.getBlock();
			if ((!TempBlock.isTempBlock(block)) && (ElementalAbility.isAir(block.getType()) || !GeneralMethods.isSolid(block))) {
				blocks.add(new TempBlock(block, GeneralMethods.getWaterData(7)));
				AFFECTED_BLOCKS.put(block, block);
			}
		}
	}
	
	private void allowFlight() {
		if (!player.getAllowFlight()) {
			player.setAllowFlight(true);
		}
		if (!player.isFlying()) {
			player.setFlying(true);
		}
	}
	
	private void removeFlight() {
		if (player.getAllowFlight()) {
			player.setAllowFlight(false);
		}
		if (player.isFlying()) {
			player.setFlying(false);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertBaseBlock();
		for (final TempBlock tb : blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		flightHandler.removeInstance(player, getName());
		player.setFlySpeed(initFlySpeed);
		removeFlight();
		new WaterSpout(player);
	}
	
	public void revertBaseBlock() {
		if (baseBlock != null) {
			baseBlock.revertBlock();
			baseBlock = null;
		}
	}
	
	private boolean isWithinMaxSpoutHeight(Location baseBlockLocation, double threshold) {
		if (baseBlockLocation == null) {
			return false;
		}
		double playerHeight = player.getLocation().getY();
		if (playerHeight > baseBlockLocation.getY() + maxHeight + threshold) {
			return false;
		}
		return true;
	}
	
	public void rotateParticles(Block block) {
		if (!useParticles) {
			return;
		}
		
		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();
			
			Location location = block.getLocation();
			Location playerLoc = player.getLocation();
			
			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());
			
			double dy = playerLoc.getY() - block.getY();
			if (dy > height) {
				dy = height;
			}
			
			double[] directions = { -0.5, 0.325, 0.25, 0.125, 0.0, 0.125, 0.25, 0.325, 0.5 };
			int index = angle;
			angle++;
			if (angle >= directions.length) {
				angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}
				
				Location effectLoc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				ParticleEffect.WATER_SPLASH.display(effectLoc2, 5, directions[index], directions[index], directions[index]);
			}
		}
	}
	
	private double spoutableWaterHeight(Location location) {
		double newHeight = height;
		if (isNight(player.getWorld())) {
			newHeight = getNightFactor(newHeight);
		}
		
		maxHeight = newHeight + 5;
		Block blocki;
		
		for (int i = 0; i < maxHeight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
				return -1;
			}
			
			if (TempBlock.get(blocki) == null || !blocks.contains(TempBlock.get(blocki))) {
				if (isWater(blocki)) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
					}
					
					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}
				
				if (isIcebendable(blocki) || isSnow(blocki)) {
					if (isIcebendable(blocki)) {
						if (blocki.getType() == Material.PACKED_ICE && !canBendOnPackedIce) {
							remove();
							return -1;
						}
					}
					
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
						baseBlock = new TempBlock(blocki, Material.WATER);
					}
					
					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}
				
				if (!ElementalAbility.isAir(blocki.getType()) && (!isPlant(blocki) || !bPlayer.canPlantbend())) {
					revertBaseBlock();
					return -1;
				}
			}
		}
		return -1;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public String getName() {
		return "WaterSpoutRush";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a waterbender to accelerate their WaterSpout and move much faster than normal for a brief period of time.";
	}
	
	@Override
	public String getInstructions() {
		return "WaterSpout (Tap sneak) > WaterSpout (Tap sneak) > Surge (Left-click)";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new WaterSpoutRush(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Surge", ClickType.LEFT_CLICK));
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
