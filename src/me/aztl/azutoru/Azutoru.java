package me.aztl.azutoru;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.ElementType;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.firebending.FireBlast;
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

import me.aztl.azutoru.ability.air.combo.AirCocoon;
import me.aztl.azutoru.ability.fire.FireDaggers;
import me.aztl.azutoru.ability.fire.bluefire.combo.Evaporate;
import me.aztl.azutoru.ability.fire.combo.FireStreams;
import me.aztl.azutoru.ability.water.combo.WaterPinwheel;

public class Azutoru extends JavaPlugin {
	
	public static Azutoru az;
	private Config config;
	private AzutoruMethods methods;
	private Element glassElement;
	
	@Override
	public void onEnable() {
		az = this;
		
		config = new Config(new File("azutoru.yml"));
		setupConfig();
		
		CoreAbility.registerPluginAbilities(this, "me.aztl.azutoru.ability");
		
		setupCollisions();
		
		getServer().getPluginManager().registerEvents(new AzutoruListener(this), this);
		
		methods = new AzutoruMethods(this);
		
		glassElement = new SubElement("Glass", Element.EARTH, ElementType.BENDING, this) {
			@Override
			public ChatColor getColor() {
				return Element.EARTH.getSubColor();
			}
		};
	}
	
	@Override
	public void onDisable() {}
	
	public AzutoruMethods getMethods() {
		return methods;
	}
	
	public String dev() {
		return "Aztl";
	}
	
	public String prefix() {
		return ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "Azutoru" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY;
	}
	
	public String version() {
		return prefix() + " v. 1.0.0";
	}
	
	public Element getGlassElement() {
		return glassElement;
	}
	
	@Override
	public FileConfiguration getConfig() {
		return config.get();
	}
	
	public Config config() {
		return config;
	}
	
	private void setupCollisions() {
		if (CoreAbility.getAbility(AirCocoon.class) != null) {
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(AirSwipe.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(FireBlast.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(EarthBlast.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(WaterManipulation.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(Lightning.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility("Combustion"), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(Torrent.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(EarthSmash.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(AirBurst.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(AirCocoon.class), CoreAbility.getAbility(FireBurst.class), true, false));
		}
			
		if (CoreAbility.getAbility(WaterPinwheel.class) != null) {
			ProjectKorra.getCollisionInitializer().addLargeAbility(CoreAbility.getAbility(WaterPinwheel.class));
			ProjectKorra.getCollisionInitializer().addRemoveSpoutAbility(CoreAbility.getAbility(WaterPinwheel.class));
		}
			
		if (CoreAbility.getAbility(FireDaggers.class) != null) {
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(AirSwipe.class), true, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(FireBlast.class), true, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(EarthBlast.class), true, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(WaterManipulation.class), true, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(Lightning.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility("Combustion"), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(Torrent.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(EarthSmash.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(AirBurst.class), true, false));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(FireDaggers.class), CoreAbility.getAbility(FireBurst.class), true, false));
		}
		
		if (CoreAbility.getAbility(FireStreams.class) != null) {
			ProjectKorra.getCollisionInitializer().addLargeAbility(CoreAbility.getAbility(FireStreams.class));
			ProjectKorra.getCollisionInitializer().addRemoveSpoutAbility(CoreAbility.getAbility(FireStreams.class));
			ProjectKorra.getCollisionInitializer().addComboAbility(CoreAbility.getAbility(FireStreams.class));
		}
		
		if (CoreAbility.getAbility(Evaporate.class) != null) {
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(Torrent.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(WaterManipulation.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(SurgeWave.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(SurgeWall.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(TorrentWave.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(WaterSpoutWave.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(IceSpikeBlast.class), false, true));
			ProjectKorra.getCollisionManager().addCollision(new Collision(CoreAbility.getAbility(Evaporate.class), CoreAbility.getAbility(IceWave.class), false, true));
			ProjectKorra.getCollisionInitializer().addLargeAbility(CoreAbility.getAbility(Evaporate.class));
			ProjectKorra.getCollisionInitializer().addRemoveSpoutAbility(CoreAbility.getAbility(Evaporate.class));
			ProjectKorra.getCollisionInitializer().addComboAbility(CoreAbility.getAbility(Evaporate.class));
		}
		
	}
	
	private void setupConfig() {
		FileConfiguration c = config.get();
		
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
		c.addDefault("Abilities.Air.CloudSurf.BreakDamage", 2);
		
		
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
		
		// Marionettes
		
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
		c.addDefault("Abilities.Water.WaterRun.BreakDamage", 2);
		
		// WaterSlash
		c.addDefault("Abilities.Water.WaterSlash.Enabled", true);
		c.addDefault("Abilities.Water.WaterSlash.Damage", 3);
		c.addDefault("Abilities.Water.WaterSlash.Speed", 2);
		c.addDefault("Abilities.Water.WaterSlash.Cooldown", 5000);
		c.addDefault("Abilities.Water.WaterSlash.HitRadius", 1);
		c.addDefault("Abilities.Water.WaterSlash.SourceRange", 8);
		c.addDefault("Abilities.Water.WaterSlash.Range", 20);
		c.addDefault("Abilities.Water.WaterSlash.Duration", 30000);
		
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
		
		// EarthLevitation
		
		// EarthShift
		c.addDefault("Abilities.Earth.EarthShift.Enabled", true);
		c.addDefault("Abilities.Earth.EarthShift.Range", 15);
		c.addDefault("Abilities.Earth.EarthShift.Speed", 1);
		c.addDefault("Abilities.Earth.EarthShift.Cooldown", 5000);
		
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
		c.addDefault("Abilities.Earth.LavaWalk.RevertTime", 5000);
		c.addDefault("Abilities.Earth.LavaWalk.CanBendTempLava", true);
		
		// MetalCables
		
		// Sandstorm
		
		// TectonicRift
		
		
		// FIRE
		
		// ElectricField
		
		// Electrify
		
		// Evaporate
		c.addDefault("Abilities.Fire.Evaporate.Enabled", true);
		c.addDefault("Abilities.Fire.Evaporate.Cooldown", 10000);
		c.addDefault("Abilities.Fire.Evaporate.InitialShieldRadius", 3);
		c.addDefault("Abilities.Fire.Evaporate.RadiusIncreaseRate", 0.1);
		c.addDefault("Abilities.Fire.Evaporate.HitRadius", 1);
		c.addDefault("Abilities.Fire.Evaporate.CollisionRadius", 2);
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
		
		// FireBreath
		
		// FireDaggers
		c.addDefault("Abilities.Fire.FireDaggers.Enabled", true);
		c.addDefault("Abilities.Fire.FireDaggers.Damage", 2);
		c.addDefault("Abilities.Fire.FireDaggers.Cooldown", 15000);
		c.addDefault("Abilities.Fire.FireDaggers.UsageCooldown", 500);
		c.addDefault("Abilities.Fire.FireDaggers.Duration", 0);
		c.addDefault("Abilities.Fire.FireDaggers.BlockDuration", 2000);
		c.addDefault("Abilities.Fire.FireDaggers.HitRadius", 0.5);
		c.addDefault("Abilities.Fire.FireDaggers.Range", 5);
		c.addDefault("Abilities.Fire.FireDaggers.ThrowSpeed", 2);
		c.addDefault("Abilities.Fire.FireDaggers.MaxThrows", 6);
		
		// FireStreams
		c.addDefault("Abilities.Fire.FireStreams.Enabled", true);
		c.addDefault("Abilities.Fire.FireStreams.Damage", 4);
		c.addDefault("Abilities.Fire.FireStreams.Cooldown", 10000);
		c.addDefault("Abilities.Fire.FireStreams.Range", 40);
		c.addDefault("Abilities.Fire.FireStreams.HitRadius", 1);
		c.addDefault("Abilities.Fire.FireStreams.Speed", 2);
		c.addDefault("Abilities.Fire.FireStreams.ExplosionRadius", 2);
		
		// FireWhips
		
		// Meteor
		
		
		// CHI
		
		// Duck
		c.addDefault("Abilities.Chi.Duck.Enabled", true);
		c.addDefault("Abilities.Chi.Duck.Cooldown", 5000);
		c.addDefault("Abilities.Chi.Duck.Duration", 3000);
		
		// Parry
		c.addDefault("Abilities.Chi.Parry.Enabled", true);
		c.addDefault("Abilities.Chi.Parry.Cooldown", 5000);
		c.addDefault("Abilities.Chi.Parry.Duration", 1000);
		c.addDefault("Abilities.Chi.Parry.MaxDistance", 5);
		
		// MULTI-ELEMENTAL
		c.addDefault("Abilities.Multi-Elemental.Dodge.Enabled", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Cooldown", 5000);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Chi", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Fire", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Air", true);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Water", false);
		c.addDefault("Abilities.Multi-Elemental.Dodge.Earth", false);
		
		config.save();
	}

}
