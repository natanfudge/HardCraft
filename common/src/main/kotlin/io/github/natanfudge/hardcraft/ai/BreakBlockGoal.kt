package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.MinecraftConstants
import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.health.isDestroyable
import io.github.natanfudge.hardcraft.mixinhandler.demolition
import io.github.natanfudge.hardcraft.utils.TickThrottler
import io.github.natanfudge.hardcraft.utils.directionTo
import io.github.natanfudge.hardcraft.utils.roundUp
import io.github.natanfudge.hardcraft.utils.valuesBetween
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

private class Accelerometer(private val mob: MobEntity) {
    // Tracks ticks so we can tell when 5 ticks have passed
    private var ticks = 0

    // Keeps track of the positions of the mob in the last 20 ticks, checking every 5 secs.
    private var positionMemory = arrayOfNulls<Vec3d>(4)

    var moving = true

    fun tick() {
        ticks++
        if (ticks >= MinecraftConstants.TicksPerSecond) {
            ticks = 0
        }
        if (ticks % 5 == 0) {
            val slot = ticks / 5
            // TODO: experiment how this can be abused to prevent zombies from attacking
            // I think the solution is to severely limit the ability to push things.
            moving = positionMemory.all { it == null || mob.pos.distanceTo(it) >= 0.1 }
            positionMemory[slot] = mob.pos
        }
    }
}


class BreakBlockGoal(private val mob: HostileEntity) : Goal() {
    private val world = mob.world as ServerWorld
    private val accelerometer = Accelerometer(mob)
    private val breakThrottler = TickThrottler()

    /**
     * Track how long the mob has been moving so we can know if it needs to break blocks
     */
    private var nonIdleTicks = 0
    override fun canStart(): Boolean {
        return true
    }


    // Try to target any block that does not allow the mob to pass with its height
    private fun getNextLogicalBlockToBreak(nextPathPos: BlockPos): BlockPos? {
        val direction = mob.pos.directionTo(nextPathPos)
        val xValues = valuesBetween(0, direction.x)
        val yValues = valuesBetween(direction.y, (mob.height.roundUp() - 1))
        val zValues = valuesBetween(0, direction.z)
        val blocksInDirection = buildList {
            for (x in xValues) {
                for (y in yValues) {
                    for (z in zValues) {
                        add(BlockPos(mob.blockX + x, mob.blockY + y, mob.blockZ + z))
                    }
                }
            }
        }
        return blocksInDirection
            .filter { world.isDestroyable(it) }
            .minByOrNull { mob.distanceTo(it) }
    }

    override fun shouldContinue(): Boolean {
        return true
    }


    override fun shouldRunEveryTick(): Boolean {
        return true
    }

    override fun tick() {
        accelerometer.tick()
        nonIdleTicks = if (mob.navigation.isIdle) 0 else nonIdleTicks + 1
        if (nonIdleTicks >= MinecraftConstants.TicksPerSecond && !accelerometer.moving) {
            breakThrottler.runThrottled(DoDamageToBlockInterval) {
                val path = mob.navigation.currentPath ?: return
                if (path.isFinished) return
                val targetBlockPos = getNextLogicalBlockToBreak(path.currentNodePos) ?: return
                if (!mob.handSwinging) {
                    mob.swingHand(mob.activeHand)
                }
                world.damageBlock(targetBlockPos, DoDamageToBlockInterval * mob.demolition)
            }
        }
    }
}
private const val DoDamageToBlockInterval = 5
