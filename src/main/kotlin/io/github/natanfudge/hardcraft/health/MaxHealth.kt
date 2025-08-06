package io.github.natanfudge.hardcraft.health

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.roundToInt

/**
 * Returns the max block health of a block.
 * If the block's max health is not relevant, for example for air blocks, returns null
 */
fun World.getMaxBlockHealth(pos: BlockPos): Int? {
    val result = getMaxBlockHealthOrMinus1(pos)
    return if (result == -1) null else result
}

/**
 * Same as [getMaxBlockHealth] except returns -1 instead of null for fail cases.
 * This method exists to avoid boxing for nullable int.
 */
fun World.getMaxBlockHealthOrMinus1(pos: BlockPos): Int {
    val state = getBlockState(pos)
    if (state.isAir) return -1
    return state.block.getMaxHealthOrMinus1()
}

/**
 * If this is an item of a block, returns its max health.
 */
fun ItemStack.getBlockMaxHealth(): Int? = item.getBlockMaxHealth()
fun Item.getBlockMaxHealth(): Int? = (this as? BlockItem)?.block?.getMaxHealth()

fun Block.getMaxHealth(): Int? {
    val health = getMaxHealthOrMinus1()
    return if (health == -1) null else health
}

fun Block.getMaxHealthOrMinus1(): Int {
    if (hardness <= 0) return -1
    return (hardness * 1000).roundToInt()
}

/**
 * Returns true if the block at the position is part of the CH system meaning mobs can destroy it.
 */
fun World.isDestroyable(pos: BlockPos) = getMaxBlockHealth(pos) != null
