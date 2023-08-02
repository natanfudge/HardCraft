package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BlockMixinHandler {
    @JvmStatic
    fun onBlockPlaced(world: World, pos: BlockPos) {
        //TODO: If itemstack is damaged... set health accordingly...

        // Reset current health of block when it is placed
        CurrentHealthStorage.delete(world, pos)
    }
}