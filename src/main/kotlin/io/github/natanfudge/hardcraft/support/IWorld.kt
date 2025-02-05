package io.github.natanfudge.hardcraft.support

import io.github.natanfudge.hardcraft.utils.canSupportOtherBlocks
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldAccess

interface IBlock {
    fun canSupportOtherBlocks(): Boolean
    fun isWorldBottom(): Boolean
}


interface IWorld {
    fun getBlock(pos: BlockPos): IBlock
}

fun IWorld.canSupportOtherBlocks(pos: BlockPos): Boolean {
    return this.getBlock(pos).canSupportOtherBlocks()
}

class RealBlock(private val block: BlockState, private val world: WorldAccess, private val pos: BlockPos) : IBlock {
    override fun canSupportOtherBlocks(): Boolean = block.canSupportOtherBlocks(world, pos)

    // Void air is good enough, there SHOULD be bedrock in that area.
    override fun isWorldBottom(): Boolean = block.block == Blocks.BEDROCK || block.block == Blocks.VOID_AIR
}

// On second call, I know we run on Worker-Main-10 and not the main server thread.
class RealWorld(val world: WorldAccess) : IWorld {
    override fun getBlock(pos: BlockPos): IBlock {
        val state = world.getBlockState(pos)
        return RealBlock(state, world, pos)
    }
}

class VirtualWorld(val blocks: Map<BlockPos, IBlock>) : IWorld {
    override fun getBlock(pos: BlockPos): IBlock = blocks[pos] ?: VirtualBlock.AIR
}

class VirtualBlock : IBlock {
    override fun canSupportOtherBlocks(): Boolean = this == AIR
    override fun isWorldBottom(): Boolean = this == BEDROCK

    companion object {
        val AIR = VirtualBlock()
        val BEDROCK = VirtualBlock()
        val STONE = VirtualBlock()
    }
}