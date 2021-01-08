package me.aztl.azutoru.manager;

import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.combo.Crumble;

public class AzutoruManager implements Runnable {
	
	@Override
	public void run() {
		RaiseEarth.progressAll();
		Crumble.progressAll();
	}

}
