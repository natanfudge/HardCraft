package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.getBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.deleteCurrentBlockHealth
import io.github.natanfudge.hardcraft.health.setCurrentBlockHealth
import io.github.natanfudge.hardcraft.support.Support
import io.github.natanfudge.hardcraft.utils.canSupportOtherBlocks
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object BlockEvents {
    @JvmStatic
    fun onBlockReplaced(world: ServerWorld, pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        if (world.server.isOnThread) {
//            val loaded = world.isChunkLoaded(pos)

//            world.server.execute {
            world.deleteCurrentBlockHealth(pos)
            // If old block could support, but new block can't, we may need to drop blocks
//            if (oldBlock.canSupportOtherBlocks(world, pos) && !newBlock.canSupportOtherBlocks(world, pos)) {
//                val floating = Support.floatingBlocksAfterRemoval(world, pos)
//
//                println("Floating result: ${floating.size} items")
//                for (floatingBlock in floating) {
//                    world.removeBlock(floatingBlock, false)
//                }
//            }
//            }
//            world.server.execute {

//            }

        } else {
            // Don't really care about "non-main" block placements, such as those that occur on chunk load.
//            world.server.execute {
//                onBlockReplaced(world, pos, oldBlock, newBlock)
//            }
//            HardCraft.Logger.error("Not expecting onBlockReplaced to run on a non-server thread!")
            //TODO: I think this should be scheduled on the main thread otherwise
//            world.server.execute {
//                onBlockReplaced(world, pos, oldBlock, newBlock)
//            }
        }

    }

    @JvmStatic
    fun onBlockPlacedByItemStack(context: ItemPlacementContext) {
        val world = context.world as? ServerWorld ?: return
        world.setCurrentBlockHealth(context.blockPos, context.stack.getBlockCurrentHealth())


        // Reset current health of block when it is placed so it doesn't retain the old value
        // (we don't have an easy way of deleting the value when the block is destroyed)
//        CurrentHealthStorage.delete(world, pos)
    }

    @JvmStatic
    fun beforeTryBlockBreak(world: ServerWorld, pos: BlockPos) {
        /**
         * See CurrentHealthStorage.locked
         */
        CurrentHealthStorage.lock(world)
    }

    @JvmStatic
    fun afterTryBlockBreak(world: ServerWorld, pos: BlockPos) {
        /**
         * See CurrentHealthStorage.locked
         */
        CurrentHealthStorage.unlock(world)
    }
}