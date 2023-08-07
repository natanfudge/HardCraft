package io.github.natanfudge.hardcraft.mixinhandler

import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.BowAttackGoal
import net.minecraft.entity.ai.goal.CrossbowAttackGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.ProjectileAttackGoal
import net.minecraft.entity.mob.MobEntity

object RangedAttackGoals {
    @JvmStatic
    fun getRangedAttackGoalActor(goal: Goal): MobEntity?  = when(goal) {
        is BowAttackGoal<*> -> goal.actor as MobEntity
        is CrossbowAttackGoal<*> -> goal.actor as MobEntity
        is ProjectileAttackGoal -> goal.owner as MobEntity
        else -> null
    }
    @JvmStatic
    fun getRangedAttackGoalSpeed(goal: Goal): Double  = when(goal) {
        is BowAttackGoal<*> -> goal.speed
        is CrossbowAttackGoal<*> -> goal.speed
        is ProjectileAttackGoal -> goal.mobSpeed
        else -> error("Unexpected non-ranged goal $goal")
    }
}