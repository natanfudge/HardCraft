package io.github.natanfudge.genericutils.inventory

import io.github.natanfudge.genericutils.forEachNonEmptyStackIndexed
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList

private const val ItemStackListSlotKey = "Slot"
private const val ItemStackListKey = "Items"

/**
 * Utilities for reading and writing an Inventory to and from NBT in the same format as vanilla.
 */
fun Inventory.writeItemsNbt(nbt: NbtCompound) {
    val nbtList = NbtList()
    forEachNonEmptyStackIndexed { i, stack ->
        val compound = NbtCompound()
        compound.putByte(ItemStackListSlotKey, i.toByte())
        stack.writeNbt(compound)
        nbtList.add(compound)
    }
    nbt.put(ItemStackListKey, nbtList)
}

fun Inventory.readItemsNbt(nbt: NbtCompound) {
    val nbtList = nbt.getList(ItemStackListKey, NbtElement.COMPOUND_TYPE.toInt())

    for (i in nbtList.indices) {
        val nbtCompound = nbtList.getCompound(i)
        val j = nbtCompound.getByte(ItemStackListSlotKey).toInt() and 255
        setStack(j, ItemStack.fromNbt(nbtCompound))
    }
}
