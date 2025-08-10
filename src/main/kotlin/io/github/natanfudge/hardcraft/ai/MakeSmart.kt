package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.hardcraft.mixinhandler.MobHooks.hateVillagers
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.passive.VillagerEntity

/**
 * Make mobs more threatening by allowing them to break blocks that block their way
 */
fun HostileEntity.makeSmart() {
    if (world != null && !world.isClient) {
        this.goalSelector.add(0, ReachTargetGoal(this))
        if (hateVillagers) {
            // It's often useful for testing to make mobs attack villagers, so we have a flag that makes them attack villagers.
            this.goalSelector.add(3, ActiveTargetGoal<VillagerEntity?>(this, VillagerEntity::class.java, true))
        }
    }
    this.navigation = HardCraftNavigation(this, world, this.navigation)
}