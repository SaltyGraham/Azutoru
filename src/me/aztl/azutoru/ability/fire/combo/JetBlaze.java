package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.fire.FireJet;

public class JetBlaze extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private int particleAmount;
	private double particleSpread;
	
	private boolean madeNewJet;
	private int counter;
	
	public JetBlaze(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		if (!bPlayer.canBend(getAbility(player, FireJet.class))) {
			return;
		}
		
		if (!hasAbility(player, FireJet.class)) {
			new FireJet(player, ClickType.LEFT_CLICK);
			madeNewJet = true;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.JetBlaze.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.JetBlaze.Duration");
		particleAmount = Azutoru.az.getConfig().getInt("Abilities.Fire.JetBlaze.ParticleAmount");
		particleSpread = Azutoru.az.getConfig().getDouble("Abilities.Fire.JetBlaze.ParticleSpread");
		
		applyModifiers();
		
		start();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			duration *= BlueFireAbility.getRangeFactor();
		}
		
		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			duration = (long) getDayFactor(duration);
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown = 0;
			duration = 0;
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (!hasAbility(player, FireJet.class)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			if (madeNewJet) {
				getAbility(player, FireJet.class).removeWithCooldown();
			}
			remove();
			return;
		}
		
		if (counter % 4 == 0) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0);
		}
		counter++;
		
		ParticleEffect.SMOKE_LARGE.display(player.getLocation(), particleAmount, particleSpread, particleSpread, particleSpread);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "JetBlaze";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a firebender to slightly speed up their FireJet and damage living entities nearby.";
	}
	
	@Override
	public String getInstructions() {
		return "Blaze (Tap sneak) > Blaze (Tap sneak) > FireJet (Left-click)";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new JetBlaze(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
		return combo;
	}

	@Override
	public void load() {
	}

	@Override
	public void stop() {
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
	public boolean isEnabled() {
		boolean enabled = Azutoru.az.getConfig().getBoolean("Abilities.Fire.JetBlaze.Enabled")
				&& Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireJet.Enabled");
		return enabled;
	}

}
