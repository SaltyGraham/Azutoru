package me.aztl.azutoru;

import java.util.Set;

import org.bukkit.ChatColor;
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

import me.aztl.azutoru.ability.air.CloudSurf;
import me.aztl.azutoru.ability.air.combo.AirCocoon;
import me.aztl.azutoru.ability.air.combo.AirSpoutRush;
import me.aztl.azutoru.ability.chi.passive.Dodge;
import me.aztl.azutoru.ability.chi.passive.Duck;
import me.aztl.azutoru.ability.chi.passive.Parry;
import me.aztl.azutoru.ability.earth.EarthRidge;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.RaiseEarth.Orientation;
import me.aztl.azutoru.ability.earth.glass.GlassShards;
import me.aztl.azutoru.ability.earth.lava.passive.LavaWalk;
import me.aztl.azutoru.ability.earth.passive.EarthShield;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.ability.earth.sand.combo.DustDevilRush;
import me.aztl.azutoru.ability.earth.sand.combo.DustStepping;
import me.aztl.azutoru.ability.fire.FireDaggers;
import me.aztl.azutoru.ability.fire.FireJet;
import me.aztl.azutoru.ability.fire.FireWhips;
import me.aztl.azutoru.ability.fire.combo.JetBlast;
import me.aztl.azutoru.ability.fire.combo.JetBlaze;
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
import me.aztl.azutoru.ability.water.ice.IceRidge;
import me.aztl.azutoru.ability.water.ice.combo.IceShots;
import me.aztl.azutoru.ability.water.ice.combo.MistStepping;
import me.aztl.azutoru.ability.water.multiability.Transform;
import me.aztl.azutoru.ability.water.plant.PlantWhip;
import me.aztl.azutoru.util.TorrentRedirection;

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
		Material item = player.getInventory().getItemInMainHand().getType();
		
		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.AIR)) {
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
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.WATER)) {
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
				} else if (abil.equalsIgnoreCase("icespike") && CoreAbility.hasAbility(player, MistStepping.class)) {
					CoreAbility.getAbility(player, MistStepping.class).step();
				} else if (abil.equalsIgnoreCase("iceridge") && CoreAbility.hasAbility(player, IceRidge.class)) {
					CoreAbility.getAbility(player, IceRidge.class).onClick();
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) {
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
				} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
					new RaiseEarth(player, ClickType.LEFT_CLICK);
				} else if (abil.equalsIgnoreCase("earthridge") && CoreAbility.hasAbility(player, EarthRidge.class)) {
					CoreAbility.getAbility(player, EarthRidge.class).onClick();
				} else if (abil.equalsIgnoreCase("earthblast") && CoreAbility.hasAbility(player, DustStepping.class)) {
					CoreAbility.getAbility(player, DustStepping.class).step();
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) {
					return;
				}
				
				if (abil.equalsIgnoreCase("firedaggers")) {
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
				} else if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
					if (CoreAbility.hasAbility(player, FireJet.class)) {
						CoreAbility.getAbility(player, FireJet.class).onLeftClick();
					} else if (CoreAbility.hasAbility(player, JetBlast.class)
							|| CoreAbility.hasAbility(player, JetBlaze.class)) {
						return;
					} else {
						new FireJet(player, ClickType.LEFT_CLICK);
					}
				} else if (abil.equalsIgnoreCase("blaze") & CoreAbility.hasAbility(player, JetStepping.class)) {
					CoreAbility.getAbility(player, JetStepping.class).step();
				} else if (abil.equalsIgnoreCase("firewhips")) {
					if (CoreAbility.hasAbility(player, FireWhips.class)) {
						CoreAbility.getAbility(player, FireWhips.class).onClick();
					} else {
						new FireWhips(player);
					}
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

		String abil = bPlayer.getBoundAbilityName();
		if (Suffocate.isBreathbent(player)) {
			if (!abil.equalsIgnoreCase("airswipe")
					|| !abil.equalsIgnoreCase("fireblast")
					|| !abil.equalsIgnoreCase("earthblast")
					|| !abil.equalsIgnoreCase("watermanipulation")) {
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
			if (Azutoru.az.getConfig().getBoolean("Abilities.Chi.Parry.Enabled")) {
				new Parry(player);
			}
			if (Azutoru.az.getConfig().getBoolean("Abilities.Chi.Duck.Enabled")) {
				new Duck(player);
			}
		}
		
		CoreAbility coreAbil = bPlayer.getBoundAbility();
		if (coreAbil == null) {
			return;
		}
		Material item = player.getInventory().getItemInMainHand().getType();
		
		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}
		
		// Start sneaking
		if (!player.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.WATER)) {
					return;
				}
				
				if (abil.equalsIgnoreCase("plantwhip")) {
					new PlantWhip(player);
				} else if (abil.equalsIgnoreCase("watercanvas")) {
					new WaterCanvas(player);
				} else if (abil.equalsIgnoreCase("bloodstrangle")) {
					new BloodStrangle(player);
				} else if (abil.equalsIgnoreCase("torrent") && TorrentRedirection.canRedirect(player)) {
					Torrent to = new Torrent(player, false);
					to.setFormed(true);
				} else if (abil.equalsIgnoreCase("iceridge")) {
					new IceRidge(player);
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) {
					return;
				}
				
				if (abil.equalsIgnoreCase("glassshards")) {
					new GlassShards(player, false);
				} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
					new RaiseEarth(player, ClickType.SHIFT_DOWN);
				} else if (abil.equalsIgnoreCase("earthridge")) {
					new EarthRidge(player);
				} else if (abil.equalsIgnoreCase("earthsmash")) {
					new EarthShield(player);
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) {
					return;
				}
				
				if (abil.equalsIgnoreCase("firedaggers") && CoreAbility.hasAbility(player, FireDaggers.class)) {
					CoreAbility.getAbility(player, FireDaggers.class).onSneak();
				} else if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
					if (CoreAbility.hasAbility(player, FireJet.class)) {
						CoreAbility.getAbility(player, FireJet.class).onSneak();
					} else {
						new FireJet(player, ClickType.SHIFT_DOWN);
					}
				}
			}
		}
		// Releasing sneak
		if (player.isSneaking() && bPlayer.canBendIgnoreBindsCooldowns(coreAbil)) {
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) {
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
		Material item = player.getInventory().getItemInMainHand().getType();
		
		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(item) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.EARTH)) {
					return;
				}
				
				if (abil.equalsIgnoreCase("glassshards")) {
					new GlassShards(player, true);
				} else if (abil.equalsIgnoreCase("lavaflow") && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
					if (LavaWalk.isActive(player)) {
						LavaWalk.setActive(player, false);
						player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now disabled.");
					} else {
						LavaWalk.setActive(player, true);
						player.sendMessage(ChatColor.DARK_GREEN + "LavaWalk is now enabled.");
					}
				} else if (coreAbil.equals(CoreAbility.getAbility(RaiseEarth.class))) {
					new RaiseEarth(player, ClickType.RIGHT_CLICK);
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && GeneralMethods.getElementsWithNoWeaponBending().contains(Element.FIRE)) {
					return;
				}
				
				if (coreAbil.equals(CoreAbility.getAbility(FireJet.class))) {
					if (CoreAbility.hasAbility(player, FireJet.class)) {
						CoreAbility.getAbility(player, FireJet.class).onRightClick();
					} else {
						new FireJet(player, ClickType.RIGHT_CLICK);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onAbilityStart(AbilityStartEvent event) {
		if (event.getAbility() instanceof Collapse) {
			Collapse collapse = (Collapse) event.getAbility();
			Block block = collapse.getBlock();
			if (RaiseEarth.isRaiseEarthBlock(block)) {
				RaiseEarth re = RaiseEarth.getAffectedBlocks().get(block);
				if (re.getColumns().get(0).getOrientation() == Orientation.HORIZONTAL || AzutoruMethods.getFaceDirection(re.getFace()).equals(new Vector(0, -1, 0))) {
					re.removeAllColumns();
					EarthAbility.playEarthbendingSound(block.getLocation());
					event.setCancelled(true);
					return;
				}
				re.removeAllColumns(false, false);
			}
		}
	}
	
	@EventHandler
	public void onLeafDecay(LeavesDecayEvent event) {
		Block block = event.getBlock();
		if (TempBlock.isTempBlock(block) && PlantWhip.getAffectedBlocks().contains(TempBlock.get(block))) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onReload(BendingReloadEvent event) {
		Azutoru.az.reloadConfig();
		event.getSender().sendMessage(Azutoru.az.prefix() + " Config reloaded.");
		RaiseEarth.removeAllCleanup();
		event.getSender().sendMessage(Azutoru.az.prefix() + " Cleaned up earth blocks.");
		
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
				if (AzutoruMethods.isNonParryableMob(damager) || damager instanceof Projectile) {
					return;
				}
				
				double distance = Azutoru.az.getConfig().getDouble("Abilities.Chi.Parry.MaxDistance");
				if (damager.getLocation().distanceSquared(player.getLocation()) < distance) {
					event.setCancelled(true);
					CoreAbility.getAbility(player, Parry.class).removeWithCooldown();
					player.setSneaking(false);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			
		}
		if (event.getEntity() instanceof Player && event.getCause().equals(DamageCause.SUFFOCATION)) {
			Player player = (Player) event.getEntity();
			Block damager = player.getEyeLocation().getBlock();
			if (damager != null) {
				if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllBlocks") == true) {
					event.setCancelled(true);
					return;
				}
				if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllIceBlocks") == true) {
					if (ElementalAbility.isIce(damager.getType())) {
						event.setCancelled(true);
						return;
					} else {
						for (Block b : GeneralMethods.getBlocksAroundPoint(damager.getLocation(), 1)) {
							if (ElementalAbility.isIce(b)) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.AllEarthBlocks") == true) {
					if (ElementalAbility.isEarth(damager)) {
						event.setCancelled(true);
						return;
					} else {
						for (Block b : GeneralMethods.getBlocksAroundPoint(damager.getLocation(), 1)) {
							if (ElementalAbility.isEarth(b)) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				if (Azutoru.az.getConfig().getBoolean("Properties.PreventSuffocation.BendingTempBlocks") == true) {
					if (damager instanceof TempBlock) {
						event.setCancelled(true);
						return;
					} else if (TempBlock.isTouchingTempBlock(damager)) {
						event.setCancelled(true);
						return;
					} else if (EarthAbility.getMovedEarth().containsKey(damager)) {
						event.setCancelled(true);
						return;
					} else {
						for (Block b : GeneralMethods.getBlocksAroundPoint(damager.getLocation(), 1)) {
							if (EarthAbility.getMovedEarth().containsKey(b)) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
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
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntityType().equals(EntityType.FALLING_BLOCK) && event.getEntity().hasMetadata("Crumble")) {
			event.setCancelled(true);
		}
	}
}
