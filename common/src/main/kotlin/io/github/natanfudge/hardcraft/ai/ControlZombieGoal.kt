package io.github.natanfudge.hardcraft.ai

import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos

class ControlZombieGoal(private val mob: MobEntity) : Goal() {
    private var activeTarget: BlockPos? = null

    companion object {
        var _targetPos: BlockPos? = null

        @JvmStatic
        fun setTargetPos(pos: BlockPos) {
            _targetPos = pos
        }
    }

    override fun canStart(): Boolean {
        return this.activeTarget != _targetPos
    }

    override fun shouldContinue(): Boolean {
        return !this.mob.navigation.isIdle
    }

    override fun start() {
        this.activeTarget = _targetPos
        val pos = this.activeTarget!!
        println("Starting movement")
        mob.navigation.startMovingTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1.0)
    }

    override fun stop() {
        this.mob.navigation.stop()
        super.stop()
    }

    override fun shouldRunEveryTick(): Boolean {
        return true
    }
}