package io.github.natanfudge.genericutils.network

import io.github.natanfudge.hardcraft.HardCraft
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtInt

fun addGeneratedLoot(consumer: (ItemStack) -> Unit) {
    someInternalBs(consumer)
}

fun modifiedAddGeneratedLoot(consumer: (ItemStack) -> Unit) {
    someInternalBs {
        it.setSubNbt("${HardCraft.ModId}:damage", NbtInt.of(5))
        consumer(it)
    }
}


private fun someInternalBs(consumer: (ItemStack) -> Unit) {

}