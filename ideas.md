# 0.1 Complete Prototype Requirements

## 1. Hardcraft mechanics

- Verified for Zombies, Skeletons, and Spiders, and Creepers.
- Digging down, Climbing up, and breaking towards the target, when there is no path. 
- Water Resistance, piston resistance, Lava resistance, and APIs to activate/deactivate them
- API for assigning block break power of mobs

## 2. Revenge of the Mobs mechanics

- 10 Waves of mobs spawn at night, each increasing in difficulty:
  - Scaling health
  - Scaling count
  - Scaling damage
  - Scaling speed
  - Scaling block break power
  - Wave 2 - Water Resistance
  - Wave 4 - Lava Resistance
  - Wave 6 - Piston Resistance
- You die you lose
- GUI shows countdown for when the spawning starts, and the current wave/max wave. 

#  Ideas

## Rogue-like mechanics

Upon completing a wave, get loot that allows you to choose between different upgrades. 

## Reworked Creepers

It doesn't make much sense that creeps can dig since they have no hands. Instead, they should act as "wall breakers", and will explode as soon as they reach a wall when they can't find a path. Creepers have increased blast radius and only deal a set of amount of damage to blocks, only outright destroying them if the blocks can't withstand the damage. 


## New mob mechanics
  - Mobs may have a bigger step height.
  - It makes sense for mobs to have a set of capabilities, and then unlock them as the game progresses, for example:
      - Level 1: Vanilla Mobs
        - Level 2: Unlock breaking blocks
        - Level 3: Unlock Climbing
        - Level 4: Water resistance
        - Level 5: Lava Resistance
        - Level 6: Push resistance

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



## Mob block Damage
Each mob needs to have an amount of block damage assigned to it. 
## Reworked objective
- Instead of losing instantly when you die, a more reasonable way would be to allow dying and respawning as normal, but there is some penalty (not respawn timer, lol)
- The objective then becomes defending something ("The Ender Dragon Egg" maybe), and if you die it will hinder you instead of ending the game. 

## Leaderboards Site 
- When you lose, you have the option of uploading your results to a site maintained by me. The results will include the amount of time you survived, as well as the modpack you used (or vanilla). Results will only count as "verified" if you have full recorded footage of your run, and 5 people confirmed the run. The main leaderboard will only show verified results. It's possible to report a run as not genuine to get it removed. 

## Mob placing improvements
- Perhaps we can make the mobs gather blocks and only then they can place them.



# DONE:

- Give every block a damage value that is persistent. Enemies may damage blocks and finally - destroy them.
- Mobs will always know how to find players. They will pathfind a way to them, and if they can't, they will break blocks to get where they need.

## New block mechanics

### Max health value

- Every block has a certain amount of initial health that determines how hard it is to destroy.
- Unless otherwise specified, the max health value directly correlates with the hardness of a block.

### Current health Value

- Every block has a current health value that is initialized as the max health value.
- When a damaged block is harvested by a player, it retains its current health value and will stay damaged when it is placed again.
- The % of max health remaining is displayed as durability on the block.
- Blocks will appear cracked when they have taken damage. The more damage - the more cracked
- WAILA displays the exact amount of currentHealth/maxHealth.

### Support

- Blocks (other than bedrock) must be supported by another block - it must have a support path to bedrock.
- This means that there are no restrictions to *placing* blocks, but *breaking or destroying* blocks can have serious consequences.
- When a block is broken, blocks (efficiently) check if they lost their support. If so, they will fall down like gravel.

## New mob mechanics

  - All mobs actively try to fuck up the player at all times. 
  - Mobs, unfairly, find the closest player, with no respect to distance or vision, and try to kill him.
  - If a reasonable path is found, the mob will use the path and attack the player as usual.
  - If no path exists and the mob is on the same level or higher than the play - mobs will break blocks along the way to get to the player.

## New Itemstack Mechanics

- When blocks are broken, they retain their health value as an ItemStack.
- Health value is shown as damage on the item
