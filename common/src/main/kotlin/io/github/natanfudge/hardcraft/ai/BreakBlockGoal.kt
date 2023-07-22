package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.health.damageBlock
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class BreakBlockGoal(private val mob: MobEntity) : Goal() {
    private val world = mob.world as ServerWorld

    companion object {
        var _targetPos: BlockPos? = null

        @JvmStatic
        fun setTargetPos(pos: BlockPos) {
            _targetPos = pos
        }
    }

    override fun canStart(): Boolean {
        return _targetPos != null
    }

    override fun shouldContinue(): Boolean {
        return !this.mob.navigation.isIdle
    }

    override fun start() {
        val pos = _targetPos!!
        this.mob.navigation.startMovingTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1.0)
    }

    override fun stop() {
        this.mob.navigation.stop()
        super.stop()
    }

    override fun shouldRunEveryTick(): Boolean {
        //TODO: need to see how much this can be minimized. I think 'path find to enemy' and 'break block' should be seperate goals.
        return true
    }

    override fun tick() {

        // TODO: maybe add some sound to block breaking
        if (!mob.handSwinging) {
            mob.swingHand(mob.activeHand)
        }
        val targetPos = _targetPos!!
        if (mob.distanceTo(targetPos) < 10) {
            world.damageBlock(targetPos, 1)
        }
    }
}