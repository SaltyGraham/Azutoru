package me.aztl.azutoru.ability.water.multicombo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.AzutoruMethods;
import me.aztl.azutoru.ability.water.combo.WaterSlash;
import me.aztl.azutoru.ability.water.multiability.Transform;

public class Hexapod extends WaterAbility implements AddonAbility, MultiAbility, ComboAbility {
	
	public static enum HexapodArm {
		RIGHT_CRANIAL("Right Cranial", ArmSide.RIGHT, AnatomicalDirection.CRANIAL),
		LEFT_CRANIAL("Left Cranial", ArmSide.LEFT, AnatomicalDirection.CRANIAL),
		RIGHT_MEDIAL("Right Medial", ArmSide.RIGHT, AnatomicalDirection.MEDIAL),
		LEFT_MEDIAL("Left Medial", ArmSide.LEFT, AnatomicalDirection.MEDIAL),
		RIGHT_CAUDAL("Right Caudal", ArmSide.RIGHT, AnatomicalDirection.CAUDAL),
		LEFT_CAUDAL("Left Caudal", ArmSide.LEFT, AnatomicalDirection.CAUDAL);
		
		public static enum ArmSide {
			RIGHT, LEFT;
		}
		
		public static enum AnatomicalDirection {
			CRANIAL, MEDIAL, CAUDAL;
		}
		
		private String name;
		private ArmSide side;
		private AnatomicalDirection direction;
		
		private List<Block> rightCranial, leftCranial, rightMedial, leftMedial, rightCaudal, leftCaudal;
		
		private HexapodArm(String name, ArmSide side, AnatomicalDirection direction) {
			this.name = name;
			this.side = side;
			this.direction = direction;
			
			rightCranial = new ArrayList<>();
			leftCranial = new ArrayList<>();
			rightMedial = new ArrayList<>();
			leftMedial = new ArrayList<>();
			rightCaudal = new ArrayList<>();
			leftCaudal = new ArrayList<>();
		}
		
		public String getName() {
			return name;
		}
		
		public ArmSide getSide() {
			return side;
		}
		
		public AnatomicalDirection getDirection() {
			return direction;
		}
		
		public List<Block> getBlocks() {
			switch (name) {
			case "Right Cranial":
				return rightCranial;
			case "Left Cranial":
				return leftCranial;
			case "Right Medial":
				return rightMedial;
			case "Left Medial":
				return leftMedial;
			case "Right Caudal":
				return rightCaudal;
			case "Left Caudal":
				return leftCaudal;
			default:
				break;
			}
			return null;
		}
	}
	
	public static enum HexapodAbility {
		NONE("null", -1, ClickType.LEFT_CLICK),
		SLAM("Slam", 0, ClickType.LEFT_CLICK),
		SPIN("Slam", 0, ClickType.SHIFT_DOWN),
		SLASH("Slash", 1, ClickType.LEFT_CLICK),
		GRAPPLE("Grapple", 2, ClickType.LEFT_CLICK),
		CLING("Cling", 3, ClickType.LEFT_CLICK),
		GRAB("Grab", 4, ClickType.LEFT_CLICK),
		WALL("Shell", 5, ClickType.LEFT_CLICK),
		SHELL("Shell", 5, ClickType.SHIFT_DOWN),
		CATAPULT("Catapult", 6, ClickType.LEFT_CLICK),
		TREBUCHET("Catapult", 6, ClickType.SHIFT_DOWN),
		WINGS("Wings", 7, ClickType.LEFT_CLICK),
		TRANSFORM("Transform", 8, ClickType.LEFT_CLICK);
		
		private String name;
		private int slot;
		private ClickType clickType;
		
		private HexapodAbility(String name, int slot, ClickType clickType) {
			this.name = name;
			this.slot = slot;
			this.clickType = clickType;
		}
		
		public String getName() {
			return name;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public void setSlot(int slot) {
			this.slot = slot;
		}
		
		public ClickType getClickType() {
			return clickType;
		}
		
		public static HexapodAbility getAbility(int slot, ClickType clickType) {
			for (HexapodAbility ability : HexapodAbility.values()) {
				if (ability.getSlot() == slot && ability.getClickType() == clickType) {
					return ability;
				}
			}
			return NONE;
		}
	}
	
	public static enum HexapodWing {
		RIGHT("Right Wing"),
		LEFT("Left Wing");
		
		private String name;
		private List<Block> rightWing, leftWing;
		
		private HexapodWing(String name) {
			this.name = name;
			
			rightWing = new ArrayList<>();
			leftWing = new ArrayList<>();
		}
		
		public List<Block> getBlocks() {
			switch (name) {
			case "Right Wing":
				return rightWing;
			case "Left Wing":
				return leftWing;
			default:
				break;
			}
			return null;
		}
	}
	
	private long cooldown, duration;
	private double sourceRange, armLength, wingLength;
	
	private HexapodAbility ability;
	private List<HexapodArm> activeArms;
	private HexapodArm mainActiveArm;
	private List<HexapodWing> activeWings;
	private World world;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	
	public Hexapod(Player player, ClickType clickType) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (hasAbility(player, WaterArms.class)) {
			getAbility(player, WaterArms.class).remove();
		}
		
		if (hasAbility(player, Hexapod.class)) {
			getAbility(player, Hexapod.class).activate(player.getInventory().getHeldItemSlot(), clickType);
			return;
		}
		
		if (clickType == ClickType.SHIFT_DOWN) {
			MultiAbilityManager.bindMultiAbility(player, "Hexapod");
			
			cooldown = Azutoru.az.getConfig().getLong("Abilities.Water.Hexapod.Cooldown");
			duration = Azutoru.az.getConfig().getLong("Abilities.Water.Hexapod.Duration");
			sourceRange = Azutoru.az.getConfig().getDouble("Abilities.Water.Hexapod.SourceRange");
			armLength = Azutoru.az.getConfig().getDouble("Abilities.Water.Hexapod.ArmLength");
			wingLength = Azutoru.az.getConfig().getDouble("Abilities.Water.Hexapod.Wings.WingLength");
			
			ability = HexapodAbility.NONE;
			world = player.getWorld();
			affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();
			activeArms = new ArrayList<>();
			activeWings = new ArrayList<>();
			
			Block sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, bPlayer.canIcebend(), false, false, false);
			if (sourceBlock != null && GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
				start();
			}
		}
	}
	
	public void activate(int slot, ClickType clickType) {
		if (ability != HexapodAbility.NONE) {
			return;
		}
		
		HexapodAbility ability = HexapodAbility.getAbility(slot, clickType);
		
		if (clickType != ability.getClickType()) {
			return;
		}
		
		if (bPlayer.isOnCooldown(ability.getName())) {
			return;
		}
		
		if (ability == HexapodAbility.SLASH) {
			new WaterSlash(player, false);
		} else if (ability == HexapodAbility.TRANSFORM) {
			new Transform(player);
		} else {
			switchMainActiveArm();
			this.ability = ability;
		}
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
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
		
		if (ability != HexapodAbility.WINGS) {
			for (HexapodArm arm : HexapodArm.values()) {
				if (activeArms.contains(arm)) {
					continue;
				}
				displayArm(arm);
			}
		} else {
			for (HexapodWing wing : HexapodWing.values()) {
				if (activeWings.contains(wing)) {
					break;
				}
				displayWing(wing);
			}
		}
		
		switch (ability) {
		case SLAM:
			progressSlam();
			break;
		case SPIN:
			progressSpin();
			break;
		case GRAPPLE:
			progressGrapple();
			break;
		case CLING:
			progressCling();
			break;
		case GRAB:
			progressGrab();
			break;
		case WALL:
			progressWall();
			break;
		case SHELL:
			progressShell();
			break;
		case CATAPULT:
			progressCatapult();
			break;
		case TREBUCHET:
			progressTrebuchet();
			break;
		case WINGS:
			progressWings();
			break;
		case NONE:
			break;
		default:
			break;
		}
	}
	
	private void progressSlam() {
	}

	private void progressSpin() {
	}

	private void progressGrapple() {
	}

	private void progressCling() {
	}

	private void progressGrab() {
	}

	private void progressWall() {
	}

	private void progressShell() {
	}

	private void progressCatapult() {
	}

	private void progressTrebuchet() {
	}

	private void progressWings() {
	}
	
	private void displayArm(HexapodArm arm) {
		AzutoruMethods.revertBlocks(affectedBlocks);
		arm.getBlocks().clear();
		
		Location eye = player.getEyeLocation().clone();
		float yaw = eye.getYaw();
		Vector dir = eye.getDirection();
		
		if (arm.getSide() == HexapodArm.ArmSide.RIGHT) {
			eye = GeneralMethods.getRightSide(eye, 1.5);
			
			switch (arm.getDirection()) {
			case CRANIAL:
				yaw += 60;
			case MEDIAL:
				yaw += 80;
			default:
				break;
			}
			
			if (yaw >= 180) {
				yaw -= 360;
			}
		} else if (arm.getSide() == HexapodArm.ArmSide.LEFT) {
			eye = GeneralMethods.getLeftSide(eye, 1.5);
			
			switch (arm.getDirection()) {
			case CRANIAL:
				yaw -= 60;
			case MEDIAL:
				yaw -= 80;
			default:
				break;
			}
			
			if (yaw < -180) {
				yaw += 360;
			}
		}
		
		eye.setYaw(yaw);
		eye.setPitch(0);
		
		for (double d = 0; d <= armLength; d += 0.5) {
			switch (arm.getDirection()) {
			case CRANIAL:
				double y = 0.1 * Math.pow(eye.distance(player.getEyeLocation()), 3);
				dir.setY(y);
			case MEDIAL:
				yaw -= 0.1 * Math.pow(eye.distance(player.getEyeLocation()), 3);
				eye.setYaw(yaw);
				dir = eye.getDirection();
			default:
				break;
			}
			
			eye.add(dir.multiply(1.0 / armLength));
			
			arm.getBlocks().add(eye.getBlock());
		}
		
		for (Block b : arm.getBlocks()) {
			TempBlock tb = new TempBlock(b, Material.WATER);
			tb.setRevertTime(100);
			affectedBlocks.put(b, tb);
		}
	}
	
	private void displayWing(HexapodWing wing) {
		AzutoruMethods.revertBlocks(affectedBlocks);
		wing.getBlocks().clear();
		
		Location headLoc = player.getEyeLocation().clone();
		Location torsoLoc = player.getEyeLocation().clone().subtract(headLoc.getDirection());
		
		if (wing == HexapodWing.RIGHT) {
			for (int i = 0; i < wingLength; i++) {
				headLoc = GeneralMethods.getRightSide(player.getEyeLocation(), i);
				wing.getBlocks().add(headLoc.getBlock());
			}
			for (int i = 0; i < wingLength - 1; i++) {
				torsoLoc.add(GeneralMethods.getRightSide(torsoLoc, 1));
				wing.getBlocks().add(torsoLoc.getBlock());
			}
		} else if (wing == HexapodWing.LEFT) {
			for (int i = 0; i < wingLength; i++) {
				headLoc = GeneralMethods.getLeftSide(player.getEyeLocation(), i);
				wing.getBlocks().add(headLoc.getBlock());
			}
			for (int i = 0; i < wingLength - 1; i++) {
				torsoLoc.add(GeneralMethods.getLeftSide(torsoLoc, 1));
				wing.getBlocks().add(torsoLoc.getBlock());
			}
		}
		
		for (Block b : wing.getBlocks()) {
			TempBlock tb = new TempBlock(b, Material.WATER);
			tb.setRevertTime(100);
			affectedBlocks.put(b, tb);
		}
	}

	public void switchMainActiveArm() {
		switch (mainActiveArm) {
		case RIGHT_CRANIAL:
			mainActiveArm = HexapodArm.LEFT_CRANIAL;
		case LEFT_CRANIAL:
			mainActiveArm = HexapodArm.RIGHT_MEDIAL;
		case RIGHT_MEDIAL:
			mainActiveArm = HexapodArm.LEFT_MEDIAL;
		case LEFT_MEDIAL:
			mainActiveArm = HexapodArm.RIGHT_CRANIAL;
		default:
			mainActiveArm = HexapodArm.RIGHT_CRANIAL;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		affectedBlocks.clear();
		for (HexapodArm arm : HexapodArm.values()) {
			arm.getBlocks().clear();
		}
		for (HexapodWing wing : HexapodWing.values()) {
			wing.getBlocks().clear();
		}
		MultiAbilityManager.unbindMultiAbility(player);
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "Hexapod";
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public String getInstructions() {
		return null;
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
		return new Hexapod(player, ClickType.SHIFT_DOWN);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("OctopusForm", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("OctopusForm", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("Hexapod", ClickType.SHIFT_DOWN));
		return combo;
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> subs = new ArrayList<>();
		subs.add(new MultiAbilityInfoSub("Slam", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Slash", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Grapple", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Cling", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Grab", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Shell", Element.ICE));
		subs.add(new MultiAbilityInfoSub("Wings", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Spear", Element.WATER));
		subs.add(new MultiAbilityInfoSub("Transform", Element.WATER));
		return subs;
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
