package io.github.natanfudge.hardcraft.support

import net.minecraft.util.math.BlockPos

/**
 * Hopefully this doesn't conflict with anything
 */
 val BEDROCK_ROOT = CompactBlockPos(Long.MIN_VALUE)

@JvmInline value class CompactBlockPos( val packedPos: Long) {
    override fun toString(): String {
        return toBlockPos().toString()
    }

    fun toBlockPos(): BlockPos {
        return BlockPos.fromLong(packedPos)
    }
}

fun BlockPos.compact() = CompactBlockPos(asLong())