package me.aztl.azutoru.ability.earth.metal.multiability;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.util.ClickType;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.AzutoruMethods.Hand;
import me.aztl.azutoru.util.Rope;

public class MetalCables extends MetalAbility implements AddonAbility, MultiAbility {

	public static enum CableAbility {
		GRAPPLE_LEFT, GRAPPLE_RIGHT, GRAPPLE_PULL, GRAB_LEFT, GRAB_RIGHT, GRAB_PULL, LEAP, RETRACT;
	}
	
	private long cooldown, duration;
	private int maxUses;
	@SuppressWarnings("unused")
	private double grappleRange, grabRange, grabRadius, speed;
	
	private World world;
	private CableAbility ability;
	private Rope left, right;
	private Location leftLoc, rightLoc;
	private Vector direction;
	@SuppressWarnings("unused")
	private boolean leftInUse, rightInUse;
	@SuppressWarnings("unused")
	private Predicate<Location> removalPolicy;
	
	public MetalCables(Player player, ClickType type) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		MetalCables mc = getAbility(player, MetalCables.class);
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
			
			world = player.getWorld();
			
			start();
		}
	}
	
	public void activate(int slot, ClickType type) {
		switch (slot) {
		case 0: // "Grapple Left"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.GRAPPLE_LEFT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = CableAbility.GRAPPLE_PULL;
			}
			break;
		case 1: // "Grapple Right"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.GRAPPLE_RIGHT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = CableAbility.GRAPPLE_PULL;
			}
			break;
		case 2: // "Grab Left"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.GRAB_LEFT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = CableAbility.GRAB_PULL;
			}
			break;
		case 3: // "Grab Right"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.GRAB_RIGHT;
			} else if (type == ClickType.SHIFT_DOWN) {
				ability = CableAbility.GRAB_PULL;
			}
			break;
		case 4: // "Leap"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.LEAP;
			}
			break;
		case 5: // "Retract"
			if (type == ClickType.LEFT_CLICK) {
				ability = CableAbility.RETRACT;
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
		
		leftLoc = AzutoruMethods.getHandPos(player, Hand.LEFT);
		rightLoc = AzutoruMethods.getHandPos(player, Hand.RIGHT);
		direction = player.getEyeLocation().getDirection();
		
		switch (ability) {
		case GRAPPLE_LEFT:
			if (left == null) {
				left = new Rope(leftLoc, direction, 10, 100, 0.5, speed, null);
			}
			grapple(left);
			break;
		case GRAPPLE_RIGHT:
			if (right == null) {
				right = new Rope(rightLoc, direction, 10, 100, 0.5, speed, null);
			}
			grapple(right);
			break;
		case GRAPPLE_PULL:
			grapplePull();
			break;
		case GRAB_LEFT:
			grab(left);
			break;
		case GRAB_RIGHT:
			grab(right);
			break;
		case GRAB_PULL:
			grabPull();
			break;
		case LEAP:
			leap();
			break;
		case RETRACT:
			remove();
			break;
		default:
			break;
		}
	}
	
	private void grapple(Rope cable) {
		
	}
	
	private void grapplePull() {
		
	}
	
	private void grab(Rope cable) {
		
	}
	
	private void grabPull() {
		
	}
	
	private void leap() {
		
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		MultiAbilityManager.unbindMultiAbility(player);
	}
	
	public CableAbility getAbility() {
		return ability;
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
