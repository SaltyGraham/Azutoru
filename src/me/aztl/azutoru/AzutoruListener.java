package me.aztl.azutoru;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

import me.aztl.azutoru.ability.air.CloudSurf;
import me.aztl.azutoru.ability.air.combo.AirCocoon;
import me.aztl.azutoru.ability.air.combo.AirSpoutRush;
import me.aztl.azutoru.ability.chi.passive.Dodge;
import me.aztl.azutoru.ability.chi.passive.Duck;
import me.aztl.azutoru.ability.chi.passive.Parry;
import me.aztl.azutoru.ability.earth.glass.GlassShards;
import me.aztl.azutoru.ability.earth.lava.passive.LavaWalk;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.ability.earth.sand.combo.DustDevilRush;
import me.aztl.azutoru.ability.fire.FireDaggers;
import me.aztl.azutoru.ability.fire.combo.FireAugmentation;
import me.aztl.azutoru.ability.fire.lightning.Electrify;
import me.aztl.azutoru.ability.water.WaterCanvas;
import me.aztl.azutoru.ability.water.blood.BloodStrangle;
import me.aztl.azutoru.ability.water.combo.RazorRings;
import me.aztl.azutoru.ability.water.combo.WaterPinwheel;
import me.aztl.azutoru.ability.water.combo.WaterRun;
import me.aztl.azutoru.ability.water.combo.WaterSlash;
import me.aztl.azutoru.ability.water.combo.WaterSphere;
import me.aztl.azutoru.ability.water.combo.WaterSpoutRush;
import me.aztl.azutoru.ability.water.healing.HealingHands;
import me.aztl.azutoru.ability.water.ice.combo.IceShots;
import me.aztl.azutoru.ability.water.multiability.Transform;
import me.aztl.azutoru.ability.water.plant.PlantWhip;

public class AzutoruListener implements Listener {

	Azutoru plugin;
	
	public AzutoruListener(final Azutoru plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onLeftClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}
		if (bPlayer == null) {
			return;
		}
		if (Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		} else if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
			event.setCancelled(true);
			return;
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		} else if (GeneralMethods.isInteractable(player.getTargetBlock((Set<Material>)null, 5))) {
			return;
		}
		
		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		
		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			
			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("airspout") && CoreAbility.hasAbility(player, AirSpoutRush.class)) {
					CoreAbility.getAbility(player, AirSpoutRush.class).remove();
				} else if (abil.equalsIgnoreCase("cloudsurf")) {
					if (CoreAbility.hasAbility(player, CloudSurf.class)) {
						CoreAbility.getAbility(player, CloudSurf.class).remove();
					} else {
						new CloudSurf(player);
					}
				}
			}
			
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("plantwhip") && CoreAbility.hasAbility(player, PlantWhip.class)) {
					CoreAbility.getAbility(player, PlantWhip.class).onLaunch();
				} else if (abil.equalsIgnoreCase("surge") && CoreAbility.hasAbility(player, RazorRings.class)) {
					new RazorRings(player);
				} else if (abil.equalsIgnoreCase("transform")) {
					new Transform(player);
				} else if (abil.equalsIgnoreCase("waterspout") && CoreAbility.hasAbility(player, WaterRun.class)) {
					CoreAbility.getAbility(player, WaterRun.class).remove();
				} else if (abil.equalsIgnoreCase("waterspout") && CoreAbility.hasAbility(player, WaterSpoutRush.class)) {
					CoreAbility.getAbility(player, WaterSpoutRush.class).remove();
				} else if (abil.equalsIgnoreCase("icespike") && CoreAbility.hasAbility(player, IceShots.class)) {
					CoreAbility.getAbility(player, IceShots.class).onClick();
				} else if (abil.equalsIgnoreCase("surge") && CoreAbility.hasAbility(player, WaterPinwheel.class)) {
					CoreAbility.getAbility(player, WaterPinwheel.class).onClick();
				} else if (abil.equalsIgnoreCase("surge") && CoreAbility.hasAbility(player, WaterSphere.class)) {
					CoreAbility.getAbility(player, WaterSphere.class).onClick();
				} else if (abil.equalsIgnoreCase("torrent") && CoreAbility.hasAbility(player, WaterSlash.class)) {
					CoreAbility.getAbility(player, WaterSlash.class).onClick();
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("dustdevil")) {
					if (CoreAbility.hasAbility(player, DustDevil.class)) {
						if (CoreAbility.hasAbility(player, DustDevilRush.class)) {
							CoreAbility.getAbility(player, DustDevilRush.class).remove();
						} else {
							CoreAbility.getAbility(player, DustDevil.class).remove();
						}
					} else {
						new DustDevil(player);
					}
				} else if (abil.equalsIgnoreCase("glassshards")) {
					if (CoreAbility.hasAbility(player, GlassShards.class)) {
						CoreAbility.getAbility(player, GlassShards.class).onClick();
					}
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("heatcontrol") && CoreAbility.hasAbility(player, FireAugmentation.class)) {
					CoreAbility.getAbility(player, FireAugmentation.class).onClick();
				} else if (abil.equalsIgnoreCase("firedaggers")) {
					if (!player.isSneaking()) {
						if (CoreAbility.hasAbility(player, FireDaggers.class)) {
							CoreAbility.getAbility(player, FireDaggers.class).onClick();
						} else {
							new FireDaggers(player);
						}
					} else if (CoreAbility.hasAbility(player, FireDaggers.class) && player.isSneaking()) {
						CoreAbility.getAbility(player, FireDaggers.class).onJumpSneakClick();
					}
				} else if (abil.equalsIgnoreCase("electrify")) {
					new Electrify(player);
				}
			}
			
			if (coreAbil instanceof ChiAbility && bPlayer.isElementToggled(Element.CHI) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (event.isCancelled() || bPlayer == null) {
			return;
		}
		
		String abilName = bPlayer.getBoundAbilityName();
		if (Suffocate.isBreathbent(player)) {
			if (!abilName.equalsIgnoreCase("airswipe")
					|| !abilName.equalsIgnoreCase("fireblast")
					|| !abilName.equalsIgnoreCase("earthblast")
					|| !abilName.equalsIgnoreCase("watermanipulation")) {
				if (!player.isSneaking()) {
					event.setCancelled(true);
				}
			}
		}
		
		if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
			event.setCancelled(true);
			return;
		}
		
		if (bPlayer.isElementToggled(Element.CHI) == true) {
			new Duck(player);
			new Parry(player);
		}
		
		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		if (coreAbil == null) {
			return;
		}
		
		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}
		
		// Start sneaking
		if (!player.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
			}
			
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("plantwhip")) {
					new PlantWhip(player);
				} else if (abil.equalsIgnoreCase("healinghands")) {
					new HealingHands(player);
				} else if (abil.equalsIgnoreCase("watercanvas")) {
					new WaterCanvas(player);
				} else if (abil.equalsIgnoreCase("bloodstrangle")) {
					new BloodStrangle(player);
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("glassshards")) {
					new GlassShards(player, false);
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("firedaggers") && CoreAbility.hasAbility(player, FireDaggers.class)) {
					CoreAbility.getAbility(player, FireDaggers.class).onSneak();
				}
			}
			
			if (coreAbil instanceof ChiAbility && bPlayer.isElementToggled(Element.CHI) == true) {				
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
					return;
				}
			}
		}
		// Releasing sneak
		if (player.isSneaking() && bPlayer.canBendIgnoreBindsCooldowns(coreAbil)) {
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("firedaggers") && CoreAbility.hasAbility(player, FireDaggers.class)) {
					CoreAbility.getAbility(player, FireDaggers.class).onJumpReleaseSneak();
				}
			}
		}
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}
		if (bPlayer == null) {
			return;
		}
		if (Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		} else if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
			event.setCancelled(true);
			return;
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		} else if (GeneralMethods.isInteractable(player.getTargetBlock((Set<Material>)null, 5))) {
			return;
		}
		
		if (bPlayer.isToggled() && !AzutoruMethods.isOnGround(player) && player.isSneaking()) {
			new Dodge(player);
		}
		
		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		
		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				
				if (abil.equalsIgnoreCase("glassshards")) {
					new GlassShards(player, true);
				} else if (abil.equalsIgnoreCase("lavaflow") && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
					if (LavaWalk.isActive()) {
						LavaWalk.setActive(false);
						player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now disabled.");
					} else {
						LavaWalk.setActive(true);
						player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now enabled.");
					}
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onLeafDecay(LeavesDecayEvent event) {
		if (event.getBlock().hasMetadata("PlantWhip")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onReload(BendingReloadEvent event) {
		Azutoru.az.config().reload();
		event.getSender().sendMessage(Azutoru.az.prefix() + " Config reloaded.");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				CoreAbility.registerPluginAbilities(plugin, "me.aztl.azutoru.ability");
			}
		}.runTaskLater(Azutoru.az, 1);
	}
	
	@EventHandler
	public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (CoreAbility.hasAbility(player, AirCocoon.class)) {
				event.setCancelled(true);
			}
			if (CoreAbility.hasAbility(player, Duck.class) && CoreAbility.getAbility(player, Duck.class).isDucking()) {
				event.setCancelled(true);
				CoreAbility.getAbility(player, Duck.class).removeWithCooldown();
			}
			if (CoreAbility.hasAbility(player, Parry.class)) {
				Entity damager = event.getDamager();
				if (AzutoruMethods.getNonParryableMobs().contains(damager.getType())
						|| damager instanceof Projectile) {
					return;
				}
				
				double distance = Azutoru.az.getConfig().getDouble("Abilities.Chi.Parry.MaxDistance");
				if (damager.getLocation().distanceSquared(player.getLocation()) < distance) {
					event.setCancelled(true);
					CoreAbility.getAbility(player, Parry.class).removeWithCooldown();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamageByBlock(EntityDamageByBlockEvent event) {
		if (event.getEntity() instanceof Player && event.getCause().equals(DamageCause.SUFFOCATION)) {
			Block damager = event.getDamager();
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.BendingTempBlocks") == true) {
				if (damager instanceof TempBlock || EarthAbility.getMovedEarth().containsKey(damager)) {
					event.setCancelled(true);
				}
			}
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllBlocks") == true) {
				event.setCancelled(true);
			}
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllIceBlocks") == true) {
				if (ElementalAbility.isIce(damager.getType())) {
					event.setCancelled(true);
				}
			}
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllEarthBlocks") == true) {
				if (ElementalAbility.isEarth(damager)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		if (event.getTo().getX() == event.getFrom().getX()
				&& event.getTo().getY() == event.getFrom().getY()
				&& event.getTo().getZ() == event.getFrom().getZ()) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if (CoreAbility.hasAbility(player, DustDevil.class) && !CoreAbility.hasAbility(player, DustDevilRush.class)) {
			Vector velocity = new Vector();
			velocity.setX(event.getTo().getX() - event.getFrom().getX());
			velocity.setZ(event.getTo().getZ() - event.getFrom().getZ());
			
			player.setVelocity(velocity.multiply(0.2));
			
			return;
		}
	}
}
