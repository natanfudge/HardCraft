package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.setBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.getBlockMaxHealth
import io.github.natanfudge.hardcraft.health.getCurrentBlockHealth
import io.github.natanfudge.hardcraft.health.getMaxBlockHealth
import io.github.natanfudge.hardcraft.health.getMaxHealth
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameter
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.roundToInt

object ItemDrop {
    fun onItemDrop(stack: ItemStack, context: LootContext) {
        val origin = context.origin() ?: return
        val world = context.world ?: return
        val destroyedBlock = context[LootContextParameters.BLOCK_STATE]
        // If there's no origin we can't determine if the block is damaged (this shouldn't happen for regular cases)
        if (destroyedBlock != null) {
            val blockPos = origin.toBlockPos()
            val blockCurrentHealth = world.getCurrentBlockHealth(blockPos) ?: return
            val blockMaxHealth = destroyedBlock.block.getMaxHealth() ?: return
            val stackMaxHealth = stack.getBlockMaxHealth() ?: return

            val healthFraction = blockCurrentHealth.toFloat() / blockMaxHealth
            // Set the health of the stack to be the same percent as the block that was broken
            // (the broken block might not equal the dropped block)
            val newStackHealth = (stackMaxHealth * healthFraction).roundToInt()

            stack.setBlockCurrentHealth(newStackHealth)

            stack.damage = stackMaxHealth - newStackHealth
        }
    }
}


fun Vec3d.toBlockPos() = BlockPos.ofFloored(this)
fun LootContext.origin(): Vec3d? = get(LootContextParameters.ORIGIN)
fun LootContext.isBlockDrop(): Boolean = hasParameter(LootContextParameters.BLOCK_STATE)