package me.aztl.azutoru.util;

import java.util.Arrays;

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
		return Arrays.asList(Material.values()).stream().anyMatch(m -> m.toString().contains("GLASS"));
	}
	
	public static void playGlassbendingSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 5, 1);
	}

}
