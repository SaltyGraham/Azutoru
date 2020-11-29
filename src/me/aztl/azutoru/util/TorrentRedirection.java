package me.aztl.azutoru.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.waterbending.Torrent;

import me.aztl.azutoru.Azutoru;

public class TorrentRedirection {
	
	public static double detectionRadius = Azutoru.az.getConfig().getDouble("Properties.TorrentRedirection.DetectionRadius");
	public static double detectionRange = Azutoru.az.getConfig().getDouble("Properties.TorrentRedirection.DetectionRange");

	public static boolean canRedirect(Player player) {
		if (CoreAbility.hasAbility(player, Torrent.class)) {
			return false;
		}
		if (BendingPlayer.getBendingPlayer(player) == null 
				|| !BendingPlayer.getBendingPlayer(player).canBendIgnoreCooldowns(CoreAbility.getAbility(Torrent.class))) {
			return false;
		}
		
		for (Torrent to : CoreAbility.getAbilities(Torrent.class)) {
			if (!to.getLocation().getWorld().equals(player.getLocation().getWorld())) {
				continue;
			}
			if (!to.isLaunching()) {
				continue;
			}
			
			Location tLoc = to.getLocation();
			Location pLoc = player.getEyeLocation();
			pLoc.add(pLoc.getDirection().multiply(detectionRange * 0.5));
			
			if (tLoc.distance(pLoc) <= detectionRadius) {
				to.remove();
				return true;
			}
		}
		
		return false;
	}
	
}
