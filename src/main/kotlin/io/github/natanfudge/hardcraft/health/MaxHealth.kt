package io.github.natanfudge.hardcraft.health

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.roundToInt

/**
 * Returns the max block health of a block.
 * If the block's max health is not relevant, for example for air blocks, returns null
 */
fun World.getMaxBlockHealth(pos: BlockPos): Int? {
    val result = getMaxBlockHealthOrMinus1(pos)
    return if (result == -1) null else result
}

/**
 * Same as [getMaxBlockHealth] except returns -1 instead of null for fail cases.
 * This method exists to avoid boxing for nullable int.
 */
fun World.getMaxBlockHealthOrMinus1(pos: BlockPos): Int {
    val state = getBlockState(pos)
    if (state.isAir) return -1
    val hardness = state.block.hardness
    if (hardness <= 0) return -1
    //TODO: custom health
    return (hardness * 1000).roundToInt()
}

/**
 * Returns true if the block at the position is part of the CH system meaning mobs can destroy it.
 */
fun World.isDestroyable(pos: BlockPos) = getMaxBlockHealth(pos) != null
