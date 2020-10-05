package me.aztl.azutoru.ability.fire.combo;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

import me.aztl.azutoru.Azutoru;

public class FireAugmentation extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown, duration;
	private double range, speed, damage, sourceRange;
	private boolean allowSlotChange;
	
	private Block sourceBlock;
	private Location location, destination;
	private Vector direction;
	private boolean clicked, launching, pulling;
	private ArrayList<BukkitRunnable> tasks;
	
	public FireAugmentation(Player player) {
		super(player);
		
		tasks = new ArrayList<>();
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Fire.FireAugmentation.Cooldown");
		duration = Azutoru.az.getConfig().getLong("Abilities.Fire.FireAugmentation.Duration");
		range = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireAugmentation.Range");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireAugmentation.Speed");
		damage = Azutoru.az.getConfig().getDouble("Abilities.Fire.FireAugmentation.Damage");
		sourceRange = Azutoru.az.getConfig().getLong("Abilities.Fire.FireAugmentation.SourceRange");
		allowSlotChange = Azutoru.az.getConfig().getBoolean("Abilities.Fire.FireAugmentation.AllowSlotChange");
		
		clicked = false;
		launching = false;
		pulling = false;
		
		sourceBlock = player.getTargetBlock((HashSet<Material>) null, (int) sourceRange);
		
		location = sourceBlock.getLocation().add(0, 0.5, 0);
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (!bPlayer.getBoundAbilityName().equalsIgnoreCase("heatcontrol") && !allowSlotChange) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (location.distanceSquared(player.getLocation()) > range * range) {
			location.subtract(direction.clone().multiply(speed));
			return;
		}
		
		if (player.getWorld() != location.getWorld()) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		
		if (clicked) {
			progressStream();
		}
	}
	
	public void progressStream() {
		if (launching) {
			destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
			
			direction = GeneralMethods.getDirection(location, destination).normalize();
			
			if (player.isSneaking()) {
				pulling = true;
				launching = false;
			}
		}
		
		if (pulling) {
			destination = GeneralMethods.getTargetedLocation(player, 3);
			
			direction = GeneralMethods.getDirection(location, destination).normalize();
			
			if (!player.isSneaking()) {
				launching = true;
				pulling = false;
			}
		}
		
		location.add(direction.clone().multiply(speed));
		
		if (!isTransparent(location.getBlock())) {
			location.add(0, 0.1, 0);
			location.subtract(direction.clone().multiply(speed));
		}
		
		for (int i = 0; i < 10; i++) {
			BukkitRunnable br = new BukkitRunnable() {
				Location loc = FireAugmentation.this.location.clone();
				Vector dir = FireAugmentation.this.direction.clone();
				
				@Override
				public void run() {
					for (int angle = -180; angle <= 180; angle += 20) {
						Vector ortho = GeneralMethods.getOrthogonalVector(dir.clone(), angle, 2);
						playFirebendingParticles(loc.clone().add(ortho), 1, 0, 0, 0);
					}
				}
				
			};
			br.runTaskLater(Azutoru.az, i * 2);
			tasks.add(br);
		}
		
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, 2)) {
			if (isIgnitable(block) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				if (canFireGrief()) {
					if (isPlant(block) || isSnow(block)) {
						new PlantRegrowth(player, block);
					}
				}
				createTempFire(block.getLocation());
			}
		}
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
			if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
				DamageHandler.damageEntity(entity, damage, this);
			}
		}
	}
	
	public void onClick() {
		clicked = true;
		launching = true;
	}
	
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location != null ? location : null;
	}

	@Override
	public String getName() {
		return "FireAugmentation";
	}
	
	@Override
	public String getDescription() {
		return "This combo allows a firebender to increase the size of their flames and spread fire wherever they look..";
	}
	
	@Override
	public String getInstructions() {
		return "FireShield (Hold sneak) > HeatControl (Release sneak) > HeatControl (Left click). Hold sneak to pull the flames towards you.";
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
		return new FireAugmentation(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("HeatControl", ClickType.SHIFT_UP));
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
		return true;
	}

}
