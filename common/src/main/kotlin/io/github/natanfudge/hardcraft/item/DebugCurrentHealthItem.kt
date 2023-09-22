package io.github.natanfudge.hardcraft.item

import io.github.natanfudge.genericutils.inServer
import io.github.natanfudge.genericutils.isServer
import io.github.natanfudge.genericutils.superclasses.KItem
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.health.getMaxBlockHealth
import io.github.natanfudge.hardcraft.health.repairBlock
import io.github.natanfudge.hardcraft.item.DebugCurrentHealthItem.Mode.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.roundToInt

class DebugCurrentHealthItem private constructor(
    /**
     * True to make it damage blocks, false to make it heal blocks
     */
    val damage: Boolean
) : KItem(if (damage) "debug_damage_block" else "debug_repair_block", group = HardcraftItemGroup) {

    enum class Mode {
        /**
         * Change by 100
         */
        Constant,

        /**
         * Change by 50%
         */
        Half,

        /**
         * Change by 100%
         */
        All
    }

    companion object {
        val Damage = DebugCurrentHealthItem(true)
        val Repair = DebugCurrentHealthItem(false)

        // It's just debug. I can do this sus global variable shit.
        var mode = Constant
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.player?.isSneaking == true) return ActionResult.PASS
        context.world.inServer {
            val maxHealth = getMaxBlockHealth(context.blockPos) ?: return ActionResult.PASS
            val amount = when (mode) {
                Constant -> 100
                Half -> (maxHealth * 0.5).roundToInt()
                All -> maxHealth
            }
            if (damage) {
                damageBlock(context.blockPos, amount)
            } else {
                repairBlock(context.blockPos, amount)
            }
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user.isSneaking) {
            if (world.isServer) {
                mode = when (mode) {
                    Constant -> Half
                    Half -> All
                    All -> Constant
                }
                val messageSuffix = when (mode) {
                    Constant -> "100"
                    Half -> "50%"
                    All -> "All"
                }
                val messagePrefix = if (damage) "Damage" else "Repair"
                user.sendMessage(Text.literal("$messagePrefix $messageSuffix"))
            }
            return TypedActionResult.consume(user.getStackInHand(hand))
        }
        return super.use(world, user, hand)
    }
}