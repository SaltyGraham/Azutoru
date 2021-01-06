package me.aztl.azutoru.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;

public class PlayerUtil {
	
	public static void allowFlight(Player player) {
		player.setAllowFlight(true);
		player.setFlying(true);
	}
	
	public static void removeFlight(Player player, boolean allowFlight, boolean fly) {
		player.setAllowFlight(allowFlight);
		player.setFlying(fly);
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
	
	public static boolean isOnGround(Player player) {
    	Block b = player.getLocation().subtract(0, 0.1, 0).getBlock();
    	if (GeneralMethods.isSolid(b) && !player.getLocation().getBlock().isLiquid()) {
    		return true;
    	}
    	return false;
    }

}
