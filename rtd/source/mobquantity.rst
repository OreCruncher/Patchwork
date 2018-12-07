Adjust Mob Spawn Quantity
-------------------------
..	versionadded:: 0.2.3.0

The mob spawn cap for natural spawns can be adjusted by enabling the Change Mob Quantities feature.
This feature is off by default so you would have to specifically enable it.

During the normal world tick Minecraft will spawn mobs based on their class and the class mob cap.
The Minecraft defaults are:

..	list-table::
	:widths: 30 30 30
   	:header-rows: 1
   	
   	*	- Class
   		- Mob Cap
   		- Example
   		
   	*	- MONSTER
   		- 70
   		- Skeletons and Zombies
   	*	- CREATURE
   		- 10
   		- Cows, pigs, and sheep
   	*	- AMBIENT
   		- 15
   		- Bats
   	*	- WATER_CREATURE
   		- 5
   		- Derpy Squids
   		
Minecraft will cap the number of mobs of a given type within all currently loaded chunks.  Note
that this only applies to natural spawning.  Mob spawns from spawners and other modded sources can
exceed this cap.  However, once spawned they will affect the natural mob spawn process.  (This is
why you see less mobs in the world on servers where players have mob spawn systems producing large
quantities of mobs.  Not that I am salty or anything...)

Keep in mind that Minecraft has worldgen related mob spawn.  This is entirely separate from natural
mob spawn.  Worldgen spawns occur when a chunk is initially created.  This is how you get all the
cows and pigs populated in new chunks.  As you can see by the CREATURE cap in the table above (10)
natural spawn for animals is limited to 10 mobs in *all currently loaded chunks*.  This is why you
rarely see animals spawn on servers.  It is more than likely a player has an animal pen with at
least 10 mobs, and once that chunk is loaded no more natural animal spawn will occur.  You will
have to find newly generated chunks.

OK - so why use this feature?

- You can increase the number of hostile mob spawns by increasing the MONSTER cap.  Quantity has a quality all it's own.
- You can decrease the number of Bats and Squids.  If you want to reduce the quantity of these mobs and not eliminate them entirely tweak the values down.

..	note::

	Mods can do their own mob spawning and not adhere to what Minecraft rules are.  This feature
	cannot do anything about that.  All this feature does is modify the constant values that
	Minecraft uses.

Configuration
^^^^^^^^^^^^^

- **features.modifyMobQuantity** Enable/disable modification of mob spawn caps.
- **mobquantity.ambient** Change the AMBIENT mob quantity cap.
- **mobquantity.creature** Change the CREATURE mob quantity cap.
- **mobquantity.monster** Change the MONSTER mob quantity cap.
- **mobquantity.water** Change the WATER mob quantity cap.
