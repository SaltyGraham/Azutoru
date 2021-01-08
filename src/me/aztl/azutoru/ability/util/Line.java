package me.aztl.azutoru.ability.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;

import me.aztl.azutoru.Azutoru;

public abstract class Line extends ElementalAbility implements AddonAbility {
	
	protected Location location;
	protected Location origin;
	protected Vector direction;
	protected double speed;
	protected double range;
	protected long interval;
	
	private long nextUpdate;
	
	public Line(Player player, Location origin, Vector direction, double speed, double range) {
		this(player, origin, direction, speed, range, 0);
	}

	public Line(Player player, Location origin, Vector direction, double speed, double range, long interval) {
		super(player);
		
		this.origin = origin;
		this.location = origin;
		this.direction = direction;
		this.speed = speed;
		this.range = range;
		this.interval = interval;
		
		start();
	}
	
	@Override
	public void progress() {
		if (interval >= 1) {
			long time = System.currentTimeMillis();
			if (time <= nextUpdate) return;
			nextUpdate = time + interval * 50;
		}
		
		if (location.distanceSquared(origin) > range * range
				|| GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		
		location.add(direction.clone().multiply(speed));
		Block block = location.getBlock();
		
		if (!isValidBlock(block)) {
			if (isValidBlock(block.getRelative(BlockFace.UP)))
				block = location.add(0, 1, 0).getBlock();
			else if (isValidBlock(block.getRelative(BlockFace.DOWN)))
				block = location.subtract(0, 1, 0).getBlock();
			else {
				remove();
				return;
			}
		}
		
		render(block);
	}
	
	protected abstract boolean isValidBlock(Block block);
	
	protected abstract void render(Block block);
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}
	
	@Override
	public String getName() {
		return "Line";
	}
	
	@Override
	public Element getElement() {
		return Element.AVATAR;
	}
	
	@Override
	public Location getLocation() {
		return location;
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
	public void load() {}

	@Override
	public void stop() {}

}
