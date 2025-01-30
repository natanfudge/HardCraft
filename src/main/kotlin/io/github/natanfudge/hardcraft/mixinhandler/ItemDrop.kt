package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.setBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.getBlockMaxHealth
import io.github.natanfudge.hardcraft.health.getCurrentBlockHealth
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object ItemDrop {
    fun onItemDrop(stack: ItemStack, context: LootContext) {
        val origin = context.origin() ?: return
        val world = context.world ?: return
        // If there's no origin we can't determine if the block is damaged (this shouldn't happen for regular cases)
        if (context.isBlockDrop()) {
            val blockPos = origin.toBlockPos()
            val health = world.getCurrentBlockHealth(blockPos) ?: return
            val maxHealth = stack.getBlockMaxHealth() ?: return
            // Sometimes when blocks are broken they become different blocks with less max health, so we don't need to damage them in that case.
            if (health < maxHealth) {
                stack.setBlockCurrentHealth(health)

                stack.damage = maxHealth - health
            }
        }
    }
}


fun Vec3d.toBlockPos() = BlockPos.ofFloored(this)
fun LootContext.origin(): Vec3d? = get(LootContextParameters.ORIGIN)
fun LootContext.isBlockDrop(): Boolean = hasParameter(LootContextParameters.BLOCK_STATE)