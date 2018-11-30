Ring of Flight
--------------
.. image:: images/ringofflight.png
   :align: center

The Ring of Flight gives the player creative flight capability when equipped in a Baubles ring
slot.  There are 5 types of rings:

- Flight Core.  Does not give flight ability itself but is used to create the other rings.  There is no crafting recipe for the core - it has to be discovered in a village blacksmith or dungeon chest.
- Avian Leg Ring Band.  Basic flight ring crafted with a Flight Core and Feather Wings.  Has the least durability of the rings.
- Ring of Methodical Flight.  This ring has much more durability than the Avian Leg Ring, but is much slower as well.  It is crafted using the Flight Core and Obidian Wings.
- Stout Ring of Flight.  This ring has a little less durability than Methodical Ring, but the flight speed is the same as the Avian Leg Ring.  It is crafted using the Flight Core and Sturdy Wings.
- Ring of the Zephyr.  This ring has less durability than the Methodical Ring, but faster flight speed than the other rings.  It is crafted using the Flight Core and Speedy Wings.

The Feather and Obsidian Wings can be crafted by the player.  The Stout and Speedy Wings cannot be
crafted, and have to be found in village blacksmith and dungeon chests.

Each of the rings has a durability that will be consumed when the player flies.  When the
durability reaches 0, the player will drop to the ground and will take damage if the fall distance
is high enough.

..	versionadded:: 0.2.2.0

To refuel a Ring of Flight combine the item with Essence of Flight and a Set of Tools in a crafting
grid.  Each Essence of Flight will repair up to 5000 points of flight time.  If a stack or more of
Essence of FLight is provided the crafting operation will deduct an amount needed to fully charge
the item.  The Set of Tools will be damaged based on the amount refueled.

..	list-table::
	:widths: 20 20 40
	:header-rows: 1
	
	*	- Ring
		- Flight Duration
		- Comment
		
	*	- Avian Leg Ring Band
		- 15 - 16 minutes
		- Feather Wing
	*	- Ring of Methodical Flight
		- 62 - 63 minutes
		- Obsidian Wing
	*	- Stout Ring of Flight
		- 46 - 47 minutes
		- Sturdy Wing; must be found
	*	- Ring of the Zephyr
		- 31 - 32 minutes
		- Speedy Wing; must be found

Configuration
^^^^^^^^^^^^^

- **items.enableRingOfFlight** Enables/disables loot table inclusion and recipes for the ring.
- **features.renderWings** Enables/disables rendering of wings on player's back.
- **ringofflight.refuelAmount** Amount per Fire Charge repaired

