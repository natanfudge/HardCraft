package io.github.natanfudge.hardcraft.health

import io.github.natanfudge.genericutils.destroyBlock
import io.github.natanfudge.genericutils.isServer
import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.Packets
import it.unimi.dsi.fastutil.longs.Long2IntMap
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

/**
 * Minecraft doesn't provide a mechanism for loading PersistentStorage for clients,
 * so we keep a map ourselves so a client can still reach for the data. We sync the data ourselves.
 */
var clientStorage: CurrentHealthStorage? = null

private data class PendingHealthUpdate(
    val pos: BlockPos,
    val health: Int?,
    /**
     * Will be called once the update is applied
     */
    val callback: () -> Unit,
)


/**
 * CurrentHealthStorage data implementation
 */
typealias CHSData = Long2IntMap

/**
 * Generifying this is not possible because it uses Long2IntMap for efficiency. If it was generic all the primitive values will be boxed. Where is Valhalla?
 */
class CurrentHealthStorage(private val world: World, private val map: CHSData) : PersistentState() {
    /**
     * Due to the uncontrollable ordering nature of hooking into minecraft events,
     * we have cases where we need to update [CurrentHealthStorage], but doing
     * so will remove important information that we need.
     * For example, when a block is broken we need to:
     * - Set the dropped itemstack's health to be the same as the broken block
     * - Delete the current-health entry at that block's position.
     * However, Minecraft performs these operations at the opposite order:
     * - It first removes blocks with setBlockState
     * - Only afterwards it drops loot.
     *
     * So if we remove current-health entries on setBlockState, we will lose the data for the dropped itemstack.
     * We can't delete the entry after every loot drop, because sometimes blocks are removed without dropping loot,
     * sometimes they are removed without even being broken.
     *
     * So what we do is this:
     * - When a player breaks a block, we **lock** [CurrentHealthStorage].
     * - on setBlockState, we attempt to delete the entry, but since [CurrentHealthStorage] is locked,
     * changes will only be made once [CurrentHealthStorage] is unlocked.
     * - On loot drop, the information will be available.
     * - Only once the breaking event has completely ended, [CurrentHealthStorage] will be unlocked and changes will occur.
     *
     * This ensures correct ordering, without doing something like coroutine delay hacks.
     */
    private var locked = false

    /**
     * @see locked
     */
    private val pendingChanges = mutableListOf<PendingHealthUpdate>()

    /**
     * @see locked
     */
    fun lock() {
        locked = true
    }

    /**
     * @see locked
     */
    fun unlock() {
        locked = false
        for ((pos, health, onSuccess) in pendingChanges) {
            set(pos, health, onSuccess)
        }
        pendingChanges.clear()
    }


    val allValues: CHSData = map

    /**
     * Sends health info from the server to a player
     */
    fun sendAll(toPlayer: ServerPlayerEntity) {
        Packets.loadBlockHealth.send(Packets.LoadBlockHealth(map), toPlayer)
    }

//    /**
//     * Forgets CH info about a specific position, essentially making it not be damaged.
//     */
//    fun delete(blockPos: BlockPos) {
//        markDirty()
//        map.remove(blockPos.asLong())
//    }

    /**
     * @param onSuccess Is called once the value has been set. Might not happen as soon as this is called.
     */
    fun set(blockPos: BlockPos, value: Int?, onSuccess: () -> Unit) {
        val key = blockPos.asLong()
        val actualValue = when (value) {
            // Having max health is same as not having a value
            null, -1, world.getMaxBlockHealth(blockPos) -> null
            else -> value
        }

        val existingValue = map.getOrElse(key) { null }
        if (existingValue == actualValue) {
            // No need to do anything if the value hasn't changed
            return /*false*/
        }
        if (locked) {
            // See 'locked' documentation
            pendingChanges.add(PendingHealthUpdate(blockPos, actualValue, onSuccess))
            return /*false*/
        }

        markDirty()
        if (actualValue == null) {
            map.remove(key)
        } else {
            if (actualValue <= 0 && world.isServer) world.destroyBlock(blockPos)
            val maxHealth = world.getMaxBlockHealth(blockPos) ?: return /*false*/
            val newValue = actualValue.coerceIn(0, maxHealth)
            map.put(key, newValue)
        }
        onSuccess()

    }

    /**
     * Returns the current health of the block at [blockPos] if it is damaged, or its max health if it isn't.
     */
    fun get(blockPos: BlockPos): Int? {
        return map.getOrElse(blockPos.asLong()) { world.getMaxBlockHealth(blockPos) }
    }


    companion object {
        private const val PersistentId = "${HardCraft.ModId}.CurrentHealthStorage"
        private const val ItemStackCurrentHealthNbt = "${HardCraft.ModId}.CurrentBlockHealth"
//        private const val ItemStackMaxHealthNbt = "${HardCraft.ModId}.MaxBlockHealth"

        fun ItemStack.setBlockCurrentHealth(health: Int) = setSubNbt(ItemStackCurrentHealthNbt, NbtInt.of(health))
        fun ItemStack.getBlockCurrentHealth(): Int? {
            val nbt = nbt ?: return null
            if (nbt.contains(ItemStackCurrentHealthNbt)) {
                return nbt.getInt(ItemStackCurrentHealthNbt)
            } else {
                return getBlockMaxHealth()
            }
        }
//        fun ItemStack.setBlockMaxHealth(health: Int) = setSubNbt(ItemStackMaxHealthNbt, NbtInt.of(health))
//        fun ItemStack.getBlockMaxHealth(): Int? = nbt?.getInt(ItemStackMaxHealthNbt)


        /**
         * Sends health info from the server to a player
         */
        fun sendWorldData(toPlayer: ServerPlayerEntity) {
            getStorage(toPlayer.world)?.sendAll(toPlayer)
        }


        /**
         * @param onSuccess Is called once the value has been set. Might not happen as soon as this is called.
         */
        @JvmStatic
        fun set(world: World, pos: BlockPos, value: Int?, onSuccess: () -> Unit) {
            getStorage(world)?.set(pos, value, onSuccess)
        }

        fun lock(world: World) {
            getStorage(world)?.lock()
        }

        fun unlock(world: World) {
            getStorage(world)?.unlock()
        }

//        @JvmStatic
//        fun delete(world: World, pos: BlockPos) {
//            getStorage(world)?.delete(pos)
//        }

        /**
         * Note: will return null if the block is at full health OR if block health was not loaded yet.
         * Perhaps we should allow differentiating between them.
         */
        @JvmStatic
        fun get(world: World, pos: BlockPos): Int? {
            return getStorage(world)?.get(pos)
        }

        /**
         * Similarly to MinecraftClient.getInstance(), gets the singular instance in the client-only that stores CH data.
         */
        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun getClientStorage(): CurrentHealthStorage? {
            return clientStorage
        }


        private fun getStorage(world: World): CurrentHealthStorage? {
            if (world is ServerWorld) {
                return world.persistentStateManager.getOrCreate(
                    { CurrentHealthStorage(world, CHSDataFromNbt(it)) },
                    { CurrentHealthStorage(world, createCHSData(size = null)) },
                    PersistentId
                )
            } else {
                return getClientStorage()
            }
        }
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        map.writeToNbt(nbt)
        return nbt
    }


}

fun World.getCurrentBlockHealth(pos: BlockPos): Int? = CurrentHealthStorage.get(this, pos)

fun ServerWorld.deleteCurrentBlockHealth(pos: BlockPos) {
    return setCurrentBlockHealth(pos, null)
}


/**
 * These methods are ServerWorld because they should only be called on the server, and they will automatically send a packet to the client to update it.
 *  * Returns true if setting health was successful (it won't be if the set amount is the same as the old amount)
 *
 *  @param amount If null, the amount will be deleted from that position
 */
fun ServerWorld.setCurrentBlockHealth(pos: BlockPos, amount: Int?) {
    CurrentHealthStorage.set(this, pos, amount) {
        Packets.updateBlockHealth.sendToWorld(Packets.UpdateBlockHealth(pos, amount), this)
    }

//    return changed
}

/**
 * Returns true if repairing was successful
 */
fun ServerWorld.repairBlock(pos: BlockPos, amount: Int) {
    val old = getCurrentBlockHealth(pos) ?: return /*false*/
    return setCurrentBlockHealth(pos, old + amount)
}

/**
 * Returns true if damaging was successful
 */
fun ServerWorld.damageBlock(pos: BlockPos, amount: Int)/*: Boolean*/ = repairBlock(pos, -amount)

