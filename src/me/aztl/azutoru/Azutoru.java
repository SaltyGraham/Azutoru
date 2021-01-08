package me.aztl.azutoru;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.ElementType;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.CoreAbility;

import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.config.AzutoruConfig;
import me.aztl.azutoru.listener.AzutoruListener;
import me.aztl.azutoru.manager.AzutoruManager;

public class Azutoru extends JavaPlugin {
	
	public static Azutoru az;
	private Element glassElement;
	private static Logger log;
	
	@Override
	public void onEnable() {
		az = this;
		log = getLogger();
		
		new AzutoruConfig();
		CoreAbility.registerPluginAbilities(this, "me.aztl.azutoru.ability");
		getServer().getPluginManager().registerEvents(new AzutoruListener(), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AzutoruManager(), 0, 1);
		
		glassElement = new SubElement("Glass", Element.EARTH, ElementType.BENDING, this) {
			@Override
			public ChatColor getColor() {
				return Element.EARTH.getSubColor();
			}
		};
	}
	
	@Override
	public void onDisable() {
		RaiseEarth.removeAllCleanup();
	}
	
	public Logger getLog() {
		return log;
	}
	
	public String dev() {
		return "Aztl";
	}
	
	public String prefix() {
		return ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "Azutoru" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY;
	}
	
	public String version() {
		return prefix() + " v. 1.0.0";
	}
	
	public Element getGlassElement() {
		return glassElement;
	}

}
