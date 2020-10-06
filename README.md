# Azutoru
## About Azutoru
Azutoru is a new Spigot plugin that adds onto [ProjectKorra](https://github.com/ProjectKorra/ProjectKorra), a bending plugin. Its purpose is to add interesting, unique, highly functional, and useful abilities and concepts that run smoothly. Made with competitive and casual benders alike in mind, the plugin prioritizes adding combos that use popular abilities for their activation sequences so that older benders won't have to adapt too much to be able to use new abilities.

## Development
Currently, the project is still in its early-to-mid stages of development. Each element has been given considerable attention, some more than others. My priority right now is to add more abilities for the elements I have not given as much attention as others. After that, I will attempt to rework some of the ProjectKorra Core abilities that I feel need reworking. The final stage of development will be adding depth to the subelements, such as plantbending, metalbending, etc. These subelements have mostly been neglected by the core plugin, and most of the abilities that have been created by addon developers have lacked depth. My goal with the subelements is to add a hefty and unique moveset and style for each subelement to closely match what we've seen in the shows and to expand on the canon. Feel free to keep track of my progress with my Project [here](https://github.com/Aztlon/Azutoru/projects/1).

## Bug testing
While the plugin is still in development, it will be available for bug testing on my server [Indigon](https://discord.gg/VYE5WWr), which you can access with the IP play.indigonmc.com with Minecraft 1.16.x. After I feel it is ready, I will release it publicly as I once promised. Until then, feel free to watch this repository as closely as you'd like, as I will try to update it frequently. Please do not try to compile the source code yourself or ask for the jar file. It's not ready for other servers to use.

## Contributing
I do accept contributions of any sort. If you have an idea for an ability, feel free to either make an issue for it or simply DM me on Discord. My username is Aztl#0001. If you'd like to contribute to the code, feel free to fork the repository and make a pull request.

## New features
* Configurable anti-suffocation
  * This feature is used to allow server owners to disable suffocation on certain blocks: bending TempBlocks, all earth blocks, all ice blocks, or all blocks.
* New subelement: Glassbending
  * Glassbending is a subelement of Earthbending, as demonstrated in the Kyoshi novels. Glassbending isn't one of the subelements I'm going to give a unique moveset, just because of the rarity of glass, but it will have a few interesting abilities.
## Abilities
Note: These are all the abilities I plan to have in the plugin. Some are finished, and some have not even been started.
### Air
* CloudSurf
  * Similar to Vahagn's Nimbus, CloudSurf lets airbenders levitate on a small cloud for a short period of time. Click moves can be used while on the cloud.
#### Combos
* AirCocoon
  * Demonstrated by Tenzin in his fight with the Red Lotus, AirCocoon lets airbenders wrap themselves in a cocoon of air that blocks most attacks and deflects projectiles.
  * Uses AirShield
* AirCushion
  * Also demonstrated by Tenzin in his fight with Hiroshi Sato's mechatanks, AirCushion allows airbenders to place a cushion of air on the ground for a period of time. Anyone or anything that falls in the cushion won't take fall damage.
  * Uses AirShield
* AirSpoutRush
  * Allows airbenders to accelerate their AirSpout for a period of time
  * Uses AirSpout and AirBurst
* AirVortex
  * Demonstrated by Tenzin against Equalists on the City Hall roof, AirVortex allows airbenders to spin entities around them while on AirSpout
* AirWake
  * Similar to Numin's SummonSelf, AirWake sends out an aerokinetic duplicate of the player that has high concussive force and can do damage.
  * Uses AirShield and AirBurst
### Chi
#### Passives
While these moves can be bound, they are meant to be used on any slot, so there is no point in binding them.
* Dodge
  * Dodge is actually useable by airbenders and firebenders as well (although this is configurable), but this allows chiblockers to quickly dodge an attack while they're close to the ground
* Duck
  * Allows the chiblocker to duck (crouch) for a short period of time to block incoming attacks
* Parry
  * Another defensive chi move, Parry lets chiblockers who are sneaking to block attacks that originate close to them (up to 5 blocks away). This allows for some close-ranged defensive capabilities
### Earth
#### Combos
* EarthShift
  * Allows an earthbender to displace a target on the ground, disorienting them
#### Multiabilities
* EarthLevitation
  * Still in its conceptual phase, EarthLevitation will be an earth utility multiability that lets earthbenders place earth blocks in various shapes and sizes that have permanent effects on the world. Right now, earthbenders are limited to raising temporary walls, but what if they could raise all kinds of shapes from the earth?
#### Glass
* GlassShards
  * Allows glassbenders to use a glass block to circle tiny shards of glass around them and even throw them towards an enemy. Right-clicking with it also shatters glass blocks and any glass connected to it.
#### Lava
* LavaWalk
  * A lavabending passive that will eventually be toggleable. It cools lava into stone wherever the lavabender walks, allowing safe transportation of the Nether and any other large lava body.
#### Metal
* MetalCables
  * This reworked MetalCables will have mobility, utility, and combat in mind. It's going to be setup to maximize the speed at which players can use the different subabilities. There are a few things I have to do before making this, but it will be a priority quite soon.
#### Sand
* DustDevil
  * This is a rework of the old SandSpout move. DustDevil works on any block designated a "dust block," which includes not only sand but any block that has sediment, such as dirt and grass blocks. The list of dust blocks is configurable.
* DustDevilRush
  * Just like AirSpout and WaterSpout, this new spout gets its own "rush" combo, allowing people to increase the spout speed temporarily.
* DustStepping
  * Demonstrated by Lek in the Kyoshi novels, DustStepping (combo) will allow earthbenders to climb stairs of dust
* Sandstorm
  * Similar to Hiro3's Mist or Vahagn's DustCloud, Sandstorm (combo) will allow sandbenders to become invisible. There will be more functions later on.
### Fire
* FireDaggers
  * Demonstrated by Zuko and Azula, this ability gives fire more short-ranged capabilities. The daggers can be thrown a short distance or be used to block incoming attacks. More functionality to come.
* FireWhips
  * Not yet started
* Meteor
  * This might be converted into an icebending ability, but the concept behind Meteor is based on the meteor hammer weapon in Chinese martial arts.
#### Combos
* FireAugmentation
  * Demonstrated by Zuko during his fight with Aang on Ember Island, this combo allows a firebender to create a long snake-like circle of fire that places fire wherever it goes.
* FireBlade
  * An offensive combo that works like AirSweep, allowing the user to draw an arc in the air that advances towards the direction they're looking.
* FireBreath
  * I will eventually make my own FireBreath with completely configurable colors and high functionality
* FireStreams
  * Demonstrated by Zuko during his fight with Aang in the Crystal Catacombs and similar to Vahagn's FireStream, this combo gives a firebender long-ranged damage capabilities.
#### Blue Fire
* Evaporate
  * The very first blue fire ability, Evaporate (combo), was demonstrated by Azula against Katara in the Crystal Catacombs. It is a defensive fire ability that evaporates all incoming water attacks and can even block some other abilities.
#### Lightning
* ElectricField
  * This ability will be a Lightning AoE move that creates a small storm that summons lightning bolts from the sky.
* Electrify
  * Similar to JedCore's Discharge, Electrify will be a short-ranged left-click Lightning ability whose main purpose will be to charge water and metallic blocks with electricity. More functionality to come.
### Water
* WaterCanvas
  * This ability has yet to be made, but it's going to be used for several purposes. It will pull water from a nearby source (auto-source) and use that water to cushion a waterbender's fall. It can also be used for riding for a short period of time, such as what Unalaq demonstrated near the spirit portal. It will have offensive capabilities, such as what Katara used against Combustion Man at the Western Air Temple. It can also be repurposed for other water abilities.
#### Combos
* RazorRings
  * Made in collaboration with Hiro3 and demonstrated by Katara in her fight with the "Swamp Monster," RazorRings creates sharp rings of water that can damage entities. Eventually, it will cut through leaves and other blocks.
* WaterPinwheel
  * Demonstrated by Hama in her fight with Katara, WaterPinwheel will be able to block Torrents and some other water abilities. More functionality to come.
* WaterRun
  * Demonstrated by Eska and Desna in their fight with Korra, WaterRun allows waterbenders to run across a water surface and use other abilities while doing it
* WaterSlash
  * The water version of FireBlade, WaterSlash lets waterbenders draw an arc in the air that advances its location to where they're looking, dealing damage. It will eventually cut through some objects.
* WaterSphere
  * Similar to Sorin's GeyserRush and JedCore's WaterFlow, WaterSphere lets waterbenders encase entities in a ball of water that can be sent forward, retracted, and stopped completely. Soon I will add a riding function.
* WaterSpoutRush
  * The same concept as AirSpoutRush and DustDevilRush - allows a waterbender to accelerate their WaterSpout for a brief period of time
* WaterVortex
  * Demonstrated by Korra in her fight with Eska and Desna, WaterVortex will work a lot like TorrentWave on a WaterSpout, with their spout expanding horizontally to push entities away and possibly blind
#### Multiabilities
* Hexapod
  * The first multicombo in ProjectKorra - a multiability activated by a combo
  * 6-armed WaterArms multiability, demonstrated by Ming Hua, with the subabilities inspired by things insects can do in our world:
  * Whip: rope-like tentacle that does knockback and a little bit of damage
  * Slash: uses WaterSlash combo
  * Grapple: similar to WaterArms grapple
  * Cling: cling to a surface
  * Grab: grab multiple entities, carry them with you, or throw them
  * Shell: icebending defensive move, uses arms to create an ice wall or wraps arms to make an ice shell
  * Catapult: propel yourself high into the air with your arms
  * Wings: ...
  * Transform: switch to another ability
* OctopusForm
  * I'm going to rework OctopusForm into a multiability. More info to come.
* Transform
  * A WIP concept, Transform will allow waterbenders to transition between one multiability to another with fluidity
#### Blood
* BloodStrangle
  * Demonstrated by Yakone and Tarrlok, BloodStrangle allows bloodbenders to grab multiple entities and slowly strangle them, similar to Suffocate
#### Healing
* Spiritbending
  * Demonstrated by Unalaq and Korra, healers will be able to "pacify" mobs with spiritbending, effectively just removing them from the world. If used on a human or passive mob, it will attempt to destroy their soul, slowly killing them.
#### Ice
* IceRidge
  * This ability will create a wall of ice that extend in a certain controllable direction like JedCore's EarthLine
* IceShots
  * Demonstrated by Kya during her fight with Zaheer, this combo creates a ring of water around the user and shoots ice shards from the ring
#### Plant
* PlantWhip
  * Summon a whip of plants that does damage
