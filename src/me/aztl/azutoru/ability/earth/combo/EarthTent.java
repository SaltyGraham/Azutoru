package me.aztl.azutoru.ability.earth.combo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;

public class EarthTent extends EarthAbility implements AddonAbility, ComboAbility {
	
	private long cooldown, revertTime;
	private int height, width, length;
	
	private Location location;
	private static Set<TempBlock> affectedBlocks = new HashSet<>();
	
	public EarthTent(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.EarthTent.Cooldown");
		revertTime = Azutoru.az.getConfig().getLong("Abilities.Earth.EarthTent.RevertTime");
		height = Azutoru.az.getConfig().getInt("Abilities.Earth.EarthTent.Height");
		width = Azutoru.az.getConfig().getInt("Abilities.Earth.EarthTent.Width") / 2;
		length = Azutoru.az.getConfig().getInt("Abilities.Earth.EarthTent.Length");
		
		location = player.getLocation();
		
		if (isEarthbendable(GeneralMethods.getTopBlock(player.getLocation(), 2))) {
			start();
		}
	}

	@Override
	public void progress() {
		setupBlocks();
		playEarthbendingSound(player.getLocation());
		bPlayer.addCooldown(this);
		remove();
	}
	
	private void setupBlocks() {
		BlockFace face = AzutoruMethods.getCardinalDirection(location.getYaw());
		BlockFace[] faces = { face, face.getOppositeFace() };
		
		Block left = location.getBlock().getRelative(getLeft(face), width);
		Block right = location.getBlock().getRelative(getRight(face), width);
		
		Material matLeft = GeneralMethods.getTopBlock(left.getLocation(), 2).getType();
		Material matRight = GeneralMethods.getTopBlock(right.getLocation(), 2).getType();
		
		if (!isEarth(matLeft)) matLeft = Material.DIRT;
		if (!isEarth(matRight)) matRight = Material.DIRT;
		
		List<Block> lefts = new ArrayList<>();
		List<Block> rights = new ArrayList<>();
		
		lefts.add(left);
		for (int i = 0; i < height; i++) {
			left = left.getRelative(BlockFace.UP).getRelative(getRight(face));
			lefts.add(left);
		}
		
		rights.add(right);
		for (int i = 0; i < height - 1; i++) {
			right = right.getRelative(BlockFace.UP).getRelative(getLeft(face));
			rights.add(right);
		}
		
		List<Block> addLeft = new ArrayList<>();
		for (Block b : lefts) {
			for (BlockFace f : faces) {
				for (int depth = 1; depth <= length / 2; depth++) {
					addLeft.add(b.getRelative(f, depth));
				}
			}
		}
		lefts.addAll(addLeft);
		
		List<Block> addRight = new ArrayList<>();
		for (Block b : rights) {
			for (BlockFace f : faces) {
				for (int depth = 1; depth <= length / 2; depth++) {
					addRight.add(b.getRelative(f, depth));
				}
			}
		}
		rights.addAll(addRight);
		
		createTempBlocks(lefts, matLeft);
		createTempBlocks(rights, matRight);
	}
	
	private void createTempBlocks(List<Block> blocks, Material material) {
		for (Block b : blocks) {
			if (isAir(b.getType())) {
				TempBlock tb = new TempBlock(b, material);
				tb.setRevertTime(revertTime);
				addBlock(tb);
				tb.setRevertTask(() -> removeBlock(tb));
			}
		}
	}
	
	public static void addBlock(TempBlock tempBlock) {
		affectedBlocks.add(tempBlock);
		addEarthbendableTempBlock(tempBlock);
	}
	
	public static void removeBlock(TempBlock tempBlock) {
		affectedBlocks.remove(tempBlock);
		removeEarthbendableTempBlock(tempBlock);
	}
	
	public static boolean isEarthTentBlock(Block block) {
		return TempBlock.isTempBlock(block) && affectedBlocks.contains(TempBlock.get(block));
	}
	
	public static boolean isEarthTentBlock(TempBlock tempBlock) {
		return affectedBlocks.contains(tempBlock);
	}
	
	public static Set<TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}
	
	private BlockFace getLeft(BlockFace face) {
		return getRight(face).getOppositeFace();
	}
	
	private BlockFace getRight(BlockFace face) {
		switch (face) {
		case NORTH:
			return BlockFace.EAST;
		case EAST:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.WEST;
		case WEST:
			return BlockFace.NORTH;
		default:
			return face;
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "EarthTent";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows an earthbender to construct a tent around them using nearby earth blocks. It is useful for defense and temporary shelter.";
	}
	
	@Override
	public String getInstructions() {
		return "EarthRidge (Left-click) > RaiseEarth (Right-click on a block)";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new EarthTent(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("EarthRidge", ClickType.LEFT_CLICK));
		combo.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.EarthTent.Enabled")
				&& Azutoru.az.getConfig().getBoolean("Abilities.Earth.EarthRidge.Enabled");
	}

}
