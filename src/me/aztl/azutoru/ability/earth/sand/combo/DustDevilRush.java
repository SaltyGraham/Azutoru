package me.aztl.azutoru.ability.earth.sand.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.sand.DustDevil;

public class DustDevilRush extends SandAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long duration;
	
	public DustDevilRush(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevilRush.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevilRush.Duration");
		
		if (CoreAbility.hasAbility(player, DustDevil.class)) {
			start();
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		return;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "DustDevilRush";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a sandbender to increase the speed on their DustDevil for a period of time.";
	}
	
	@Override
	public String getInstructions() {
		return "DustDevil (Tap sneak) > DustDevil (Tap sneak) > Shockwave (Left-click). To end the combo, left-click with DustDevil.";
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
	public Object createNewComboInstance(Player player) {
		return new DustDevilRush(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("DustDevil", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));
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
		return true;
	}

}
