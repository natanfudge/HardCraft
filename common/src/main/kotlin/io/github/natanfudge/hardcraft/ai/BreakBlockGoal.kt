package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.genericutils.toBlockPos
import io.github.natanfudge.hardcraft.health.damageBlock
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class BreakBlockGoal(private val mob: MobEntity) : Goal() {
    private lateinit var targetBlockPos: BlockPos
    private val world = mob.world as ServerWorld

    override fun canStart(): Boolean {
        // Mob can go through - no need to break blocks
        if (!mob.horizontalCollision) return false
        val path = mob.navigation.currentPath ?: return false
        if (path.isFinished) return false
        for (i in 0 until (path.currentNodeIndex + 2).coerceAtMost(path.length)) {
            val pathPos = path.getNode(i).toBlockPos()
            if (mob.distanceTo(pathPos) <= 2.25) {
                val toBreak = getBlockToBreak(pathPos, mob.height)
                if (toBreak != null) {
                    this.targetBlockPos = toBreak
                    return true
                }
            }
        }
        return false
    }

    // Try to target any block that does not allow the mob to pass with its height
    private fun getBlockToBreak(pathPos: BlockPos, mobHeight: Float): BlockPos? {
        for (i in 0..mobHeight.toInt()) {
            val currentPos = pathPos.up(i)
            if (!world.isAir(currentPos)) return currentPos
        }
        return null
    }

    override fun shouldContinue(): Boolean {
        return !world.isAir(targetBlockPos)
    }


    override fun shouldRunEveryTick(): Boolean {
        return true
    }

    override fun tick() {
        if (!mob.handSwinging) {
            mob.swingHand(mob.activeHand)
        }
        //TODO: to prevent sending a million packets, damage blocks in batches
        world.damageBlock(targetBlockPos, 2)
    }
}
