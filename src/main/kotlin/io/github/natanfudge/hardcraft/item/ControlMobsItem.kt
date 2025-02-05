package io.github.natanfudge.hardcraft.item

import io.github.natanfudge.genericutils.inServer
import io.github.natanfudge.genericutils.superclasses.KItem
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

val DoneMobs = mutableSetOf<Goal>()

//var MobCommandActive = false

object ControlMobsItem: KItem("control_mobs") {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?>? {
        world.inServer {
            DoneMobs.clear()
//            println("Hand: $hand")
//            println("World: ${world.isClient}")
//            MobCommandActive = !MobCommandActive
//            println("Active: $MobCommandActive")
        }
        return TypedActionResult.success(user.getStackInHand(hand))
//        return super.use(world, user, hand)
    }
}