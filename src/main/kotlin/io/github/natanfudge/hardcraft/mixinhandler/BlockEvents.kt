package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.getBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.deleteCurrentBlockHealth
import io.github.natanfudge.hardcraft.health.setCurrentBlockHealth
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object BlockEvents {
    @JvmStatic
    fun onBlockReplaced(world: ServerWorld, pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        if (world.server.isOnThread) {
            world.deleteCurrentBlockHealth(pos)
        } else {
            HardCraft.Logger.error("Not expecting onBlockReplaced to run on a non-server thread!")
            // SUS: should we do this?
//            world.server.execute {
//                onBlockReplaced(world, pos, oldBlock, newBlock)
//            }
        }

    }

    @JvmStatic
    fun onBlockPlacedByItemStack(context: ItemPlacementContext) {
        val world = context.world as? ServerWorld ?: return
        world.setCurrentBlockHealth(context.blockPos, context.stack.getBlockCurrentHealth())
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