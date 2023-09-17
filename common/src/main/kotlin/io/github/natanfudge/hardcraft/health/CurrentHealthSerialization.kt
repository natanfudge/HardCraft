package io.github.natanfudge.hardcraft.health

import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.longs.Long2IntMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.minecraft.nbt.NbtCompound


/**
 * This file takes great care to use the fast methods of Long2IntMap and not the slow generic ones!
 */


/**
 * Creates a [CHSData]
 */
fun createCHSData(size: Int?): CHSData {
    val map = if(size == null) Long2IntOpenHashMap() else Long2IntOpenHashMap(size)
    map.defaultReturnValue(CurrentHealthNullValue)
    return map
}

// To and from buf
fun CHSData.writeToBuf(buf: ByteBuf) {
    buf.writeInt(size)
    for (entry in this.long2IntEntrySet()) {
        buf.writeLong(entry.longKey)
        buf.writeInt(entry.intValue)
    }
}

fun CHSDataFromBuf(buf: ByteBuf): CHSData {
    val size = buf.readInt()
    val map = createCHSData(size)
    repeat(size) {
        map.put(buf.readLong(), buf.readInt())
    }
    return map
}


// To and from NBT
fun CHSData.writeToNbt(nbt: NbtCompound): NbtCompound {
    return nbt.apply {
        for (entry in long2IntEntrySet()) {
            putInt(entry.longKey.toString(), entry.intValue)
        }
    }
}

fun CHSDataFromNbt(nbt: NbtCompound): CHSData {
    val map = createCHSData(nbt.size)
    for (key in nbt.keys) {
        map.put(key.toLong(), nbt.getInt(key))
    }
    return map
}

inline fun Long2IntMap.getOrElse(key: Long, other: () -> Int?) = get(key).let { if(it == CurrentHealthNullValue) other() else it }
@PublishedApi internal const val CurrentHealthNullValue = -1