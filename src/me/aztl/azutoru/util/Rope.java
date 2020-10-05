package me.aztl.azutoru.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public class Rope {
	
	private Player player;
	private Location startLocation;
	private Element element;
	
	public Rope(Player player, Location startLocation, Element element) {
		this.player = player;
		this.startLocation = startLocation;
		this.element = element;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Location getStartLocation() {
		return startLocation;
	}
	
	public Element getElement() {
		return element;
	}

}
