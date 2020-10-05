package me.aztl.azutoru.ability.earth.lava.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;

public class LavaWalk extends LavaAbility implements AddonAbility, PassiveAbility {

	private int radius;
	private long revertTime;
	private boolean canBendTempLava;
	
	private Set<TempBlock> COOLED_BLOCKS = new HashSet<>();
	
	public LavaWalk(Player player) {
		super(player);
		
		radius = Azutoru.az.getConfig().getInt("Abilities.Earth.LavaWalk.Radius");
		revertTime = Azutoru.az.getConfig().getLong("Abilities.Earth.LavaWalk.RevertTime");
		canBendTempLava = Azutoru.az.getConfig().getBoolean("Abilities.Earth.LavaWalk.CanBendTempLava");
		
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (bPlayer == null) {
			return;
		}
		
		for (Block affectedBlock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), radius)) {
			if ((EarthAbility.isLava(affectedBlock) && !TempBlock.isTempBlock(affectedBlock) || (EarthAbility.isLava(affectedBlock)) && TempBlock.isTempBlock(affectedBlock) && canBendTempLava)) {
				TempBlock tb = new TempBlock(affectedBlock, Material.STONE);
				tb.setRevertTime(revertTime);
				tb.setRevertTask(() -> COOLED_BLOCKS.remove(tb));
			}
		}
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
		return "Walk close to a lava pool.";
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
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
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
		return true;
	}

}
