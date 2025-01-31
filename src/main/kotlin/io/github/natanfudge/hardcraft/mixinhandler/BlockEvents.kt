package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.getBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.deleteCurrentBlockHealth
import io.github.natanfudge.hardcraft.health.setCurrentBlockHealth
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object BlockEvents {
    @JvmStatic
    fun onBlockReplaced(world: ServerWorld, pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        world.deleteCurrentBlockHealth(pos)
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