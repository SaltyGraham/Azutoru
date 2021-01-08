package me.aztl.azutoru.listener;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.air.CloudSurf;
import me.aztl.azutoru.ability.air.combo.AirCocoon;
import me.aztl.azutoru.ability.air.combo.AirSpoutRush;
import me.aztl.azutoru.ability.chi.passive.Dodge;
import me.aztl.azutoru.ability.chi.passive.Duck;
import me.aztl.azutoru.ability.chi.passive.Parry;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.RaiseEarth.Orientation;
import me.aztl.azutoru.ability.earth.Shockwave;
import me.aztl.azutoru.ability.earth.combo.EarthRidge;
import me.aztl.azutoru.ability.earth.glass.GlassShards;
import me.aztl.azutoru.ability.earth.lava.passive.LavaWalk;
import me.aztl.azutoru.ability.earth.metal.multiability.MetalCables;
import me.aztl.azutoru.ability.earth.passive.EarthShield;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.ability.earth.sand.combo.DustDevilRush;
import me.aztl.azutoru.ability.earth.sand.combo.DustStepping;
import me.aztl.azutoru.ability.fire.FireDaggers;
import me.aztl.azutoru.ability.fire.FireJet;
import me.aztl.azutoru.ability.fire.FireWhips;
import me.aztl.azutoru.ability.fire.combo.JetStepping;
import me.aztl.azutoru.ability.fire.lightning.Electrify;
import me.aztl.azutoru.ability.water.WaterCanvas;
import me.aztl.azutoru.ability.water.blood.BloodStrangle;
import me.aztl.azutoru.ability.water.combo.RazorRings;
import me.aztl.azutoru.ability.water.combo.WaterPinwheel;
import me.aztl.azutoru.ability.water.combo.WaterRun;
import me.aztl.azutoru.ability.water.combo.WaterSlash;
import me.aztl.azutoru.ability.water.combo.WaterSphere;
import me.aztl.azutoru.ability.water.combo.WaterSpoutRush;
import me.aztl.azutoru.ability.water.ice.combo.IceRidge;
import me.aztl.azutoru.ability.water.ice.combo.IceShots;
import me.aztl.azutoru.ability.water.ice.combo.MistStepping;
import me.aztl.azutoru.ability.water.multiability.Transform;
import me.aztl.azutoru.ability.water.plant.PlantWhip;
import me.aztl.azutoru.util.MathUtil;
import me.aztl.azutoru.util.PlayerUtil;
import me.aztl.azutoru.util.TorrentRedirection;
import me.aztl.azutoru.util.WorldUtil;

public class AzutoruListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (event.getHand() != EquipmentSlot.HAND
				|| (event.getAction() != Action.LEFT_CLICK_AIR
				&& event.getAction() != Action.LEFT_CLICK_BLOCK
				&& event.getAction() != Action.RIGHT_CLICK_AIR
				&& event.getAction() != Action.RIGHT_CLICK_BLOCK)
				|| bPlayer == null
				|| bPlayer.isChiBlocked()
				|| player.getTargetBlock(null, 5).getType().isInteractable()
				|| Suffocate.isBreathbent(player)
				|| Bloodbending.isBloodbent(player)
				|| MovementHandler.isStopped(player)) {
			return;
		}
		
		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		Material item = player.getInventory().getItemInMainHand().getType();
		
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (coreAbil == null && MultiAbilityManager.hasMultiAbilityBound(player)) {
				if (MultiAbilityManager.hasMultiAbilityBound(player, "MetalCables")) {
					new MetalCables(player, ClickType.LEFT_CLICK);
				}
				// Additional multiabilities go here
			} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
				if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.AIR)) return;
					
					if (abil.equalsIgnoreCase("AirSpout") && CoreAbility.hasAbility(player, AirSpoutRush.class)) {
						CoreAbility.getAbility(player, AirSpoutRush.class).remove();
					} else if (abil.equalsIgnoreCase("CloudSurf")) {
						new CloudSurf(player);
					}
				}
				
				if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) return;
					
					if (abil.equalsIgnoreCase("DustDevil")) {
						new DustDevil(player);
					} else if (abil.equalsIgnoreCase("GlassShards") && CoreAbility.hasAbility(player, GlassShards.class)) {
						CoreAbility.getAbility(player, GlassShards.class).onClick();
					} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
						new RaiseEarth(player, ClickType.LEFT_CLICK);
					} else if (abil.equalsIgnoreCase("EarthBlast") && CoreAbility.hasAbility(player, EarthRidge.class)) {
						CoreAbility.getAbility(player, EarthRidge.class).onClick();
					} else if (abil.equalsIgnoreCase("EarthBlast") && CoreAbility.hasAbility(player, DustStepping.class)) {
						CoreAbility.getAbility(player, DustStepping.class).step();
					} else if (coreAbil.equals(CoreAbility.getAbility(MetalCables.class))) {
						new MetalCables(player, ClickType.LEFT_CLICK);
					} else if (coreAbil.equals(CoreAbility.getAbility(Shockwave.class)) && CoreAbility.hasAbility(player, Shockwave.class)) {
						CoreAbility.getAbility(player, Shockwave.class).onClick();
					}
				}
				
				if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) return;
					
					if (abil.equalsIgnoreCase("FireDaggers")) {
						new FireDaggers(player);
					} else if (abil.equalsIgnoreCase("Electrify")) {
						new Electrify(player);
					} else if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
						new FireJet(player, ClickType.LEFT_CLICK);
					} else if (abil.equalsIgnoreCase("Blaze") & CoreAbility.hasAbility(player, JetStepping.class)) {
						CoreAbility.getAbility(player, JetStepping.class).step();
					} else if (abil.equalsIgnoreCase("FireWhips")) {
						new FireWhips(player);
					}
				}
				
				if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.WATER)) return;
					
					if (abil.equalsIgnoreCase("PlantWhip") && CoreAbility.hasAbility(player, PlantWhip.class)) {
						CoreAbility.getAbility(player, PlantWhip.class).onClick();
					} else if (abil.equalsIgnoreCase("Surge") && CoreAbility.hasAbility(player, RazorRings.class)) {
						new RazorRings(player);
					} else if (abil.equalsIgnoreCase("Transform")) {
						new Transform(player);
					} else if (abil.equalsIgnoreCase("WaterSpout") && CoreAbility.hasAbility(player, WaterRun.class)) {
						CoreAbility.getAbility(player, WaterRun.class).remove();
					} else if (abil.equalsIgnoreCase("WaterSpout") && CoreAbility.hasAbility(player, WaterSpoutRush.class)) {
						CoreAbility.getAbility(player, WaterSpoutRush.class).remove();
					} else if (abil.equalsIgnoreCase("IceSpike") && CoreAbility.hasAbility(player, IceShots.class)) {
						CoreAbility.getAbility(player, IceShots.class).onClick();
					} else if (abil.equalsIgnoreCase("Surge") && CoreAbility.hasAbility(player, WaterPinwheel.class)) {
						CoreAbility.getAbility(player, WaterPinwheel.class).onClick();
					} else if (abil.equalsIgnoreCase("Surge") && CoreAbility.hasAbility(player, WaterSphere.class)) {
						CoreAbility.getAbility(player, WaterSphere.class).onClick();
					} else if (abil.equalsIgnoreCase("Torrent") && CoreAbility.hasAbility(player, WaterSlash.class)) {
						CoreAbility.getAbility(player, WaterSlash.class).onClick();
					} else if (abil.equalsIgnoreCase("IceSpike") && CoreAbility.hasAbility(player, MistStepping.class)) {
						CoreAbility.getAbility(player, MistStepping.class).step();
					} else if (abil.equalsIgnoreCase("IceSpike") && CoreAbility.hasAbility(player, IceRidge.class)) {
						CoreAbility.getAbility(player, IceRidge.class).onClick();
					}
				}
			}
		} else { // Right-click
			if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) return;
			
			if (bPlayer.isToggled() && !PlayerUtil.isOnGround(player) && player.isSneaking()) {
				new Dodge(player);
			}
			
			if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
				if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) return;
					
					if (abil.equalsIgnoreCase("GlassShards")) {
						new GlassShards(player, true);
					} else if (abil.equalsIgnoreCase("LavaFlow") && item == Material.AIR) {
						LavaWalk.toggle(player);
					} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
						new RaiseEarth(player, ClickType.RIGHT_CLICK);
					}
				}
				
				if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE)) {
					if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) return;
					
					if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
						new FireJet(player, ClickType.RIGHT_CLICK);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (Suffocate.isBreathbent(player)
				|| Bloodbending.isBloodbent(player)
				|| MovementHandler.isStopped(player)
				|| bPlayer.isChiBlocked())
			return;
		
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		String abil = bPlayer.getBoundAbilityName();
		Material item = player.getInventory().getItemInMainHand().getType();
		
		if (bPlayer.isElementToggled(Element.CHI)) {
			if (Azutoru.az.getConfig().getBoolean("Abilities.Chi.Parry.Enabled")) {
				new Parry(player);
			}
			if (Azutoru.az.getConfig().getBoolean("Abilities.Chi.Duck.Enabled")) {
				new Duck(player);
			}
		}
		
		if (coreAbil == null) {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				if (MultiAbilityManager.hasMultiAbilityBound(player, "MetalCables")) {
					new MetalCables(player, ClickType.SHIFT_DOWN);
				}
				// Additional multiabilities go here
			} else return;
		}
		
		if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (!player.isSneaking()) {
				// Starting to sneak
				if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.WATER)) return;
					
					if (abil.equalsIgnoreCase("PlantWhip")) {
						new PlantWhip(player);
					} else if (abil.equalsIgnoreCase("WaterCanvas")) {
						new WaterCanvas(player);
					} else if (abil.equalsIgnoreCase("BloodStrangle")) {
						new BloodStrangle(player);
					} else if (abil.equalsIgnoreCase("Torrent") && TorrentRedirection.canRedirect(player)) {
						Torrent to = new Torrent(player, false);
						to.setFormed(true);
					}
				}
				
				if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) return;
					
					if (abil.equalsIgnoreCase("GlassShards")) {
						new GlassShards(player, false);
					} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
						new RaiseEarth(player, ClickType.SHIFT_DOWN);
					} else if (abil.equalsIgnoreCase("EarthSmash")) {
						new EarthShield(player);
					} else if (coreAbil.equals(CoreAbility.getAbility(Shockwave.class))) {
						new Shockwave(player, false);
					}
				}
				
				if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) return;
					
					if (abil.equalsIgnoreCase("FireDaggers") && CoreAbility.hasAbility(player, FireDaggers.class)) {
						CoreAbility.getAbility(player, FireDaggers.class).onSneak();
					} else if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
						new FireJet(player, ClickType.SHIFT_DOWN);
					}
				}
			} else {
				// Releasing sneak
				if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE)) {
					if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) return;
					
					if (abil.equalsIgnoreCase("FireDaggers") && CoreAbility.hasAbility(player, FireDaggers.class)) {
						CoreAbility.getAbility(player, FireDaggers.class).onJumpReleaseSneak();
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAbilityStart(AbilityStartEvent event) {
		if (event.getAbility() instanceof Collapse) {
			Block block = ((Collapse) event.getAbility()).getBlock();
			if (RaiseEarth.isRaiseEarthBlock(block)) {
				RaiseEarth re = RaiseEarth.getAffectedBlocks().get(block);
				if (re.getColumns().get(0).getOrientation() == Orientation.HORIZONTAL || MathUtil.getFaceDirection(re.getFace()).equals(new Vector(0, -1, 0))) {
					re.removeAllColumns();
					EarthAbility.playEarthbendingSound(block.getLocation());
					event.setCancelled(true);
					return;
				}
				re.removeAllColumns(false, false);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onLeafDecay(LeavesDecayEvent event) {
		TempBlock tb = TempBlock.get(event.getBlock());
		if (tb != null && PlantWhip.getAffectedBlocks().contains(tb)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onReload(BendingReloadEvent event) {
		Azutoru.az.reloadConfig();
		event.getSender().sendMessage(Azutoru.az.prefix() + " Config reloaded.");
		RaiseEarth.removeAllCleanup();
		event.getSender().sendMessage(Azutoru.az.prefix() + " Cleaned up earth blocks.");
		
		new BukkitRunnable() {
			@Override
			public void run() {
				CoreAbility.registerPluginAbilities(Azutoru.az, "me.aztl.azutoru.ability");
			}
		}.runTaskLater(Azutoru.az, 1);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (CoreAbility.hasAbility(player, AirCocoon.class)) {
				event.setCancelled(true);
				return;
			}
			if (CoreAbility.hasAbility(player, Duck.class) && CoreAbility.getAbility(player, Duck.class).isDucking()) {
				CoreAbility.getAbility(player, Duck.class).removeWithCooldown();
				event.setCancelled(true);
				return;
			}
			if (CoreAbility.hasAbility(player, Parry.class)) {
				Entity damager = event.getDamager();
				if (WorldUtil.isNonParryableMob(damager) || damager instanceof Projectile) return;
				
				double distance = Azutoru.az.getConfig().getDouble("Abilities.Chi.Parry.MaxDistance");
				if (damager.getLocation().distanceSquared(player.getLocation()) < distance * distance) {
					CoreAbility.getAbility(player, Parry.class).removeWithCooldown();
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		if (event.getCause() == DamageCause.SUFFOCATION) {
			Block damager = ((Player) event.getEntity()).getEyeLocation().getBlock();
			if (damager == null) return;
			
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllBlocks")) {
				event.setCancelled(true);
				return;
			}
			
			List<Block> surrounding = GeneralMethods.getBlocksAroundPoint(damager.getLocation(), 1);
			
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllIceBlocks")) {
				if (ElementalAbility.isIce(damager.getType())
						|| surrounding.stream().anyMatch(b -> ElementalAbility.isIce(b))) {
					event.setCancelled(true);
					return;
				}
			}
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllEarthBlocks")) {
				if (ElementalAbility.isEarth(damager)
						|| surrounding.stream().anyMatch(b -> ElementalAbility.isEarth(b))) {
					event.setCancelled(true);
					return;
				}
			}
			if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.BendingTempBlocks")) {
				if (damager instanceof TempBlock
						|| TempBlock.isTouchingTempBlock(damager)
						|| EarthAbility.getMovedEarth().containsKey(damager)
						|| surrounding.stream().anyMatch(b -> EarthAbility.getMovedEarth().containsKey(b))) {
					event.setCancelled(true);
					return;
				}
			}
		} else if (event.getCause() == DamageCause.FALL && Azutoru.az.getConfig().getBoolean("Abilities.Earth.Shockwave.Enabled")) {
			Player player = (Player) event.getEntity();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			CoreAbility coreAbil = bPlayer.getBoundAbility();
			if (bPlayer == null || coreAbil == null) return;
			
			if (bPlayer.getBoundAbility().equals(CoreAbility.getAbility(Shockwave.class)) && bPlayer.isElementToggled(Element.EARTH)) {
				new Shockwave(player, true);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getTo().getX() == event.getFrom().getX()
				&& event.getTo().getY() == event.getFrom().getY()
				&& event.getTo().getZ() == event.getFrom().getZ())
			return;
		
		Player player = event.getPlayer();
		
		if (CoreAbility.hasAbility(player, DustDevil.class) && !CoreAbility.hasAbility(player, DustDevilRush.class)) {
			Vector velocity = new Vector();
			velocity.setX(event.getTo().getX() - event.getFrom().getX());
			velocity.setZ(event.getTo().getZ() - event.getFrom().getZ());
			
			player.setVelocity(velocity.multiply(0.2));
			
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntityType().equals(EntityType.FALLING_BLOCK) && event.getEntity().hasMetadata("Crumble")) {
			event.setCancelled(true);
		}
	}
}
