package me.aztl.azutoru.ability.earth.lava.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;

public class LavaWalk extends LavaAbility implements AddonAbility, PassiveAbility {

	@Attribute(Attribute.RADIUS)
	private int radius;
	private boolean canBendTempLava;
	@Attribute(Attribute.RANGE)
	private double range;
	
	private World world;
	private Set<TempBlock> affectedBlocks = new HashSet<>();
	private boolean active;
	
	public LavaWalk(Player player) {
		super(player);
		
		FileConfiguration c = Azutoru.az.getConfig();
		radius = c.getInt("Abilities.Earth.LavaWalk.Radius");
		canBendTempLava = c.getBoolean("Abilities.Earth.LavaWalk.CanBendTempLava");
		range = c.getDouble("Abilities.Earth.LavaWalk.Range");
		
		world = player.getWorld();
		active = true;
	}

	@Override
	public void progress() {
		if (!active || !bPlayer.canUsePassive(this) || !bPlayer.canBendPassive(this)) {
			if (!affectedBlocks.isEmpty())
				revertBlocks();
			return;
		}
		
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		for (Block affectedBlock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), radius)) {
			if ((isLava(affectedBlock) && !TempBlock.isTempBlock(affectedBlock)) 
					|| (isLava(affectedBlock) && TempBlock.isTempBlock(affectedBlock) && canBendTempLava)) {
				TempBlock tb = new TempBlock(affectedBlock, Material.STONE);
				affectedBlocks.add(tb);
			}
		}
		
		if (!player.getWorld().equals(world)) {
			revertBlocks();
			world = player.getWorld();
		}
		
		affectedBlocks.stream()
			.filter(tb -> tb.getBlock().getLocation().distanceSquared(player.getLocation()) > range * range)
			.forEach(tb -> tb.revertBlock());
	}
	
	public void revertBlocks() {
		affectedBlocks.forEach(tb -> tb.revertBlock());
		affectedBlocks.clear();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		if (!active)
			revertBlocks();
		this.active = active;
	}
	
	public static void toggle(Player player) {
		LavaWalk lw = getAbility(player, LavaWalk.class);
		if (lw.active) {
			lw.setActive(false);
			player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now disabled.");
		} else {
			lw.setActive(true);
			player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now enabled.");
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertBlocks();
	}
	
	public Set<TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "LavaWalk";
	}
	
	@Override
	public String getDescription() {
		return "This passive allows lavabenders to automatically cool lava as they walk near it, effectively allowing them to cross large pools of lava with ease.";
	}
	
	@Override
	public String getInstructions() {
		return "Walk close to a lava pool. Right-click with LavaFlow with an empty hand to toggle LavaWalk.";
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.LavaWalk.Enabled");
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

}
