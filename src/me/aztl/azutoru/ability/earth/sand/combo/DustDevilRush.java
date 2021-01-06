package me.aztl.azutoru.ability.earth.sand.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;

public class DustDevilRush extends SandAbility implements AddonAbility, ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private RemovalPolicy policy;
	
	public DustDevilRush(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevilRush.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Earth.DustDevilRush.Duration");
		
		policy = Policies.builder()
					.add(new ExpirationPolicy(duration)).build();
		
		if (hasAbility(player, DustDevil.class))
			start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
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
		boolean enabled = Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustDevilRush.Enabled")
				&& Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustDevil.Enabled");
		return enabled;
	}

}
