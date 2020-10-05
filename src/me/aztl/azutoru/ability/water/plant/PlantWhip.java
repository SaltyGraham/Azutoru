package me.aztl.azutoru.ability.water.plant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

import me.aztl.azutoru.Azutoru;

public class PlantWhip extends PlantAbility implements AddonAbility {

	private double sourceRange, damage, range, speed, hitRadius, knockback, knockup;
	private long cooldown, duration;
	
	private Location origin, location;
	private Block sourceBlock;
	private Material material;
	private Vector direction;
	private boolean launching;

	public PlantWhip(Player player) {
		super(player);
		
		final PlantWhip oldWhip = getAbility(player, PlantWhip.class);
		if (oldWhip != null) {
			if (!oldWhip.launching) {
				oldWhip.remove();
			}
		}
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		setFields();
		
		launching = false;
		
		if (!setOrigin()) {
			return;
		}
		
		start();
		
	}

	public void setFields() {
		
		damage = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.Damage");
		cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.PlantWhip.Cooldown");
		range = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.Range");
		sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.SourceRange");
		duration = Azutoru.az.getConfig().getLong("Abilities.Water.PlantWhip.Duration");
		speed = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.Speed");
		hitRadius = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.HitRadius");
		knockback = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.Knockback");
		knockup = Azutoru.az.getConfig().getDouble("Abilities.Water.PlantWhip.Knockup");
		
	}
	
	public boolean setOrigin() {
		Block source = WaterAbility.getPlantSourceBlock(player, sourceRange, false, true);
		
		if (source != null) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, source.getLocation())) {
				remove();
				return false;
			}
			origin = source.getLocation();
			sourceBlock = origin.getBlock();
			location = origin.clone();
			
			if (isPlantbendable(sourceBlock)) {
				switch (sourceBlock.getType()) {
				case OAK_LEAVES:
					material = Material.OAK_LEAVES;
					break;
				case SPRUCE_LEAVES:
					material = Material.SPRUCE_LEAVES;
					break;
				case BIRCH_LEAVES:
					material = Material.BIRCH_LEAVES;
					break;
				case JUNGLE_LEAVES:
					material = Material.JUNGLE_LEAVES;
					break;
				case ACACIA_LEAVES:
					material = Material.ACACIA_LEAVES;
					break;
				case DARK_OAK_LEAVES:
					material = Material.DARK_OAK_LEAVES;
					break;
				default:
					material = Material.OAK_LEAVES;
					break;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, origin) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		
		if (player.getLocation().distanceSquared(location) > range * range) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (player.getLocation().distanceSquared(origin) > sourceRange * sourceRange && !launching) {
			remove();
			return;
		}
		
		if (!launching) {
			WaterAbility.playFocusWaterEffect(origin.getBlock());
		}
		
		if (launching) {
			if (isPlant(this.sourceBlock)) {
				if (!isDecayablePlant(this.sourceBlock)) {
					this.sourceBlock.setType(Material.AIR);
				}
				new PlantRegrowth(this.player, this.sourceBlock, 2);
			}
			
			direction = player.getLocation().getDirection();
			launch();
		}
	}
	
	public void launch() {
		location.add(direction.multiply(speed));
		
		if (GeneralMethods.isSolid(location.getBlock()) && !GeneralMethods.isTransparent(location.getBlock()) && !location.getBlock().getType().toString().contains("LEAVES")) {
			remove();
			return;
		}
		
		addLeaves();
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
			if ((entity instanceof LivingEntity) && entity.getUniqueId() != player.getUniqueId()) {
				DamageHandler.damageEntity(entity, damage, this);
				Vector travelVec = GeneralMethods.getDirection(location, entity.getLocation());
				entity.setVelocity(travelVec.setY(knockup).normalize().multiply(knockback));
			}
		}
	}
	
	public void addLeaves() {
		TempBlock tb = new TempBlock(location.getBlock(), material);
		tb.setRevertTime(duration);
		
		tb.getBlock().setMetadata("PlantWhip", new FixedMetadataValue(Azutoru.az, ""));
		
		if (tb.getBlock().getType() == Material.AIR) {
			tb.getBlock().removeMetadata("PlantWhip", Azutoru.az);
		}
		
		PhaseChange.getFrozenBlocksMap().put(tb, player);
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
	}
	
	public void onLaunch() {
		launching = true;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "PlantWhip";
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
	public String getAuthor() {
		return Azutoru.az.dev();
	}

	@Override
	public String getVersion() {
		return Azutoru.az.version();
	}
	
	@Override
	public String getDescription() {
		return "This ability allows a plantbender to create a long whip of plants, dealing damage to anything near it!";
	}
	
	@Override
	public String getInstructions() {
		return "Tap sneak on a plant block and left-click in the direction you want the whip to go. The whip will follow the direction you are looking.";
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
