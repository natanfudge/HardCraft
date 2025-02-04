package io.github.natanfudge.hardcraft.support

import io.github.natanfudge.hardcraft.utils.getBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldAccess

interface IBlock {
    fun isAir(): Boolean
    fun isBedrock(): Boolean
}


interface IWorld {
    fun getBlock(pos: BlockPos): IBlock
}

fun IWorld.isAir(pos: BlockPos): Boolean {
    return this.getBlock(pos).isAir()
}

class RealBlock(private val block: BlockState) : IBlock {
    override fun isAir(): Boolean = block.isAir
    override fun isBedrock(): Boolean = block == Blocks.BEDROCK
}

// On second call, I know we run on Worker-Main-10 and not the main server thread.
class RealWorld(val world: WorldAccess) : IWorld {
    override fun getBlock(pos: BlockPos): IBlock {
        val state = world.getBlockState(pos)
        return RealBlock(state)
    }
}

class VirtualWorld(val blocks: Map<BlockPos, IBlock>) : IWorld {
    override fun getBlock(pos: BlockPos): IBlock = blocks[pos] ?: VirtualBlock.AIR
}

class VirtualBlock : IBlock {
    override fun isAir(): Boolean = this == AIR
    override fun isBedrock(): Boolean = this == BEDROCK

    companion object {
        val AIR = VirtualBlock()
        val BEDROCK = VirtualBlock()
        val STONE = VirtualBlock()
    }
}