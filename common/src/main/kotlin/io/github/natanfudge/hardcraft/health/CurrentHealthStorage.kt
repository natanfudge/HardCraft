package io.github.natanfudge.hardcraft.health

import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.destroyBlock
import io.github.natanfudge.genericutils.isServer
import io.github.natanfudge.hardcraft.Packets
import it.unimi.dsi.fastutil.longs.Long2IntMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.world.ClientWorld
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

//TODO: break blog progress issues.
// 1. It seems to randomly reset
// 2. it's not retained when you log back in.
// 3. Probably not retained when loading chunk.
// 4. Doesn't get reset when block is re-placed

/**
 * Minecraft doesn't provide a mechanism for loading PersistentStorage for clients,
 * so we keep a map ourselves so a client can still reach for the data. We sync the data ourselves.
 */
//TODO: make a single value and test
//private var clientStorage = CurrentHealthStorage()
private lateinit var clientStorage: CurrentHealthStorage
//private val serializer = MapSerializer(Long.serializer(), Int.serializer())


typealias CurrentHealthStorageDataImpl = Long2IntMap

//TODO: generify PersistentState using serialization. I think I'll wait unti I implement client syncing as well so my generic impl will have that as an option too.
// Worth noting there's no point in having a map of World,CurrentHealthStorage since clients only have one world loaded at a time. Better to just have some lateinit var World.
class CurrentHealthStorage(private val world: World, private val map: CurrentHealthStorageDataImpl) : PersistentState() {
    // In order to set the breaking animation of a block, we use setBlockBreakingInfo which requires a seperate ID for each block.
    private var usedIds = 10_000


    val allValues: CurrentHealthStorageDataImpl = map

    /**
     * Sends health info from the server to a player
     */
    fun sendAll(toPlayer: ServerPlayerEntity) {
        Packets.loadBlockHealth.send(Packets.LoadBlockHealth(map), toPlayer)
    }

    /**
     * Receives health info on the client on a player
     */
    fun load(values: CurrentHealthStorageDataImpl) {
        // Don't need to markDirty() because client values don't need to be saved
        map.clear()
        map.fastPutAll(values)
    }

    fun delete(blockPos: BlockPos) {
        markDirty()
        map.remove(blockPos.asLong())
    }

    fun set(blockPos: BlockPos, value: Int): Boolean {
        markDirty()
        if (value <= 0 && world.isServer) world.destroyBlock(blockPos)
        val maxHealth = world.getMaxBlockHealth(blockPos) ?: return false
        val newValue = value.coerceIn(0, maxHealth)
        if (world.isClient) {
            // Higher stage - more broken
            val stage = 10 - (newValue.toFloat() / maxHealth) * 10
            getClient().worldRenderer.setBlockBreakingInfo(usedIds++, blockPos, stage.toInt())
        }
        val key = blockPos.asLong()
        if (newValue == maxHealth) {
            // Having max health is same as not having a value
            map.remove(key)
        } else {
            map.put(key, newValue)
        }
        return true
    }
    //TODO: data was not loaded properly. Seems like the issue is syncing to client.

    fun get(blockPos: BlockPos): Int? {
        return map.getOrElse(blockPos.asLong()) { world.getMaxBlockHealth(blockPos) }
    }
//    fun getIfDamaged(blockPos: BlockPos) = map.get(blockPos.asLong())


    companion object {
        private const val PersistentId = "hardcraft.CurrentHealthStorage"

        /**
         * Sends health info from the server to a player
         */
        fun sendWorldData(toPlayer: ServerPlayerEntity) {
            getStorage(toPlayer.world).sendAll(toPlayer)
        }

        /**
         * Receives health info on the client on a player
         */
        @Environment(EnvType.CLIENT)
        fun load(world: ClientWorld, values: CurrentHealthStorageDataImpl) {
            clientStorage = CurrentHealthStorage(world, values)
            clientStorage.load(values)
        }

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

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun getClientStorage() = clientStorage


        private fun getStorage(world: World): CurrentHealthStorage {
            if (world is ServerWorld) {
                return world.persistentStateManager.getOrCreate(
                    { CurrentHealthStorage(world, currentHealthDataFromNbt(it)) },
                    { CurrentHealthStorage(world, createCurrentHealthStorageDataImpl(size = null)) },
                    PersistentId
                )
            } else {
                return getClientStorage()
            }
        }
    }
    //TODO: test new storage format

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        map.writeToNbt(nbt)
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

