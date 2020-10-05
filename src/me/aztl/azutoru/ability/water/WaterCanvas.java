package me.aztl.azutoru.ability.water;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;

public class WaterCanvas extends WaterAbility implements AddonAbility {
	
	private static enum AnimateState {
		TOWARD_TOPBLOCK, ICED;
	}
	
	private long cooldown;
	private long duration;
	private double selectRange;
	private Location startLocation;
	private Location topLoc, midLoc;
	private Block sourceBlock;
	private List<Double> viableSourceDistances;
	private Block topBlock;
	private AnimateState animation;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	private List<Block> newBlocks;
	private boolean clicked;
	
	public WaterCanvas(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		selectRange = Azutoru.az.getConfig().getDouble("Abilities.Water.WaterCanvas.SelectRange");
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) selectRange);
		topLoc = topBlock.getLocation().add(0, 1, 0);
		viableSourceDistances = new ArrayList<>();
		affectedBlocks = new ConcurrentHashMap<>();
		startLocation = player.getLocation().clone();
		
		if (getSource()) {
			animation = AnimateState.TOWARD_TOPBLOCK;
			start();
		}
	}
	
	public boolean getSource() {
		for (Block b : GeneralMethods.getBlocksAroundPoint(startLocation, selectRange)) {
			if (WaterAbility.isWaterbendable(b.getType()) 
					&& GeneralMethods.isAdjacentToThreeOrMoreSources(b)
					&& !GeneralMethods.isRegionProtectedFromBuild(this, b.getLocation())
					&& isTransparent(b.getRelative(BlockFace.UP))) {
				viableSourceDistances.add(b.getLocation().distanceSquared(startLocation));
				Collections.sort(viableSourceDistances);
				if (b.getLocation().distanceSquared(startLocation) == viableSourceDistances.get(0)) {
					sourceBlock = b;
					midLoc = sourceBlock.getLocation();
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			bPlayer.addCooldown(this);
			return;
		}
		
		if (clicked) {
			animation = AnimateState.ICED;
		}
		
		if (animation == AnimateState.TOWARD_TOPBLOCK) {
			if (!player.isSneaking()) {
				animation = null;
			}
			
			newBlocks = new ArrayList<>();
			revertBlocks();
			midLoc.add(GeneralMethods.getDirection(midLoc, topLoc));
			Location rLoc = GeneralMethods.getRightSide(midLoc, 1);
			Location rrLoc = GeneralMethods.getRightSide(rLoc, 1);
			Location lLoc = GeneralMethods.getLeftSide(midLoc, 1);
			Location llLoc = GeneralMethods.getLeftSide(lLoc, 1);
			rLoc.add(GeneralMethods.getDirection(rLoc, topLoc));
			rrLoc.add(GeneralMethods.getDirection(rrLoc, topLoc));
			lLoc.add(GeneralMethods.getDirection(lLoc, topLoc));
			llLoc.add(GeneralMethods.getDirection(llLoc, topLoc));
			newBlocks.add(midLoc.getBlock());
			newBlocks.add(rLoc.getBlock());
			newBlocks.add(rrLoc.getBlock());
			newBlocks.add(lLoc.getBlock());
			newBlocks.add(llLoc.getBlock());
			for (Block block : newBlocks) {
				if (isTransparent(block)) {
					createBlock(block, Material.WATER);
				} else {
					for (int i = 0; i < 4; i++) {
						if (isTransparent(block.getRelative(BlockFace.UP, i))) {
							createBlock(block, Material.WATER);
							break;
						}
					}
				}
			}
			if (midLoc.distance(topLoc) <= 1) {
				animation = null;
			}
		} else if (animation == AnimateState.ICED) {
			for (Block block : newBlocks) {
				if (block.getType() == Material.WATER) {
					block.setType(Material.ICE);
				}
			}
			bPlayer.addCooldown(this);
		}
	}
	
	public void createBlock(Block block, Material material) {
		affectedBlocks.put(block, new TempBlock(block, material));
	}
	
	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertBlocks();
		affectedBlocks.clear();
		return;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return midLoc;
	}

	@Override
	public String getName() {
		return "WaterCanvas";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a waterbender to call nearby water blocks to form a floor of water that can be used for fall damage prevention or sourcing.";
	}
	
	@Override
	public String getInstructions() {
		return "Hold sneak to call water towards you. Left-click to turn it to ice.";
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
		return false;
	}

}
