package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.health.damageBlock
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class ControlZombieGoal(private val mob: MobEntity) : Goal() {
    private val world = mob.world as ServerWorld

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
//        return _targetPos != null
    }

    override fun shouldContinue(): Boolean {
        return !this.mob.navigation.isIdle
    }
//TODO: figure out why zombies can't go over on block in a straight line.
    override fun start() {
        this.activeTarget = _targetPos
        val pos = this.activeTarget!!
        println("Starting movement")
        this.mob.navigation.startMovingTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1.0)
//        GlobalScope.launch {
//            delay(20_000)
//            mob.navigation.stop()
//        }
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
//        if (!mob.handSwinging) {
//            mob.swingHand(mob.activeHand)
//        }
//        val targetPos = _targetPos!!
//        if (mob.distanceTo(targetPos) < 5) {
//            //TODO: to prevent sending a million packets, damage blocks in batches
//            world.damageBlock(targetPos, 1)
//        }
    }
}