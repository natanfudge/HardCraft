package io.github.natanfudge.hardcraft.health

import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.destroyBlock
import io.github.natanfudge.genericutils.isServer
import io.github.natanfudge.hardcraft.Packets
import io.github.natanfudge.hardcraft.utils.asMutableMap
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.minecraft.BlockPosSerializer
import kotlinx.serialization.minecraft.getFrom
import kotlinx.serialization.minecraft.put
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

private val clientStorage = mutableMapOf<World, CurrentHealthStorage>()
private val serializer = MapSerializer(BlockPosSerializer, Int.serializer())

//TODO: generify PersistentState using serialization. I think I'll wait unti I implement client syncing as well so my generic impl will have that as an option too.
class CurrentHealthStorage(private val world: World, private val map: MutableMap<BlockPos, Int> = mutableMapOf()) : PersistentState() {
    // In order to set the breaking animation of a block, we use setBlockBreakingInfo which requires a seperate ID for each block.
    private var usedIds = 10_000

    fun delete(blockPos: BlockPos) {
        map.remove(blockPos)
    }
    fun set(blockPos: BlockPos, value: Int): Boolean {
        if (value <= 0 && world.isServer) world.destroyBlock(blockPos)
        val maxHealth = world.getMaxBlockHealth(blockPos) ?: return false
        val newValue = value.coerceIn(0, maxHealth)
        if (world.isClient) {
            // Higher stage - more broken
            val stage = 10 - (newValue.toFloat() / maxHealth) * 10
            getClient().worldRenderer.setBlockBreakingInfo(usedIds++, blockPos, stage.toInt())
        }
        map[blockPos] = newValue
//        println("Health at $blockPos set to $newValue")
        return true
    }

    fun get(blockPos: BlockPos) = map[blockPos] ?: world.getMaxBlockHealth(blockPos)


    companion object {
        private const val PersistentId = "hardcraft.CurrentHealthStorage"

        @JvmStatic
        fun set(world: World, pos: BlockPos, value: Int): Boolean {
            return getStorage(world).set(pos, value)
        }
        @JvmStatic
        fun delete(world: World, pos: BlockPos) {
             getStorage(world).delete(pos)
        }

        @JvmStatic
        fun get(world: World, pos: BlockPos): Int? {
            return getStorage(world).get(pos)
        }


        private fun getStorage(world: World): CurrentHealthStorage {
            if (world is ServerWorld) {
                return world.persistentStateManager.getOrCreate(
                    { CurrentHealthStorage(world, serializer.getFrom(it).asMutableMap()) },
                    { CurrentHealthStorage(world) },
                    PersistentId
                )
            } else {
                return clientStorage.computeIfAbsent(world) { CurrentHealthStorage(world) }
            }
        }
    }
    //TODO: test saving blocks

    //TODO: client should recieve currentHealthStorage upon loading world/dimension
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        serializer.put(map, nbt)
        return nbt
    }

}

fun World.getBlockCurrentHealth(pos: BlockPos): Int? = CurrentHealthStorage.get(this, pos)

/**
 * These methods are ServerWorld because they should only be called on the server, and they will automatically send a packet to the client to update it.
 */
fun ServerWorld.setBlockCurrentHealth(pos: BlockPos, amount: Int): Boolean {
    Packets.updateBlockHealth.sendToWorld(Packets.UpdateBlockHealth(pos, amount), this)
    return CurrentHealthStorage.set(this, pos, amount)
}

fun ServerWorld.repairBlock(pos: BlockPos, amount: Int): Boolean {
    val old = getBlockCurrentHealth(pos) ?: return false
    return setBlockCurrentHealth(pos, old + amount)
}

fun ServerWorld.damageBlock(pos: BlockPos, amount: Int): Boolean = repairBlock(pos, -amount)

