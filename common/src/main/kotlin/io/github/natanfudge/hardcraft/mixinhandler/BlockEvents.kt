package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BlockEvents {
    @JvmStatic
    fun onBlockPlaced(world: World, pos: BlockPos) {
        //TODO: If itemstack is damaged... set health accordingly...

        // Reset current health of block when it is placed so it doesn't retain the old value
        // (we don't have an easy way of deleting the value when the block is destroyed)
        CurrentHealthStorage.delete(world, pos)
    }
}