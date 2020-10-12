package me.aztl.azutoru.util;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.SubAbility;

import me.aztl.azutoru.Azutoru;

public abstract class GlassAbility extends EarthAbility implements SubAbility {

	public GlassAbility(Player player) {
		super(player);
	}
	
	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public Element getElement() {
		return Azutoru.az.getGlassElement();
	}
	
	public static boolean isGlass(Block block) {
		return block != null ? isGlass(block.getType()) : false;
	}
	
	public static boolean isGlass(Material material) {
		return getGlassBlocks().contains(material.toString());
	}
	
	public static ArrayList<String> getGlassBlocks() {
		ArrayList<String> glassBlocks = new ArrayList<String>();
		glassBlocks.add(Material.GLASS.toString());
		glassBlocks.add(Material.WHITE_STAINED_GLASS.toString());
		glassBlocks.add(Material.ORANGE_STAINED_GLASS.toString());
		glassBlocks.add(Material.MAGENTA_STAINED_GLASS.toString());
		glassBlocks.add(Material.LIGHT_BLUE_STAINED_GLASS.toString());
		glassBlocks.add(Material.YELLOW_STAINED_GLASS.toString());
		glassBlocks.add(Material.LIME_STAINED_GLASS.toString());
		glassBlocks.add(Material.PINK_STAINED_GLASS.toString());
		glassBlocks.add(Material.GRAY_STAINED_GLASS.toString());
		glassBlocks.add(Material.LIGHT_GRAY_STAINED_GLASS.toString());
		glassBlocks.add(Material.CYAN_STAINED_GLASS.toString());
		glassBlocks.add(Material.PURPLE_STAINED_GLASS.toString());
		glassBlocks.add(Material.BLUE_STAINED_GLASS.toString());
		glassBlocks.add(Material.BROWN_STAINED_GLASS.toString());
		glassBlocks.add(Material.GREEN_STAINED_GLASS.toString());
		glassBlocks.add(Material.RED_STAINED_GLASS.toString());
		glassBlocks.add(Material.BLACK_STAINED_GLASS.toString());
		glassBlocks.add(Material.GLASS_PANE.toString());
		glassBlocks.add(Material.WHITE_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.ORANGE_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.MAGENTA_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.YELLOW_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.LIME_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.PINK_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.GRAY_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.CYAN_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.PURPLE_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.BLUE_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.BROWN_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.GREEN_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.RED_STAINED_GLASS_PANE.toString());
		glassBlocks.add(Material.BLACK_STAINED_GLASS_PANE.toString());
		
		return glassBlocks;
	}
	
	public static void playGlassbendingSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 5, 1);
	}

}
