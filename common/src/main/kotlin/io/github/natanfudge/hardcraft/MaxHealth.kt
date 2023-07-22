package io.github.natanfudge.hardcraft

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.roundToInt

fun World.getMaxBlockHealth(pos: BlockPos): Int? {
    val state = getBlockState(pos)
    if (state.isAir) return null
    //TODO: custom health
    return (state.block.hardness * 1000).roundToInt()
}

fun World.getExistingMaxBlockHealth(pos: BlockPos) =
    getMaxBlockHealth(pos) ?: error("Expected block to exist in pos $pos for the purpose of getting max health, but none existed there!")