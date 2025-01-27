package io.github.natanfudge.genericutils

import io.github.natanfudge.hardcraft.HardCraft
import io.netty.buffer.Unpooled
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.pathing.PathNode
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.event.GameEvent



val World.isServer get() = !isClient

fun World.destroyBlock(pos: BlockPos) {
    val state = getBlockState(pos)
    val block = state.block
    syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state))
    emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(state))
    val shouldTriggerEvent = setBlockState(pos, Blocks.AIR.defaultState)
    if (shouldTriggerEvent) {
        block.onBroken(this, pos, state)
    }
}


inline fun World.inServer(action: context(ServerWorld)() -> Unit) {
    if (isServer) action(this as ServerWorld)
}

fun Entity.distanceTo(pos: BlockPos) = squaredDistanceTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

fun createBytebuf() = PacketByteBuf(Unpooled.buffer())