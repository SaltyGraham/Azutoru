package me.aztl.azutoru.manager;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.combo.Crumble;

public class AzutoruManager implements Runnable {

	Azutoru plugin;
	
	public AzutoruManager(Azutoru plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		RaiseEarth.progressAll();
		Crumble.progressAll();
	}

}
