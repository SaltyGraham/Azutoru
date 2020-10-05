package me.aztl.azutoru.ability.earth.metal;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;

public class MetalCables extends MetalAbility implements AddonAbility, MultiAbility {

	public static enum Ability {
		GRAPPLE_LEFT, GRAPPLE_RIGHT, GRAPPLE_PULL, GRAB_LEFT, GRAB_RIGHT, GRAB_PULL, LEAP, RETRACT;
	}
	
	public static enum Cable {
		LEFT, RIGHT;
	}
	
	private long cooldown, duration, usageCooldown;
	private int maxUses;
	@SuppressWarnings("unused")
	private double grappleRange, grabRange, grabRadius, speed;
	
	private World world;
	private Ability ability;
	@SuppressWarnings("unused")
	private Cable cable;
	@SuppressWarnings("unused")
	private Location left, right, destination, location;
	private Vector direction;
	private boolean leftInUse, rightInUse;
	
	public MetalCables(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		MetalCables mc = CoreAbility.getAbility(player, MetalCables.class);
		if (mc != null) {
			mc.activate(player.getInventory().getHeldItemSlot(), type);
			return;
		}
		
		if (type == ClickType.LEFT_CLICK) {
			MultiAbilityManager.bindMultiAbility(player, "MetalCables");
			cooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.MetalCables.Cooldown");
			duration = Azutoru.az.getConfig().getLong("Abilities.Earth.MetalCables.Duration");
			maxUses = Azutoru.az.getConfig().getInt("Abilities.Earth.MetalCables.MaxUses");
			grappleRange = Azutoru.az.getConfig().getDouble("Abilities.Earth.MetalCables.GrappleRange");
			grabRange = Azutoru.az.getConfig().getDouble("Abilities.Earth.MetalCables.GrabRange");
			grabRadius = Azutoru.az.getConfig().getDouble("Abilities.Earth.MetalCables.GrabRadius");
			speed = Azutoru.az.getConfig().getDouble("Abilities.Earth.MetalCables.Speed");
			usageCooldown = Azutoru.az.getConfig().getLong("Abilities.Earth.MetalCables.UsageCooldown");
			
			world = player.getWorld();
			
			start();
		}
	}
	
	public void activate(int slot, ClickType type) {
		switch (slot) {
		case 0:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.GRAPPLE_LEFT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = Ability.GRAPPLE_PULL;
			}
			break;
		case 1:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.GRAPPLE_RIGHT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = Ability.GRAPPLE_PULL;
			}
			break;
		case 2:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.GRAB_LEFT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = Ability.GRAB_PULL;
			}
			break;
		case 3:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.GRAB_RIGHT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = Ability.GRAB_PULL;
			}
			break;
		case 4:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.LEAP;
			}
			break;
		case 5:
			if (type == ClickType.LEFT_CLICK) {
				ability = Ability.RETRACT;
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		if (maxUses == 0) {
			remove();
			return;
		}
		
		if (!MultiAbilityManager.hasMultiAbilityBound(player, "MetalCables")) {
			remove();
			return;
		}
		
		if (!player.getWorld().equals(world)) {
			remove();
			return;
		}
		
		if (duration > 0 && System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		left = AzutoruMethods.getHandPos(player, Hand.LEFT);
		right = AzutoruMethods.getHandPos(player, Hand.RIGHT);
		
		if (ability == Ability.GRAPPLE_LEFT) {
			launchCable(Cable.LEFT);
			
		} else if (ability == Ability.GRAPPLE_RIGHT) {
			
		} else if (ability == Ability.GRAPPLE_PULL) {
			
		} else if (ability == Ability.GRAB_LEFT) {
			
		} else if (ability == Ability.GRAB_RIGHT) {
			
		} else if (ability == Ability.GRAB_PULL) {
			
		} else if (ability == Ability.RETRACT) {
			
		}
	}
	
	public void launchCable(Cable cable) {
		if (bPlayer.isOnCooldown("MetalCables_" + cable.toString())) {
			return;
		}
		
		if (isCableInUse(cable)) {
			return;
		}
		
		if (direction == null) {
			setCableStartPos(cable);
			bPlayer.addCooldown("MetalCables_" + cable.toString(), usageCooldown);
		}
		
		if (ability == Ability.GRAPPLE_LEFT || ability == Ability.GRAPPLE_RIGHT) {
			if (GeneralMethods.isSolid(location.getBlock())) {
				
			}
		} else if (ability == Ability.GRAB_LEFT || ability == Ability.GRAB_RIGHT) {
			
		}
		
		location.add(direction.multiply(speed));
		location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, new DustOptions(Color.fromRGB(171, 172, 171), 1));
		
	}
	
	private void setCableStartPos(Cable cable) {
		if (cable == Cable.LEFT) {
			location = left.clone();
			direction = left.getDirection();
		} else if (cable == Cable.RIGHT) {
			location = right.clone();
			direction = right.getDirection();
		}
	}
	
	private boolean isCableInUse(Cable cable) {
		if (cable == Cable.LEFT) {
			return leftInUse;
		} else if (cable == Cable.RIGHT) {
			return rightInUse;
		} else {
			return true;
		}
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
	public String getName() {
		return "MetalCables";
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
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> abils = new ArrayList<>();
		abils.add(new MultiAbilityInfoSub("Grapple Left", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grapple Right", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grab Left", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Grab Right", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Leap", Element.METAL));
		abils.add(new MultiAbilityInfoSub("Retract", Element.METAL));
		return abils;
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
		return false;
	}

}
