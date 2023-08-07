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
private val clientStorage = mutableMapOf<World, CurrentHealthStorage>()
private val serializer = MapSerializer(BlockPosSerializer, Int.serializer())

//TODO: generify PersistentState using serialization. I think I'll wait unti I implement client syncing as well so my generic impl will have that as an option too.
// Worth noting there's no point in having a map of World,CurrentHealthStorage since clients only have one world loaded at a time. Better to just have some lateinit var World.
class CurrentHealthStorage(private val world: World, private val map: MutableMap<BlockPos, Int> = mutableMapOf()) : PersistentState() {
    // In order to set the breaking animation of a block, we use setBlockBreakingInfo which requires a seperate ID for each block.
    private var usedIds = 10_000

    /**
     * Sends health info from the server to a player
     */
    fun sendAll(toPlayer: ServerPlayerEntity) {
        Packets.loadBlockHealth.send(Packets.LoadBlockHealth(map), toPlayer)
    }

    /**
     * Receives health info on the client on a player
     */
    fun load(values: Map<BlockPos, Int>) {
        // Don't need to markDirty() because client values don't need to be saved
        map.clear()
        map.putAll(values)
    }

    fun delete(blockPos: BlockPos) {
        markDirty()
        map.remove(blockPos)
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
        map[blockPos] = newValue
//        println("Health at $blockPos set to $newValue")
        return true
    }

    fun get(blockPos: BlockPos) = map[blockPos] ?: world.getMaxBlockHealth(blockPos)


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
        fun load(world: ClientWorld, values: Map<BlockPos, Int>) {
            getStorage(world).load(values)
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

