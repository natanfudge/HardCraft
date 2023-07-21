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
  - The % of max health remaining is displayed as durability on the block. 
  - Blocks will appear cracked when they have taken damage. The more damage - the more cracked
  - WAILA displays the exact amount of currentHealth/maxHealth. 
  ### Support 
  - Blocks (other than bedrock) must be supported by another block - it must have a support path to bedrock. 
  - This means that there are no restrictions to *placing* blocks, but *breaking or destroying* blocks can have serious consequences. 
  - When a block is broken, blocks (efficiently) check if they lost their support. If so, they will fall down like gravel. 

## New items
  ### Repair kit.
  - May be used on a damaged block to repair it.
  - Repairing is done by holding down right click on an item and healing it over time.
  - Repairing is done on a health-per-second basis, meaning high health blocks take longer to repair and take more durability. 

## New mob mechanics
  - All mobs actively try to fuck up the player at all times. 
  - Mobs, unfairly, find the closest player, with no respect to distance or vision, and lock on to him. 
  - They may break the lock with certain triggers e.g. time passed or damage taken and lock into a different player. 
  - Once a player has been locked, the mob will do everything in its power to kill the player. 
  - If a reasonable path is found, the mob will use the path and attack the player as usual.
  - If no path exists, and a tunnel may be made to reach the player, mobs will break blocks along the way to get to the player. 
  - If no path exists and no tunnel may be made, mobs will break blocks that support the player - destroying sky bases. 
  - Mobs may be dumb - and destroy anything no matter how strong the block is - or be smart and find the weakest blocks to destroy to get to the player. 

