package io.github.natanfudge.hardcraft

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
import java.io.File

private val clientStorage = mutableMapOf<World, CurrentHealthStorage>()
private val serializer = MapSerializer(BlockPosSerializer, Int.serializer())

//TODO: generify PersistentState using serialization. I think I'll wait unti I implement client syncing as well so my generic impl will have that as an option too.
class CurrentHealthStorage(private val world: World, private val map: MutableMap<BlockPos, Int> = mutableMapOf()) : PersistentState() {

    fun set(blockPos: BlockPos, value: Int) {
        map[blockPos] = value.coerceIn(0, world.getExistingMaxBlockHealth(blockPos))
    }

    fun get(blockPos: BlockPos) = map[blockPos] ?: world.getMaxBlockHealth(blockPos)


    companion object {
        private const val PersistentId = "hardcraft.CurrentHealthStorage"

        @JvmStatic
        fun set(world: World, pos: BlockPos, value: Int) {
            getStorage(world).set(pos, value)
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

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        serializer.put(map, nbt)
        return nbt
    }

    override fun save(file: File?) {
        super.save(file)
    }
}

fun World.getBlockCurrentHealth(pos: BlockPos): Int? = CurrentHealthStorage.get(this, pos)
fun World.getExistingBlockCurrentHealth(pos: BlockPos): Int = getBlockCurrentHealth(pos)
    ?: error("Expected block to exist in pos $pos for the purpose of getting current health, but none existed there!")

//TODO:
// 1. Add an easy debug mechanism for damaging a block.
//  - When debug is enabled - holding down shift + click will slowly damage a block.
//  - Implementation - mixin into onUse, then modify the world's CurrentHealthStorage.
//  - Since onUse is called on both client and server we don't need to sync anything.
// 2. Show block data by accessing world's CurrentHealthStorage and adding the damage value in waila.