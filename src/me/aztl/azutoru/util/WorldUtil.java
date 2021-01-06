package me.aztl.azutoru.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;

public class WorldUtil {
	
	private static List<Material> ignoredPlants = Arrays.asList(Material.ACACIA_SAPLING, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.ORANGE_TULIP, Material.PINK_TULIP, Material.RED_TULIP, Material.WHITE_TULIP, Material.ROSE_BUSH, Material.BLUE_ORCHID, Material.ALLIUM, Material.DANDELION, Material.LILAC, Material.OXEYE_DAISY, Material.AZURE_BLUET, Material.PEONY, Material.SUNFLOWER, Material.POPPY, Material.FERN, Material.DEAD_BUSH, Material.LARGE_FERN, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.CORNFLOWER, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.WARPED_ROOTS, Material.CRIMSON_ROOTS, Material.NETHER_SPROUTS, Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES, Material.GRASS, Material.TALL_GRASS, Material.LILY_PAD);
	
	public static void displayWaterBubble(Location loc) {
		ParticleEffect.WATER_BUBBLE.display(loc, 1, 0.5, 0.5, 0.5);
	}
	
	public static Block getTopBlock(Location loc, BlockFace face, int range) {
		Block currentBlock = loc.getBlock();
		int i = 0;
		
		while (!ElementalAbility.isAir(currentBlock.getType()) && i < range) {
			Block b = currentBlock.getRelative(face, i);
			if (ElementalAbility.isAir(b.getType())) {
				return currentBlock;
			}
			currentBlock = b;
			i++;
		}
		
		while (ElementalAbility.isAir(currentBlock.getType()) && i < range) {
			Block b = currentBlock.getRelative(face, i);
			if (!ElementalAbility.isAir(b.getType())) {
				return b;
			}
			i++;
		}
		
		return currentBlock;
	}
	
	public static boolean isDust(Block block) {
		return block != null ? isDust(block.getType()) : false;
	}
	
	public static boolean isDust(Material material) {
		return Azutoru.az.getConfig().getStringList("Properties.Earth.DustBlocks").contains(material.toString());
	}
	
	public static boolean isIgnoredPlant(Block block) {
		return block != null ? isIgnoredPlant(block.getType()) : false;
	}
	
	public static boolean isIgnoredPlant(Material material) {
		return ignoredPlants.contains(material);
	}
	
	public static boolean isNonParryableMob(Entity e) {
		return e != null ? isNonParryableMob(e.getType()) : false;
	}
	
	public static boolean isNonParryableMob(EntityType eType) {
		return Azutoru.az.getConfig().getStringList("Abilities.Chi.Parry.NonParryableMobs").contains(eType.toString());
	}
	
    public static void revertBlocks(ConcurrentHashMap<Block, TempBlock> affectedBlocks) {
    	Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
    }

}
