package me.aztl.azutoru.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.aztl.azutoru.Azutoru;

public class Config {
	
	private final Path path;
	private final FileConfiguration config;
	
	public Config(String name) {
		path = Paths.get(Azutoru.az.getDataFolder().toString(), name);
		config = YamlConfiguration.loadConfiguration(path.toFile());
		reloadConfig();
	}
	
	private void createConfig() {
		try {
			Files.createFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reloadConfig() {
		if (Files.notExists(path)) {
			createConfig();
		}
		try {
			config.load(path.toFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveConfig() {
		try {
			config.options().copyDefaults(true);
			config.save(path.toFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public FileConfiguration getConfig() {
		return config;
	}

}
