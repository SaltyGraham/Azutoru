package me.aztl.azutoru.ability.earth.sand;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.earth.sand.combo.DustDevilRush;
import me.aztl.azutoru.policy.DifferentWorldPolicy;
import me.aztl.azutoru.policy.ExpirationPolicy;
import me.aztl.azutoru.policy.Policies;
import me.aztl.azutoru.policy.RemovalPolicy;
import me.aztl.azutoru.policy.TopBlockPolicy;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.WorldUtil;

public class DustDevil extends SandAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.HEIGHT)
	private double height;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private int blindnessTime;

	private List<Location> locations;
	private Block topBlock;
	private RemovalPolicy policy;
	private Material topBlockType;
	private double currentHeight;
	private boolean canFly, isFlying;
	
	public DustDevil(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) return;
		
		if (hasAbility(player, DustDevil.class)) {
			if (hasAbility(player, DustDevilRush.class))
				getAbility(player, DustDevilRush.class).remove();
			getAbility(player, DustDevil.class).remove();
			return;
		}
		
		FileConfiguration c = Azutoru.az.getConfig();
		cooldown = c.getLong("Abilities.Earth.DustDevil.Cooldown");
		height = c.getDouble("Abilities.Earth.DustDevil.Height");
		duration = c.getLong("Abilities.Earth.DustDevil.Duration");
		damage = c.getDouble("Abilities.Earth.DustDevil.Damage");
		blindnessTime = c.getInt("Abilities.Earth.DustDevil.BlindnessTime");
		
		if (bPlayer.isAvatarState()) {
			cooldown = 0;
			height *= 1.25;
			duration = 0;
		}
		
		canFly = player.getAllowFlight();
		isFlying = player.isFlying();
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) height);
		locations = new ArrayList<>();
		policy = Policies.builder()
					.add(Policies.IN_LIQUID)
					.add(Policies.ON_GROUND)
					.add(new DifferentWorldPolicy(() -> this.player.getWorld()))
					.add(new ExpirationPolicy(duration))
					.add(new TopBlockPolicy(() -> topBlock, b -> !WorldUtil.isDust(b) && !WorldUtil.isIgnoredPlant(b))).build();
		
		if (!WorldUtil.isDust(topBlock) && !WorldUtil.isIgnoredPlant(topBlock)) return;
		
		if (!isWithinMaxSpoutHeight()) return;
		
		flightHandler.createInstance(player, getName());
		PlayerUtil.allowFlight(player);
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || policy.test(player)) {
			remove();
			return;
		}
		
		topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) height);
		if (WorldUtil.isIgnoredPlant(topBlock)) {
			int dy = WorldUtil.isIgnoredPlant(topBlock.getRelative(BlockFace.DOWN)) ? 2 : 1;
			topBlockType = topBlock.getRelative(BlockFace.DOWN, dy).getType();
		} else {
			topBlockType = topBlock.getType();
		}
		
		if (!isWithinMaxSpoutHeight()) {
			remove();
			return;
		}
		
		updateLocations();
		
		player.setFallDistance(0);
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		
		currentHeight = player.getLocation().getY() - topBlock.getY();
		if (currentHeight > height)
			PlayerUtil.removeFlight(player, canFly, isFlying);
		else
			PlayerUtil.allowFlight(player);
		
		rotateDustColumn();
	}
	
	private void rotateDustColumn() {
		for (double i = 0; i <= currentHeight; i += 0.5) {
			Location loc = player.getLocation().subtract(0, i, 0);
			double radius = 1;
			for (double a = 0; a <= FastMath.PI * 2; a += FastMath.PI / 4) {
				double x = FastMath.cos(a) * radius;
				double z = FastMath.sin(a) * radius;
				loc.add(x, 0, z);
				ParticleEffect.BLOCK_DUST.display(loc, 1, 0, FastMath.random(), 0, 1, topBlockType.createBlockData());
				loc.subtract(x, 0, z);
			}
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
				if (e instanceof LivingEntity && e != player) {
					DamageHandler.damageEntity(e, damage, this);
					if (e instanceof Player) {
						((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessTime, 1));
					}
				}
			}
		}
	}
	
	private void updateLocations() {
		Location p = player.getLocation();
		Location t = topBlock.getLocation();
		t.setX(p.getX());
		t.setY(p.getY());
		locations = MathUtil.getLinePoints(p, t, p.getBlockY() - t.getBlockY());
	}
	
	public void remove() {
		super.remove();
		if (hasAbility(player, DustDevilRush.class))
			getAbility(player, DustDevilRush.class).remove();
		bPlayer.addCooldown(this);
		flightHandler.removeInstance(player, getName());
		PlayerUtil.removeFlight(player, canFly, isFlying);
		return;
	}
	
	private boolean isWithinMaxSpoutHeight() {
		return player.getLocation().getY() <= topBlock.getLocation().getY() + height + 2;
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
	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public String getName() {
		return "DustDevil";
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a sandbender to mount themselves on a column of dust or sand. The rotating dust particles slightly damage entities that come into contact with them and cause temporary blindness.";
	}
	
	@Override
	public String getInstructions() {
		return "Left-click to start using DustDevil. Hold jump to ascend and sneak to descend. Left-click once again to disable the move.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		return Azutoru.az.getConfig().getBoolean("Abilities.Earth.DustDevil.Enabled");
	}

}
