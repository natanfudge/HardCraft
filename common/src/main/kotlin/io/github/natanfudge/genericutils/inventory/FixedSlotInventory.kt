package io.github.natanfudge.genericutils.inventory

import io.github.natanfudge.genericutils.forEachNonEmptyStackIndexed
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.collection.DefaultedList

/**
 * Easy to use Inventory implementation of a fixed amount of slots
 */
class FixedSlotInventory(slots: Int) : Inventory {
    private val inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY)

    override fun clear() {
        inventory.clear()
    }

    override fun size(): Int {
        return inventory.size
    }

    override fun isEmpty(): Boolean {
        for (i in 0 until size()) {
            val stack = getStack(i)
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getStack(slot: Int): ItemStack {
        return inventory[slot]
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        return Inventories.removeStack(inventory, slot);
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        inventory[slot] = stack;
        if (stack.count > stack.maxCount) {
            stack.count = stack.maxCount;
        }
    }

    override fun markDirty() {
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

}

