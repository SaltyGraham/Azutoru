package me.aztl.azutoru.config;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.CollisionInitializer;
import com.projectkorra.projectkorra.ability.util.CollisionManager;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.FireBurst;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

import me.aztl.azutoru.Azutoru;
import me.aztl.azutoru.ability.air.combo.AirCocoon;
import me.aztl.azutoru.ability.air.combo.AirSpoutRush;
import me.aztl.azutoru.ability.air.combo.AirWake;
import me.aztl.azutoru.ability.earth.EarthRidge;
import me.aztl.azutoru.ability.earth.RaiseEarth;
import me.aztl.azutoru.ability.earth.combo.Crumble;
import me.aztl.azutoru.ability.earth.sand.DustDevil;
import me.aztl.azutoru.ability.fire.FireDaggers;
import me.aztl.azutoru.ability.fire.bluefire.combo.Evaporate;
import me.aztl.azutoru.ability.fire.combo.FireAugmentation;
import me.aztl.azutoru.ability.fire.combo.FireBlade;
import me.aztl.azutoru.ability.fire.combo.FireStreams;
import me.aztl.azutoru.ability.util.Shot;
import me.aztl.azutoru.ability.water.combo.RazorRings;
import me.aztl.azutoru.ability.water.combo.WaterSlash;
import me.aztl.azutoru.ability.water.combo.WaterSphere;
import me.aztl.azutoru.ability.water.combo.WaterSpoutRush;
import me.aztl.azutoru.ability.water.ice.combo.IceShots;
import me.aztl.azutoru.ability.water.plant.PlantWhip;

public class AzutoruConfig {
	
	public AzutoruConfig() {
		setupMainConfig();
		setupCollisions();
	}
	
	public void setupCollisions() {		
		CollisionManager manager = ProjectKorra.getCollisionManager();
		CollisionInitializer initializer = ProjectKorra.getCollisionInitializer();
		
		CoreAbility as = CoreAbility.getAbility(AirSwipe.class);
		CoreAbility fb = CoreAbility.getAbility(FireBlast.class);
		CoreAbility eb = CoreAbility.getAbility(EarthBlast.class);
		CoreAbility wm = CoreAbility.getAbility(WaterManipulation.class);
		CoreAbility li = CoreAbility.getAbility(Lightning.class);
		CoreAbility to = CoreAbility.getAbility(Torrent.class);
		CoreAbility es = CoreAbility.getAbility(EarthSmash.class);
		CoreAbility abu = CoreAbility.getAbility(AirBurst.class);
		CoreAbility fbu = CoreAbility.getAbility(FireBurst.class);
		CoreAbility cfb = CoreAbility.getAbility(FireBlastCharged.class);
		
		CoreAbility aw = CoreAbility.getAbility(AirWake.class);
		CoreAbility ac = CoreAbility.getAbility(AirCocoon.class);
		CoreAbility asr = CoreAbility.getAbility(AirSpoutRush.class);
		CoreAbility cr = CoreAbility.getAbility(Crumble.class);
		CoreAbility dd = CoreAbility.getAbility(DustDevil.class);
		CoreAbility re = CoreAbility.getAbility(RaiseEarth.class);
		CoreAbility er = CoreAbility.getAbility(EarthRidge.class);
		CoreAbility ev = CoreAbility.getAbility(Evaporate.class);
		CoreAbility faug = CoreAbility.getAbility(FireAugmentation.class);
		CoreAbility fbl = CoreAbility.getAbility(FireBlade.class);
		CoreAbility fs = CoreAbility.getAbility(FireStreams.class);
		CoreAbility fd = CoreAbility.getAbility(FireDaggers.class);
		CoreAbility rr = CoreAbility.getAbility(RazorRings.class);
		CoreAbility ws = CoreAbility.getAbility(WaterSlash.class);
		CoreAbility wsph = CoreAbility.getAbility(WaterSphere.class);
		CoreAbility wsr = CoreAbility.getAbility(WaterSpoutRush.class);
		CoreAbility ish = CoreAbility.getAbility(IceShots.class);
		CoreAbility pw = CoreAbility.getAbility(PlantWhip.class);
		CoreAbility shot = CoreAbility.getAbility(Shot.class);
		
		// AirWake
		if (aw != null) {
			initializer.addSmallAbility(aw);
			initializer.addComboAbility(aw);
			initializer.addRemoveSpoutAbility(aw);
		}
		
		// AirCocoon
		if (ac != null) {
			manager.addCollision(new Collision(ac, as, false, true));
			manager.addCollision(new Collision(ac, fb, false, true));
			manager.addCollision(new Collision(ac, eb, false, true));
			manager.addCollision(new Collision(ac, wm, false, true));
			manager.addCollision(new Collision(ac, li, true, false));
			manager.addCollision(new Collision(ac, CoreAbility.getAbility("Combustion"), true, false));
			manager.addCollision(new Collision(ac, cfb, true, false));
			manager.addCollision(new Collision(ac, to, false, true));
			manager.addCollision(new Collision(ac, es, true, false));
			manager.addCollision(new Collision(ac, abu, true, false));
			manager.addCollision(new Collision(ac, fbu, true, false));
			manager.addCollision(new Collision(ac, fbl, true, true));
			manager.addCollision(new Collision(ac, er, true, false));
		}
		
		// AirSpoutRush
		if (asr != null) {
			manager.addCollision(new Collision(asr, as, true, false));
			manager.addCollision(new Collision(asr, eb, true, false));
			manager.addCollision(new Collision(asr, es, true, false));
			manager.addCollision(new Collision(asr, fb, true, false));
			manager.addCollision(new Collision(asr, cfb, true, false));
			manager.addCollision(new Collision(asr, fbu, true, false));
			manager.addCollision(new Collision(asr, wm, true, false));
			manager.addCollision(new Collision(asr, ws, true, false));
			manager.addCollision(new Collision(asr, fbl, true, false));
		}
		
		// Crumble
		if (cr != null) {
			manager.addCollision(new Collision(cr, re, true, true));
			manager.addCollision(new Collision(cr, es, true, true));
			manager.addCollision(new Collision(cr, eb, false, true));
			manager.addCollision(new Collision(cr, er, true, true));
		}
		
		// DustDevil
		if (dd != null) {
			manager.addCollision(new Collision(dd, as, true, false));
			manager.addCollision(new Collision(dd, eb, true, false));
			manager.addCollision(new Collision(dd, es, true, false));
			manager.addCollision(new Collision(dd, fb, true, false));
			manager.addCollision(new Collision(dd, cfb, true, false));
			manager.addCollision(new Collision(dd, fbu, true, false));
			manager.addCollision(new Collision(dd, wm, true, false));
			manager.addCollision(new Collision(dd, ws, true, false));
			manager.addCollision(new Collision(dd, fbl, true, false));
		}
		
		// EarthRidge
		if (er != null) {
			initializer.addLargeAbility(er);
		}
		
		// RaiseEarth
		if (re != null) {
			initializer.addLargeAbility(re);
			initializer.addRemoveSpoutAbility(re);
		}
		
		// Evaporate
		if (ev != null) {
			initializer.addLargeAbility(ev);
			initializer.addRemoveSpoutAbility(ev);
			initializer.addComboAbility(ev);
			manager.addCollision(new Collision(ev, to, false, true));
			manager.addCollision(new Collision(ev, wm, false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(SurgeWave.class), false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(SurgeWall.class), false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(TorrentWave.class), false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(WaterSpoutWave.class), false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(IceSpikeBlast.class), false, true));
			manager.addCollision(new Collision(ev, CoreAbility.getAbility(IceWave.class), false, true));
		}
		
		// FireAugmentation
		if (faug != null) {
			manager.addCollision(new Collision(faug, re, true, false));
			manager.addCollision(new Collision(faug, er, true, false));
		}
		
		// FireBlade
		if (fbl != null) {
			initializer.addSmallAbility(fbl);
			initializer.addComboAbility(fbl);
			initializer.addRemoveSpoutAbility(fbl);
		}
		
		// FireStreams
		if (fs != null) {
			initializer.addLargeAbility(fs);
			initializer.addRemoveSpoutAbility(fs);
			initializer.addComboAbility(fs);
		}
		
		// FireDaggers
		if (fd != null) {
			manager.addCollision(new Collision(fd, as, false, true));
			manager.addCollision(new Collision(fd, fb, false, true));
			manager.addCollision(new Collision(fd, eb, false, true));
			manager.addCollision(new Collision(fd, wm, false, true));
			manager.addCollision(new Collision(fd, li, true, false));
			manager.addCollision(new Collision(fd, CoreAbility.getAbility("Combustion"), true, false));
			manager.addCollision(new Collision(fd, cfb, true, false));
			manager.addCollision(new Collision(fd, to, true, false));
			manager.addCollision(new Collision(fd, es, true, false));
			manager.addCollision(new Collision(fd, abu, true, false));
			manager.addCollision(new Collision(fd, fbu, true, false));
		}
		
		// RazorRings
		if (rr != null) {
			initializer.addLargeAbility(rr);
			initializer.addComboAbility(rr);
			initializer.addRemoveSpoutAbility(rr);
		}
		
		// WaterSlash
		if (ws != null) {
			initializer.addSmallAbility(ws);
			initializer.addComboAbility(ws);
			initializer.addRemoveSpoutAbility(ws);
		}
		
		// WaterSphere
		if (wsph != null) {
			initializer.addLargeAbility(wsph);
		}
		
		// WaterSpoutRush
		if (wsr != null) {
			manager.addCollision(new Collision(wsr, as, true, false));
			manager.addCollision(new Collision(wsr, eb, true, false));
			manager.addCollision(new Collision(wsr, es, true, false));
			manager.addCollision(new Collision(wsr, fb, true, false));
			manager.addCollision(new Collision(wsr, cfb, true, false));
			manager.addCollision(new Collision(wsr, fbu, true, false));
			manager.addCollision(new Collision(wsr, wm, true, false));
			manager.addCollision(new Collision(wsr, ws, true, false));
			manager.addCollision(new Collision(wsr, fbl, true, false));
		}
		
		// IceShots
		if (ish != null) {
			initializer.addLargeAbility(ish);
		}
		
		// PlantWhip
		if (pw != null) {
			initializer.addSmallAbility(pw);
		}
		
		// Shot
		if (shot != null) {
			initializer.addSmallAbility(shot);
		}
	}
	
	public void setupMainConfig() {
		FileConfiguration c = Azutoru.az.getConfig();
		
		// PROPERTIES
		
		ArrayList<String> dustBlocks = new ArrayList<String>();
		dustBlocks.add(Material.SAND.toString());
		dustBlocks.add(Material.RED_SAND.toString());
		dustBlocks.add(Material.DIRT.toString());
		dustBlocks.add(Material.COARSE_DIRT.toString());
		dustBlocks.add(Material.PODZOL.toString());
		dustBlocks.add(Material.GRAVEL.toString());
		dustBlocks.add(Material.CLAY.toString());
		dustBlocks.add(Material.GRASS_BLOCK.toString());
		dustBlocks.add(Material.GRASS_PATH.toString());
		dustBlocks.add(Material.SOUL_SAND.toString());
		dustBlocks.add(Material.SOUL_SOIL.toString());
		
		c.addDefault("Properties.Earth.DustBlocks", dustBlocks);
		
		c.addDefault("Properties.PreventSuffocation.BendingTempBlocks", true);
		c.addDefault("Properties.PreventSuffocation.AllBlocks", false);
		c.addDefault("Properties.PreventSuffocation.AllIceBlocks", false);
		c.addDefault("Properties.PreventSuffocation.AllEarthBlocks", false);
		
		// ABILITIES
		
		// AIR
		
		// AirCocoon
		c.addDefault("Abilities.Air.AirCocoon.Enabled", true);
		c.addDefault("Abilities.Air.AirCocoon.Cooldown", 6000);
		c.addDefault("Abilities.Air.AirCocoon.Duration", 2000);
		
		// AirCushion
		c.addDefault("Abilities.Air.AirCushion.Enabled", true);
		c.addDefault("Abilities.Air.AirCushion.Cooldown", 8000);
		c.addDefault("Abilities.Air.AirCushion.Duration", 4000);
		c.addDefault("Abilities.Air.AirCushion.Range", 20);
		c.addDefault("Abilities.Air.AirCushion.Radius", 3);
		c.addDefault("Abilities.Air.AirCushion.Speed", 2);
		
		// AirSpoutRush
		c.addDefault("Abilities.Air.AirSpoutRush.Enabled", true);
		c.addDefault("Abilities.Air.AirSpoutRush.Cooldown", 30000);
		c.addDefault("Abilities.Air.AirSpoutRush.Duration", 5000);
		
		// AirVortex
		
		// AirWake
		c.addDefault("Abilities.Air.AirWake.Enabled", true);
		c.addDefault("Abilities.Air.AirWake.Damage", 2);
		c.addDefault("Abilities.Air.AirWake.Knockback", 5);
		c.addDefault("Abilities.Air.AirWake.Knockup", 1);
		c.addDefault("Abilities.Air.AirWake.HitRadius", 1.5);
		c.addDefault("Abilities.Air.AirWake.Speed", 1);
		c.addDefault("Abilities.Air.AirWake.Range", 20);
		c.addDefault("Abilities.Air.AirWake.Cooldown", 8000);
		c.addDefault("Abilities.Air.AirWake.Duration", 3000);
		
		// CloudSurf
		c.addDefault("Abilities.Air.CloudSurf.Enabled", true);
		c.addDefault("Abilities.Air.CloudSurf.Duration", 5000);
		c.addDefault("Abilities.Air.CloudSurf.Cooldown", 10000);
		c.addDefault("Abilities.Air.CloudSurf.ForceCloudParticles", true);
		c.addDefault("Abilities.Air.CloudSurf.AllowSneakMoves", false);
		c.addDefault("Abilities.Air.CloudSurf.DamageThreshold", 2);
		
		
		// WATER
		
		// BloodStrangle
		c.addDefault("Abilities.Water.BloodStrangle.Enabled", true);
		c.addDefault("Abilities.Water.BloodStrangle.Range", 10);
		c.addDefault("Abilities.Water.BloodStrangle.GrabRadius", 2);
		c.addDefault("Abilities.Water.BloodStrangle.Damage", 2);
		c.addDefault("Abilities.Water.BloodStrangle.CanBendUndeadMobs", false);
		c.addDefault("Abilities.Water.BloodStrangle.Cooldown", 20000);
		c.addDefault("Abilities.Water.BloodStrangle.Duration", 30000);
		
		// HealingHands
		c.addDefault("Abilities.Water.HealingHands.Enabled", true);
		c.addDefault("Abilities.Water.HealingHands.Range", 6);
		c.addDefault("Abilities.Water.HealingHands.PotionPotency", 1);
		c.addDefault("Abilities.Water.HealingHands.Cooldown", 5000);
		
		// Hexapod
		c.addDefault("Abilities.Water.Hexapod.Enabled", true);
		c.addDefault("Abilities.Water.Hexapod.SourceRange", 12);
		c.addDefault("Abilities.Water.Hexapod.Cooldown", 30000);
		c.addDefault("Abilities.Water.Hexapod.MaxSlams", 10);
		c.addDefault("Abilities.Water.Hexapod.MaxMovements", 30);
		c.addDefault("Abilities.Water.Hexapod.MaxWingFlaps", 30);
		c.addDefault("Abilities.Water.Hexapod.InitialLength", 6);
		c.addDefault("Abilities.Water.Hexapod.HoverHeight", 3);
		
		c.addDefault("Abilities.Water.Hexapod.Slam.UsageCooldown", 200);
		c.addDefault("Abilities.Water.Hexapod.Slam.Damage", 2);
		c.addDefault("Abilities.Water.Hexapod.Slam.Knockback", 1);
		c.addDefault("Abilities.Water.Hexapod.Slam.Knockup", 0.5);
		c.addDefault("Abilities.Water.Hexapod.Slam.MaxTentacleLength", 16);
		
		c.addDefault("Abilities.Water.Hexapod.Spears.DamageEnabled", true);
		c.addDefault("Abilities.Water.Hexapod.Spears.TotalDamage", 4);
		c.addDefault("Abilities.Water.Hexapod.Spears.Length", 18);
		c.addDefault("Abilities.Water.Hexapod.Spears.Range", 30);
		c.addDefault("Abilities.Water.Hexapod.Spears.SphereRadius", 1);
		c.addDefault("Abilities.Water.Hexapod.Spears.Duration", 3000);
		
		c.addDefault("Abilities.Water.Hexapod.Spider.InitialLength", 6);
		c.addDefault("Abilities.Water.Hexapod.Spider.UsageCooldownEnabled", true);
		c.addDefault("Abilities.Water.Hexapod.Spider.Strider.UsageCooldown", 500);
		c.addDefault("Abilities.Water.Hexapod.Spider.Catapult.UsageCooldown", 2000);
		
		c.addDefault("Abilities.Water.Hexapod.Whip.RespectRegions", false);
		c.addDefault("Abilities.Water.Hexapod.Whip.UsageCooldown.Enabled", true);
		c.addDefault("Abilities.Water.Hexapod.Whip.MaxLength", 16);
		c.addDefault("Abilities.Water.Hexapod.Whip.InitialLength", 5);
		c.addDefault("Abilities.Water.Hexapod.Whip.Cling.Duration", 10000);
		c.addDefault("Abilities.Water.Hexapod.Whip.Grapple.UsageCooldown", 200);
		c.addDefault("Abilities.Water.Hexapod.Whip.Cling.UsageCooldown", 2000);
		
		c.addDefault("Abilities.Water.Hexapod.Wings.WingLength", 4);
		c.addDefault("Abilities.Water.Hexapod.Wings.Thrust", 0.25);
		c.addDefault("Abilities.Water.Hexapod.Wings.UsageCooldown", 200);
		c.addDefault("Abilities.Water.Hexapod.Wings.Lift", 1);
		c.addDefault("Abilities.Water.Hexapod.Wings.RespectRegions", false);
		
		// IceRidge
		
		// IceShots
		c.addDefault("Abilities.Water.IceShots.Enabled", true);
		c.addDefault("Abilities.Water.IceShots.Cooldown", 10000);
		c.addDefault("Abilities.Water.IceShots.Duration", 30000);
		c.addDefault("Abilities.Water.IceShots.SourceRange", 12);
		c.addDefault("Abilities.Water.IceShots.MaxIceShots", 4);
		c.addDefault("Abilities.Water.IceShots.Range", 20);
		c.addDefault("Abilities.Water.IceShots.DamagePerShot", 2);
		c.addDefault("Abilities.Water.IceShots.Speed", 2);
		c.addDefault("Abilities.Water.IceShots.HitRadius", 2);
		c.addDefault("Abilities.Water.IceShots.RingRadius", 2);
		
		// MistStepping
		c.addDefault("Abilities.Water.MistStepping.Enabled", true);
		c.addDefault("Abilities.Water.MistStepping.HorizontalPush", 0.3);
		c.addDefault("Abilities.Water.MistStepping.VerticalPush", 0.5);
		c.addDefault("Abilities.Water.MistStepping.MaxSteps", 10);
		c.addDefault("Abilities.Water.MistStepping.MaxDistanceFromGround", 20);
		c.addDefault("Abilities.Water.MistStepping.Cooldown", 8000);
		c.addDefault("Abilities.Water.MistStepping.Duration", 10000);
		
		// OctopusForm
		
		// PlantWhip
		c.addDefault("Abilities.Water.PlantWhip.Enabled", true);
		c.addDefault("Abilities.Water.PlantWhip.Damage", 3);
		c.addDefault("Abilities.Water.PlantWhip.Cooldown", 3000);
		c.addDefault("Abilities.Water.PlantWhip.Range", 20);
		c.addDefault("Abilities.Water.PlantWhip.SourceRange", 7);
		c.addDefault("Abilities.Water.PlantWhip.Duration", 4000);
		c.addDefault("Abilities.Water.PlantWhip.Speed", 1);
		c.addDefault("Abilities.Water.PlantWhip.HitRadius", 1.5);
		c.addDefault("Abilities.Water.PlantWhip.Knockback", 1);
		c.addDefault("Abilities.Water.PlantWhip.Knockup", 0.2);
		
		// RazorRings
		c.addDefault("Abilities.Water.RazorRings.Enabled", true);
		c.addDefault("Abilities.Water.RazorRings.Damage", 1);
		c.addDefault("Abilities.Water.RazorRings.Cooldown", 6000);
		c.addDefault("Abilities.Water.RazorRings.ShotCooldown", 100);
		c.addDefault("Abilities.Water.RazorRings.Range", 30);
		c.addDefault("Abilities.Water.RazorRings.SourceRange", 8);
		c.addDefault("Abilities.Water.RazorRings.Speed", 1.5);
		c.addDefault("Abilities.Water.RazorRings.Duration", 10000);
		c.addDefault("Abilities.Water.RazorRings.RingsCount", 6);
		c.addDefault("Abilities.Water.RazorRings.RadiusIncreaseRate", 0.35);
		
		// Transform
		c.addDefault("Abilities.Water.Transform.Enabled", true);
		
		// WaterCanvas
		c.addDefault("Abilities.Water.WaterCanvas.Enabled", true);
		c.addDefault("Abilities.Water.WaterCanvas.SourceRange", 80);
		
		// WaterPinwheel
		c.addDefault("Abilities.Water.WaterPinwheel.Enabled", true);
		c.addDefault("Abilities.Water.WaterPinwheel.Cooldown", 10000);
		c.addDefault("Abilities.Water.WaterPinwheel.Duration", 10000);
		c.addDefault("Abilities.Water.WaterPinwheel.SourceRange", 8);
		c.addDefault("Abilities.Water.WaterPinwheel.Range", 20);
		c.addDefault("Abilities.Water.WaterPinwheel.Damage", 2);
		c.addDefault("Abilities.Water.WaterPinwheel.DeflectDamage", 1);
		c.addDefault("Abilities.Water.WaterPinwheel.HitRadius", 2);
		c.addDefault("Abilities.Water.WaterPinwheel.RingRadius", 3);
		c.addDefault("Abilities.Water.WaterPinwheel.Speed", 1);
		c.addDefault("Abilities.Water.WaterPinwheel.Knockback", 2);
		
		// WaterRun
		c.addDefault("Abilities.Water.WaterRun.Enabled", true);
		c.addDefault("Abilities.Water.WaterRun.Speed", 1);
		c.addDefault("Abilities.Water.WaterRun.Cooldown", 6000);
		c.addDefault("Abilities.Water.WaterRun.Duration", 0);
		c.addDefault("Abilities.Water.WaterRun.DamageThreshold", 2);
		
		// WaterSlash
		c.addDefault("Abilities.Water.WaterSlash.Enabled", true);
		c.addDefault("Abilities.Water.WaterSlash.Damage", 3);
		c.addDefault("Abilities.Water.WaterSlash.Speed", 2);
		c.addDefault("Abilities.Water.WaterSlash.Cooldown", 5000);
		c.addDefault("Abilities.Water.WaterSlash.HitRadius", 1);
		c.addDefault("Abilities.Water.WaterSlash.SourceRange", 8);
		c.addDefault("Abilities.Water.WaterSlash.Range", 20);
		c.addDefault("Abilities.Water.WaterSlash.Duration", 30000);
		c.addDefault("Abilities.Water.WaterSlash.MaxAngle", 50);
		
		// WaterSphere
		c.addDefault("Abilities.Water.WaterSphere.Enabled", true);
		c.addDefault("Abilities.Water.WaterSphere.Radius", 2);
		c.addDefault("Abilities.Water.WaterSphere.Speed", 1);
		c.addDefault("Abilities.Water.WaterSphere.Range", 30);
		c.addDefault("Abilities.Water.WaterSphere.SourceRange", 8);
		c.addDefault("Abilities.Water.WaterSphere.Damage", 0);
		c.addDefault("Abilities.Water.WaterSphere.Cooldown", 20000);
		c.addDefault("Abilities.Water.WaterSphere.Duration", 60000);
		c.addDefault("Abilities.Water.WaterSphere.AllowIceSource", true);
		c.addDefault("Abilities.Water.WaterSphere.AllowPlantSource", false);
		c.addDefault("Abilities.Water.WaterSphere.AllowSnowSource", false);
		c.addDefault("Abilities.Water.WaterSphere.AllowBottleSource", false);
		
		// WaterSpoutRush
		c.addDefault("Abilities.Water.WaterSpoutRush.Enabled", true);
		c.addDefault("Abilities.Water.WaterSpoutRush.Cooldown", 30000);
		c.addDefault("Abilities.Water.WaterSpoutRush.Duration", 5000);
		
		// WaterVortex
		
		
		// EARTH
		
		// Crumble
		c.addDefault("Abilities.Earth.Crumble.Enabled", true);
		c.addDefault("Abilities.Earth.Crumble.Damage", 1);
		c.addDefault("Abilities.Earth.Crumble.HitRadius", 1);
		c.addDefault("Abilities.Earth.Crumble.Cooldown", 10000);
		c.addDefault("Abilities.Earth.Crumble.Range", 10);
		c.addDefault("Abilities.Earth.Crumble.DetectionRadius", 3.5);
		c.addDefault("Abilities.Earth.Crumble.MaxEarthBlasts", 3);
		c.addDefault("Abilities.Earth.Crumble.Shield.Enabled", true);
		c.addDefault("Abilities.Earth.Crumble.Shield.BlockRadius", 3);
		c.addDefault("Abilities.Earth.Crumble.Shield.Cooldown", 0);
		c.addDefault("Abilities.Earth.Crumble.Shield.Duration", 0);
		
		// DustDevil
		c.addDefault("Abilities.Earth.DustDevil.Enabled", true);
		c.addDefault("Abilities.Earth.DustDevil.Height", 9);
		c.addDefault("Abilities.Earth.DustDevil.Damage", 0.5);
		c.addDefault("Abilities.Earth.DustDevil.BlindnessTime", 1000);
		c.addDefault("Abilities.Earth.DustDevil.Cooldown", 0);
		c.addDefault("Abilities.Earth.DustDevil.Duration", 0);
		
		// DustDevilRush
		c.addDefault("Abilities.Earth.DustDevilRush.Enabled", true);
		c.addDefault("Abilities.Earth.DustDevilRush.Cooldown", 15000);
		c.addDefault("Abilities.Earth.DustDevilRush.Duration", 5000);
		
		// DustStepping
		c.addDefault("Abilities.Earth.DustStepping.Enabled", true);
		c.addDefault("Abilities.Earth.DustStepping.HorizontalPush", 1.5);
		c.addDefault("Abilities.Earth.DustStepping.VerticalPush", 1.2);
		c.addDefault("Abilities.Earth.DustStepping.MaxSteps", 10);
		c.addDefault("Abilities.Earth.DustStepping.MaxDistanceFromGround", 20);
		c.addDefault("Abilities.Earth.DustStepping.Cooldown", 8000);
		c.addDefault("Abilities.Earth.DustStepping.Duration", 10000);
		
		// EarthRidge
		c.addDefault("Abilities.Earth.EarthRidge.Enabled", true);
		c.addDefault("Abilities.Earth.EarthRidge.Range", 20);
		c.addDefault("Abilities.Earth.EarthRidge.Damage", 3);
		c.addDefault("Abilities.Earth.EarthRidge.Knockback", 1);
		c.addDefault("Abilities.Earth.EarthRidge.Knockup", 2);
		c.addDefault("Abilities.Earth.EarthRidge.MinHeight", 5);
		c.addDefault("Abilities.Earth.EarthRidge.MaxHeight", 7);
		c.addDefault("Abilities.Earth.EarthRidge.HitRadius", 1.5);
		c.addDefault("Abilities.Earth.EarthRidge.SourceRange", 7);
		c.addDefault("Abilities.Earth.EarthRidge.Cooldown", 8000);
		c.addDefault("Abilities.Earth.EarthRidge.Duration", 5000);
		
		// EarthShift
		c.addDefault("Abilities.Earth.EarthShift.Enabled", true);
		c.addDefault("Abilities.Earth.EarthShift.Range", 15);
		c.addDefault("Abilities.Earth.EarthShift.Speed", 1);
		c.addDefault("Abilities.Earth.EarthShift.Cooldown", 5000);
		
		// EarthTent
		c.addDefault("Abilities.Earth.EarthTent.Enabled", true);
		c.addDefault("Abilities.Earth.EarthTent.Length", 5);
		c.addDefault("Abilities.Earth.EarthTent.Width", 8);
		c.addDefault("Abilities.Earth.EarthTent.Height", 5);
		c.addDefault("Abilities.Earth.EarthTent.RevertTime", 300000);
		c.addDefault("Abilities.Earth.EarthTent.Cooldown", 15000);
		
		// GlassShards
		c.addDefault("Abilities.Earth.GlassShards.Enabled", true);
		c.addDefault("Abilities.Earth.GlassShards.Damage", 2);
		c.addDefault("Abilities.Earth.GlassShards.MaxShards", 5);
		c.addDefault("Abilities.Earth.GlassShards.Range", 20);
		c.addDefault("Abilities.Earth.GlassShards.SourceRange", 6);
		c.addDefault("Abilities.Earth.GlassShards.BleedDamage", 1);
		c.addDefault("Abilities.Earth.GlassShards.BleedTime", 4000);
		c.addDefault("Abilities.Earth.GlassShards.BleedInterval", 1500);
		c.addDefault("Abilities.Earth.GlassShards.Speed", 1);
		c.addDefault("Abilities.Earth.GlassShards.Cooldown", 10000);
		c.addDefault("Abilities.Earth.GlassShards.Duration", 0);
		c.addDefault("Abilities.Earth.GlassShards.GlassCrackRadius", 2);
		
		// LavaWalk
		c.addDefault("Abilities.Earth.LavaWalk.Enabled", true);
		c.addDefault("Abilities.Earth.LavaWalk.Radius", 3);
		c.addDefault("Abilities.Earth.LavaWalk.CanBendTempLava", true);
		c.addDefault("Abilities.Earth.LavaWalk.Range", 10);
		
		// MetalCables
		
		// RaiseEarth
		c.addDefault("Abilities.Earth.RaiseEarth.Enabled", true);
		c.addDefault("Abilities.Earth.RaiseEarth.Cooldown", 3000);
		c.addDefault("Abilities.Earth.RaiseEarth.SourceRange", 6);
		c.addDefault("Abilities.Earth.RaiseEarth.Height", 6);
		c.addDefault("Abilities.Earth.RaiseEarth.WallWidth", 6);
		c.addDefault("Abilities.Earth.RaiseEarth.CollisionRadius", 2);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Enabled", true);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Knockback", 1);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Damage", 3);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.HitRadius", 1.5);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Speed", 1);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Range", 20);
		c.addDefault("Abilities.Earth.RaiseEarth.Throw.Cooldown", 4000);
		
		// Sandstorm
		
		
		// FIRE
		
		// Electrify
		
		// Evaporate
		c.addDefault("Abilities.Fire.Evaporate.Enabled", true);
		c.addDefault("Abilities.Fire.Evaporate.Cooldown", 10000);
		c.addDefault("Abilities.Fire.Evaporate.InitialShieldRadius", 3);
		c.addDefault("Abilities.Fire.Evaporate.RadiusIncreaseRate", 0.1);
		c.addDefault("Abilities.Fire.Evaporate.HitRadius", 1);
		c.addDefault("Abilities.Fire.Evaporate.CollisionRadius", 4);
		c.addDefault("Abilities.Fire.Evaporate.ParticleAmount", 2);
		c.addDefault("Abilities.Fire.Evaporate.ParticleSpread", 0.2);
		c.addDefault("Abilities.Fire.Evaporate.Duration", 2000);
		c.addDefault("Abilities.Fire.Evaporate.Speed", 0.2);
		
		// FireAugmentation
		c.addDefault("Abilities.Fire.FireAugmentation.Enabled", true);
		c.addDefault("Abilities.Fire.FireAugmentation.Cooldown", 6000);
		c.addDefault("Abilities.Fire.FireAugmentation.Duration", 10000);
		c.addDefault("Abilities.Fire.FireAugmentation.Range", 20);
		c.addDefault("Abilities.Fire.FireAugmentation.Speed", 0.6);
		c.addDefault("Abilities.Fire.FireAugmentation.Damage", 0);
		c.addDefault("Abilities.Fire.FireAugmentation.SourceRange", 5);
		c.addDefault("Abilities.Fire.FireAugmentation.AllowSlotChange", false);
		
		// FireBlade
		c.addDefault("Abilities.Fire.FireBlade.Enabled", true);
		c.addDefault("Abilities.Fire.FireBlade.Damage", 3);
		c.addDefault("Abilities.Fire.FireBlade.Speed", 1.5);
		c.addDefault("Abilities.Fire.FireBlade.Range", 25);
		c.addDefault("Abilities.Fire.FireBlade.HitRadius", 1);
		c.addDefault("Abilities.Fire.FireBlade.Cooldown", 5000);
		c.addDefault("Abilities.Fire.FireBlade.MaxAngle", 20);
		
		// FireDaggers
		c.addDefault("Abilities.Fire.FireDaggers.Enabled", true);
		c.addDefault("Abilities.Fire.FireDaggers.Damage", 2);
		c.addDefault("Abilities.Fire.FireDaggers.Cooldown", 15000);
		c.addDefault("Abilities.Fire.FireDaggers.UsageCooldown", 500);
		c.addDefault("Abilities.Fire.FireDaggers.Duration", 0);
		c.addDefault("Abilities.Fire.FireDaggers.BlockDuration", 2000);
		c.addDefault("Abilities.Fire.FireDaggers.HitRadius", 1);
		c.addDefault("Abilities.Fire.FireDaggers.Range", 5);
		c.addDefault("Abilities.Fire.FireDaggers.Speed", 2);
		c.addDefault("Abilities.Fire.FireDaggers.MaxThrows", 6);
		
		// FireJet
		c.addDefault("Abilities.Fire.FireJet.Enabled", true);
		c.addDefault("Abilities.Fire.FireJet.Cooldown", 10000);
		c.addDefault("Abilities.Fire.FireJet.Duration", 15000);
		c.addDefault("Abilities.Fire.FireJet.ParticleAmount", 1);
		c.addDefault("Abilities.Fire.FireJet.ParticleLength", 2);
		c.addDefault("Abilities.Fire.FireJet.ParticleSpread", 0.1);
		c.addDefault("Abilities.Fire.FireJet.OnSlotModifier", 1.25);
		c.addDefault("Abilities.Fire.FireJet.DamageThreshold", 4);
		c.addDefault("Abilities.Fire.FireJet.Propel.Speed", 1);
		c.addDefault("Abilities.Fire.FireJet.Propel.Duration", 5000);
		c.addDefault("Abilities.Fire.FireJet.Ski.Enabled", true);
		c.addDefault("Abilities.Fire.FireJet.Ski.Speed", 1);
		c.addDefault("Abilities.Fire.FireJet.Ski.TurningSpeed", 0.05);
		c.addDefault("Abilities.Fire.FireJet.Ski.Duration", 5000);
		c.addDefault("Abilities.Fire.FireJet.Hover.Enabled", true);
		c.addDefault("Abilities.Fire.FireJet.Hover.Speed", 0.1);
		c.addDefault("Abilities.Fire.FireJet.Hover.Duration", 5000);
		c.addDefault("Abilities.Fire.FireJet.Hover.Recovery.Enabled", true);
		c.addDefault("Abilities.Fire.FireJet.Hover.Recovery.Duration", 3000);
		c.addDefault("Abilities.Fire.FireJet.Hover.Recovery.Cooldown", 5000);
		c.addDefault("Abilities.Fire.FireJet.Hover.Drift.Enabled", true);
		c.addDefault("Abilities.Fire.FireJet.Hover.Drift.Speed", 0.4);
		
		// FireStreams
		c.addDefault("Abilities.Fire.FireStreams.Enabled", true);
		c.addDefault("Abilities.Fire.FireStreams.Damage", 4);
		c.addDefault("Abilities.Fire.FireStreams.Cooldown", 10000);
		c.addDefault("Abilities.Fire.FireStreams.Range", 40);
		c.addDefault("Abilities.Fire.FireStreams.HitRadius", 1);
		c.addDefault("Abilities.Fire.FireStreams.Speed", 2);
		c.addDefault("Abilities.Fire.FireStreams.ExplosionRadius", 2);
		
		// FireWhips
		
		// JetBlast
		c.addDefault("Abilities.Fire.JetBlast.Enabled", true);
		c.addDefault("Abilities.Fire.JetBlast.SpeedModifier", 1.4);
		c.addDefault("Abilities.Fire.JetBlast.Cooldown", 8000);
		c.addDefault("Abilities.Fire.JetBlast.Duration", 5000);
		
		// JetBlaze
		c.addDefault("Abilities.Fire.JetBlaze.Enabled", true);
		c.addDefault("Abilities.Fire.JetBlaze.SpeedModifier", 1.1);
		c.addDefault("Abilities.Fire.JetBlaze.FireTicks", 40);
		c.addDefault("Abilities.Fire.JetBlaze.Damage", 2);
		c.addDefault("Abilities.Fire.JetBlaze.Cooldown", 8000);
		c.addDefault("Abilities.Fire.JetBlaze.Duration", 5000);
		c.addDefault("Abilities.Fire.JetBlaze.ParticleHitRadius", 0.5);
		c.addDefault("Abilities.Fire.JetBlaze.ParticleAmount", 25);
		c.addDefault("Abilities.Fire.JetBlaze.ParticleSpread", 1.25);
		
		// JetStepping
		c.addDefault("Abilities.Fire.JetStepping.Enabled", true);
		c.addDefault("Abilities.Fire.JetStepping.HorizontalPush", 0.3);
		c.addDefault("Abilities.Fire.JetStepping.VerticalPush", 0.5);
		c.addDefault("Abilities.Fire.JetStepping.MaxSteps", 10);
		c.addDefault("Abilities.Fire.JetStepping.Cooldown", 8000);
		c.addDefault("Abilities.Fire.JetStepping.Duration", 0);
		
		
		// CHI
		
		// Duck
		c.addDefault("Abilities.Chi.Duck.Enabled", true);
		c.addDefault("Abilities.Chi.Duck.Cooldown", 5000);
		c.addDefault("Abilities.Chi.Duck.Duration", 3000);
		
		// Parry
		ArrayList<String> nonParryables = new ArrayList<>();
		nonParryables.add(EntityType.CREEPER.toString());
		nonParryables.add(EntityType.ELDER_GUARDIAN.toString());
		nonParryables.add(EntityType.ENDER_DRAGON.toString());
		nonParryables.add(EntityType.GUARDIAN.toString());
		nonParryables.add(EntityType.HOGLIN.toString());
		nonParryables.add(EntityType.IRON_GOLEM.toString());
		nonParryables.add(EntityType.PHANTOM.toString());
		nonParryables.add(EntityType.POLAR_BEAR.toString());
		nonParryables.add(EntityType.RAVAGER.toString());
		nonParryables.add(EntityType.SILVERFISH.toString());
		nonParryables.add(EntityType.WITHER.toString());
		nonParryables.add(EntityType.WOLF.toString());
		
		c.addDefault("Abilities.Chi.Parry.Enabled", true);
		c.addDefault("Abilities.Chi.Parry.Cooldown", 5000);
		c.addDefault("Abilities.Chi.Parry.Duration", 1000);
		c.addDefault("Abilities.Chi.Parry.MaxDistance", 5);
		c.addDefault("Abilities.Chi.Parry.NonParryableMobs", nonParryables);
		
		// MULTI-ELEMENTAL
		c.addDefault("Abilities.Multi-Elemental.Dodge.Enabled", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Cooldown", 5000);
		c.addDefault("Abilities.Multi-Elemental.Dodge.HorizontalModifier", 0.6);
		c.addDefault("Abilities.Multi-Elemental.Dodge.VerticalModifier", 0.4);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Chi", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Fire", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Air", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Water", false);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Earth", false);
		
		c.options().copyDefaults(true);
		Azutoru.az.saveConfig();
	}

}
