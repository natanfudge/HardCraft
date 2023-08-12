package io.github.natanfudge.hardcraft.health

import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.longs.Long2IntMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.minecraft.nbt.NbtCompound

/**
 * This file takes great care to use the fast methods of Long2IntMap and not the slow generic ones!
 */
fun CurrentHealthStorageDataImpl.writeToBuf(buf: ByteBuf) {
    buf.writeInt(size)
    for (entry in this.long2IntEntrySet()) {
        buf.writeLong(entry.longKey)
        buf.writeInt(entry.intValue)
    }
}

fun currentHealthDataFromByteBuf(buf: ByteBuf): CurrentHealthStorageDataImpl {
    val size = buf.readInt()
    val map = Long2IntOpenHashMap(size).apply {defaultReturnValue(NullValue)}
    repeat(size) {
        map.put(buf.readLong(), buf.readInt())
    }
    return map
}

fun CurrentHealthStorageDataImpl.writeToNbt(nbt: NbtCompound): NbtCompound {
    return nbt.apply {
        for (entry in long2IntEntrySet()) {
            putInt(entry.longKey.toString(), entry.intValue)
        }
    }
}
 const val NullValue = -1

fun currentHealthDataFromNbt(nbt: NbtCompound): CurrentHealthStorageDataImpl {
    val map = Long2IntOpenHashMap(nbt.size).apply { defaultReturnValue(NullValue) }
    for (key in nbt.keys) {
        map.put(key.toLong(), nbt.getInt(key))
    }
    return map
}

fun Long2IntMap.fastPutAll(other: Long2IntMap) {
    // Deconstructing is bad
    for (entry in other.long2IntEntrySet()) {
        put(entry.longKey, entry.intValue)
    }
}

fun Long2IntMap.getOrNull(key: Long) = get(key).let { if(it == NullValue) null else it }
inline fun Long2IntMap.getOrElse(key: Long, other: () -> Int?) = get(key).let { if(it == NullValue) other() else it }