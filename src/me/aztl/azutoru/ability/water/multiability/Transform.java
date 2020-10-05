package me.aztl.azutoru.ability.water.multiability;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave.AbilityType;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.water.ice.combo.IceShots;

public class Transform extends WaterAbility implements AddonAbility, MultiAbility {
	
	public Transform(Player player) {
		super(player);
		
		Transform transform = getAbility(player, Transform.class);
		if (transform != null) {
			switch (player.getInventory().getHeldItemSlot()) {
			case 0:
				if (player.isSneaking()) {
					new Torrent(player);
					transform.remove();
				}
				break;
			case 1:
				if (player.isSneaking()) {
					new WaterSpoutWave(player, AbilityType.SHIFT);
					transform.remove();
				}
				break;
			case 2:
				new SurgeWave(player);
				transform.remove();
				break;
			case 3:
				if (player.isSneaking()) {
					new OctopusForm(player);
					transform.remove();
				}
				break;
			case 4:
				new WaterArms(player);
				transform.remove();
				break;
			case 5:
				if (player.isSneaking()) {
					new IceBullet(player);
					transform.remove();
				}
				break;
			case 6:
				if (player.isSneaking()) {
					new IceShots(player);
					transform.remove();
				}
				break;
			case 7:
				remove();
			}
		}
		MultiAbilityManager.bindMultiAbility(player, "Transform");
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
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
		return "Transform";
	}
	
	@Override
	public boolean isHiddenAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		final ArrayList<MultiAbilityInfoSub> subs = new ArrayList<>();
		subs.add(new MultiAbilityInfoSub("Torrent", Element.WATER));
		subs.add(new MultiAbilityInfoSub("WaterWave", Element.WATER));
		subs.add(new MultiAbilityInfoSub("SurgeWave", Element.WATER));
		subs.add(new MultiAbilityInfoSub("OctopusForm", Element.WATER));
		subs.add(new MultiAbilityInfoSub("WaterArms", Element.WATER));
		subs.add(new MultiAbilityInfoSub("IceBullet", Element.ICE));
		subs.add(new MultiAbilityInfoSub("IceShots", Element.ICE));
		subs.add(new MultiAbilityInfoSub("Evaporate", Element.WATER));
		return subs;
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
		return false;
	}

}
