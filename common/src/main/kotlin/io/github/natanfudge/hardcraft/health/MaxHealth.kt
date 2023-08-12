package io.github.natanfudge.hardcraft.health

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.roundToInt

fun World.getMaxBlockHealth(pos: BlockPos): Int? {
    val result = getMaxBlockHealthOrMinus1(pos)
    return if (result == -1) null else result
}

fun World.getMaxBlockHealthOrMinus1(pos: BlockPos): Int {
    val state = getBlockState(pos)
    if (state.isAir) return -1
    val hardness = state.block.hardness
    if (hardness <= 0) return -1
    //TODO: custom health
    return (hardness * 1000).roundToInt()
}

fun World.isDestroyable(pos: BlockPos) = getMaxBlockHealth(pos) != null

//fun World.getExistingMaxBlockHealth(pos: BlockPos) =
//    getMaxBlockHealth(pos) ?: error("Expected block to exist in pos $pos for the purpose of getting max health, but none existed there!")