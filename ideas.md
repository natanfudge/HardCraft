- Give every block a damage value that is persistent. Enemies may damage blocks and finally - destroy them.
- In order to not make building in the sky overpowered, blocks must be supported by something. 
   This means they must be *above* another block or *near* another block.
  - Then, enemies will try to knock down foundations and bring down sky bases. 
- Mobs will always know how to find players. They will pathfind a way to them, and if they can't, they will break blocks to get where they need. 


## New block mechanics
  ### Max health value
  - Every block has a certain amount of initial health that determines how hard it is to destroy. 
  - Unless otherwise specified, the max health value directly correlates with the hardness of a block.
  ### Current health Value
  - Every block has a current health value that is initialized as the max health value. 
  - When a damaged block is harvested by a player, it retains its current health value and will stay damaged when it is placed again.
  - When the current health of a block reaches 0, it is destroyed. 
    - Easy mode - a destroyed block is dropped with 0 current health
    - Hard mode - a destroyed block is gone forever
  - The % of max health remaining is displayed as durability on the block. 
  - Blocks will appear cracked when they have taken damage. The more damage - the more cracked
  - WAILA displays the exact amount of currentHealth/maxHealth. 
  ### Support 
  - Blocks (other than bedrock) must be supported by another block - it must have a support path to bedrock. 
  - This means that there are no restrictions to *placing* blocks, but *breaking or destroying* blocks can have serious consequences. 
  - When a block is broken, blocks (efficiently) check if they lost their support. If so, they will fall down like gravel. 

## New Itemstack mechanics
- When blocks are broken, they retain their health value as an ItemStack.
- Health value is shown as damage on the item
- When blocks are placed they have the item's damage value

## New items
  ### Repair kit.
  - May be used on a damaged block to repair it.
  - Repairing is done by holding down right click on an item and healing it over time.
  - Repairing is done on a health-per-second basis, meaning high health blocks take longer to repair and take more durability. 
  ### Force Field.
  - May be used to protect against foes.
  - Could be powered and regenerate slowly. 
  ### Confusion bomb
  - Causes mobs to attack each other
  ### Blinding bomb
  - Causes mobs to not be effective at getting to you

## New mob mechanics
  - All mobs actively try to fuck up the player at all times. 
  - Mobs, unfairly, find the closest player, with no respect to distance or vision, and try to kill him.
  - If a reasonable path is found, the mob will use the path and attack the player as usual.
  - If no path exists and the mob is on the same level or higher than the play - mobs will break blocks along the way to get to the player.
  - If no path exists and the player is taller - need to think about this, what are the criteria for destroying Support?
  - Mobs may be dumb - and destroy anything no matter how strong the block is - or be smart and find the weakest blocks to destroy to get to the player. 
  - Mobs may have a bigger step height. 

## Push balance
- Since mobs don't break blocks from far away, pushing them would be too strong. So everything that involves pushing is nerfed.
- Liquids have only a small effect on mobs and they can all swim.
- Pistons have a limited amount of uses 

## Block break balance
- Since blocks are really easy to create, it would be trivial to block out mobs forever. 
- Solution - at first make mobs take a decent amount of time to break blocks. 
  - Then, make the mob's breaking power exponentially stronger, to the point they quickly oneshot normal blocks.
  - At that point the player will have to craft special blocks (walls / force fields) to efficiently keep mobs out since they will have much more protection. 