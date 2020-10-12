package me.aztl.azutoru;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class AzutoruMethods {

	private Azutoru plugin;
	
	public AzutoruMethods(Azutoru plugin) {
		this.plugin = plugin;
	}
	
	public Azutoru getPlugin() {
		return plugin;
	}
	
	public static boolean isDust(Block block) {
		return block != null ? isDust(block.getType()) : false;
	}
	
	public static boolean isDust(Material material) {
		return Azutoru.az.getConfig().getStringList("Properties.Earth.DustBlocks").contains(material.toString());
	}
	
	/*
	 * "Ignored" plants are plants that DustDevil skips over while finding its top block.
	 * This is so DustDevil doesn't cancel when you go over a flower or something,
	 * and also so that the block data for DustDevil doesn't use a plant.
	 */
	public static boolean isIgnoredPlant(Block block) {
		return block != null ? isIgnoredPlant(block.getType()) : false;
	}
	
	public static boolean isIgnoredPlant(Material material) {
		ArrayList<String> ignoredPlants = new ArrayList<String>();
		ignoredPlants.add(Material.ACACIA_SAPLING.toString());
		ignoredPlants.add(Material.BIRCH_SAPLING.toString());
		ignoredPlants.add(Material.DARK_OAK_SAPLING.toString());
		ignoredPlants.add(Material.JUNGLE_SAPLING.toString());
		ignoredPlants.add(Material.OAK_SAPLING.toString());
		ignoredPlants.add(Material.SPRUCE_SAPLING.toString());
		ignoredPlants.add(Material.ORANGE_TULIP.toString());
		ignoredPlants.add(Material.PINK_TULIP.toString());
		ignoredPlants.add(Material.RED_TULIP.toString());
		ignoredPlants.add(Material.WHITE_TULIP.toString());
		ignoredPlants.add(Material.ROSE_BUSH.toString());
		ignoredPlants.add(Material.BLUE_ORCHID.toString());
		ignoredPlants.add(Material.ALLIUM.toString());
		ignoredPlants.add(Material.DANDELION.toString());
		ignoredPlants.add(Material.LILAC.toString());
		ignoredPlants.add(Material.OXEYE_DAISY.toString());
		ignoredPlants.add(Material.AZURE_BLUET.toString());
		ignoredPlants.add(Material.PEONY.toString());
		ignoredPlants.add(Material.SUNFLOWER.toString());
		ignoredPlants.add(Material.POPPY.toString());
		ignoredPlants.add(Material.FERN.toString());
		ignoredPlants.add(Material.LILY_OF_THE_VALLEY.toString());
		ignoredPlants.add(Material.WITHER_ROSE.toString());
		ignoredPlants.add(Material.CORNFLOWER.toString());
		ignoredPlants.add(Material.LARGE_FERN.toString());
		ignoredPlants.add(Material.RED_MUSHROOM.toString());
		ignoredPlants.add(Material.BROWN_MUSHROOM.toString());
		ignoredPlants.add(Material.WARPED_ROOTS.toString());
		ignoredPlants.add(Material.CRIMSON_ROOTS.toString());
		ignoredPlants.add(Material.NETHER_SPROUTS.toString());
		ignoredPlants.add(Material.WHEAT.toString());
		ignoredPlants.add(Material.BEETROOTS.toString());
		ignoredPlants.add(Material.CARROTS.toString());
		ignoredPlants.add(Material.POTATOES.toString());
		ignoredPlants.add(Material.GRASS.toString());
		ignoredPlants.add(Material.TALL_GRASS.toString());
		ignoredPlants.add(Material.LILY_PAD.toString());
		
		return ignoredPlants.contains(material.toString());
	}
	
	public static List<EntityType> getNonParryableMobs() {
		List<EntityType> nonParryables = new ArrayList<>();
		nonParryables.add(EntityType.CREEPER);
		nonParryables.add(EntityType.ELDER_GUARDIAN);
		nonParryables.add(EntityType.ENDER_DRAGON);
		nonParryables.add(EntityType.GUARDIAN);
		nonParryables.add(EntityType.HOGLIN);
		nonParryables.add(EntityType.IRON_GOLEM);
		nonParryables.add(EntityType.PHANTOM);
		nonParryables.add(EntityType.POLAR_BEAR);
		nonParryables.add(EntityType.RAVAGER);
		nonParryables.add(EntityType.SILVERFISH);
		nonParryables.add(EntityType.WITHER);
		nonParryables.add(EntityType.WOLF);
		return nonParryables;
	}
	
	public static enum Hand {
		RIGHT, LEFT;
	}
	
	public static Location getHandPos(Player player, Hand hand) {
		if (hand == Hand.RIGHT) {
			Location loc = GeneralMethods.getRightSide(player.getLocation(), 0.5).add(0, 1, 0).add(player.getEyeLocation().getDirection().multiply(0.6));
			loc.setPitch(0);
			return loc;
		} else if (hand == Hand.LEFT) {
			Location loc = GeneralMethods.getLeftSide(player.getLocation(), 0.5).add(0, 1, 0).add(player.getEyeLocation().getDirection().multiply(0.6));
			loc.setPitch(0);
			return loc;
		}
		return null;
	}
	
	public static void displayWaterBubble(Location loc) {
		ParticleEffect.WATER_BUBBLE.display(loc, 1, 0.5, 0.5, 0.5);
	}
	
	public static List<Location> getLinePoints(Player player, Location startLoc, Location endLoc, int steps) {
		List<Location> locations = new ArrayList<Location>();
		Location diff = endLoc.clone().subtract(startLoc);
		double diffX = diff.getX() / steps;
		double diffY = diff.getY() / steps;
		double diffZ = diff.getZ() / steps;
		Location loc = startLoc.clone();
		for (int i = 0; i < steps; i++) {
			loc.add(diffX, diffY, diffZ);
			loc.setDirection(loc.clone().subtract(player.getEyeLocation()).toVector().normalize());
			locations.add(loc.clone());
		}
		return locations;
	}
	
	public static Vector rotateAroundAxisX(Vector v, double cos, double sin) {
		double y = v.getY() * cos - v.getZ() * sin;
		double z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}
	
    public static Vector rotateAroundAxisY(Vector v, double cos, double sin) {
        double x = v.getX() * cos + v.getZ() * sin;
        double z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }
    
    public static Vector rotateAroundAxisZ(Vector v, double cos, double sin) {
        double x = v.getX() * cos - v.getY() * sin;
        double y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }
    
    public static boolean isOnGround(Player player) {
    	Block b = player.getLocation().subtract(0, 0.2, 0).getBlock();
    	if (GeneralMethods.isSolid(b) && !player.getLocation().getBlock().isLiquid()) {
    		return true;
    	}
    	return false;
    }
    
    public static void revertBlocks(ConcurrentHashMap<Block, TempBlock> affectedBlocks) {
    	Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
    }
    
	public static boolean canPlaceWaterBlock(Block block) {
		return ElementalAbility.isWater(block) || ElementalAbility.isIce(block) || ElementalAbility.isAir(block.getType());
	}
	
}
