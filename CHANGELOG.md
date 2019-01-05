### Patchwork-1.12.2-0.2.3.3
**Requirements**
* Forge 2779+
* OreLib-1.12.2-3.5.2.0+

**Fixes**
* Do not capture owned animals if not owned by the capturing player.
* Fixed Shop Shelf sync between server and client.

### Patchwork-1.12.2-0.2.3.2
**Fixes**
* Client crash rendering 3d furnace after player death nearby.

### Patchwork-1.12.2-0.2.3.1
**What's New**
* Config options to add additional biomes where villages can spawn.
* Config options to black/white list blocks used for skinning the Shop Shelf.

**Fixes**
* IStateMapper crash on dedicated server.

**Changes**
* Use simple conversion recipes to convert between the Minecraft Furnace and Patchwork's 3D furnace blocks.

### Patchwork-1.12.2-0.2.3.0
**What's New**
* Config options to change the mob caps for natural world spawn.

**Fixes**
* Block surface that a Shop Shelf sits on will render correctly with more current versions of Forge.

**Changes**
* Bumped repair paste repair amount from 100 to 250.  You will need to udpate your config manually if you have an existing MC instance.

### Patchwork-1.12.2-0.2.2.0
**What's New**
* Wood Pile to make charcoal

**Changes**
* Configurable amount of damaged repair with Repair Paste.
* Ring of Flight can be refueled with Essence of Flight.  Also changed recipes.

### Patchwork-1.12.2-0.2.1.0
**Fixes**
* Shop Shelf renders correctly with cutout or translucent textures, like glass, stained glass, and cobwebs.

### Patchwork-1.12.2-0.2.0.0
**What's New**
* Opening an unowned Shop Shelf while in creative mode will set the shelf to a Server Shop.

**Fixes**
* Mouse scroll wheel now properly increases/descreases stacks in shop shelf config gui.
* Fixed dark rendering of items in shop shelf/furnace when solid block placed on top

**Changes**
* Brighten the coins a bit and change colors slightly to have more differentiation.