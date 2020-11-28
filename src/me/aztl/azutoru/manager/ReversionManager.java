package me.aztl.azutoru.manager;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;

import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.combo.EarthTent;

public class ReversionManager implements Runnable {
	
	Azutoru plugin;
	
	public ReversionManager(Azutoru plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		Set<Block> removal = new HashSet<>();
		for (Block block : RaiseEarth.getAffectedBlocks().keySet()) {
			if (ElementalAbility.isAir(block.getType())) {
				removal.add(block);
			}
		}
		for (Block block : removal) {
			RaiseEarth.getAffectedBlocks().remove(block);
		}
		removal.clear();
		
		for (TempBlock tb : EarthTent.getAffectedBlocks()) {
			Block block = tb.getBlock();
			if (ElementalAbility.isAir(block.getType())) {
				removal.add(block);
			}
		}
		for (Block block : removal) {
			EarthTent.removeBlock(TempBlock.get(block));
		}
		removal.clear();
	}
}
