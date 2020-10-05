package me.aztl.azutoru.util;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.SubAbility;

import me.aztl.azutoru.Azutoru;

public abstract class GlassAbility extends EarthAbility implements SubAbility {

	public GlassAbility(Player player) {
		super(player);
	}
	
	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public Element getElement() {
		return Azutoru.az.getGlassElement();
	}

}
