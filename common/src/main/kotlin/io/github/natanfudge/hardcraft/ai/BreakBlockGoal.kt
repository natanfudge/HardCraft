package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.utils.directionTo
import io.github.natanfudge.hardcraft.utils.plus
import io.github.natanfudge.hardcraft.utils.roundUp
import io.github.natanfudge.hardcraft.utils.valuesBetween
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.min

private class Accelerometer(private val mob: MobEntity) {
    // Tracks ticks so we can tell when 5 ticks have passed
    private var ticks = 0

    // Keeps track of the positions of the mob in the last 20 ticks, checking every 5 secs.
    private var positionMemory = arrayOfNulls<Vec3d>(4)

    var moving = true

    fun tick() {
        ticks++
        if (ticks >= 20) {
            ticks = 0
        }
        if (ticks % 5 == 0) {
            val slot = ticks / 5
            val previousPos = positionMemory[slot]
            if (previousPos != null) {
                // TODO: experiment how this can be abused to prevent zombies from attacking
                // I think the solution is to severely limit the ability to push things.
                moving = mob.pos.distanceTo(previousPos) >= 0.1
            }
            positionMemory[slot] = mob.pos
        }
    }
}


class BreakBlockGoal(private val mob: MobEntity) : Goal() {
    //    private var targetBlockPos: BlockPos? = null
    private val world = mob.world as ServerWorld
    private val accelerometer = Accelerometer(mob)
//    fun test(): Boolean {
//
//    }

    override fun canStart(): Boolean {
        return true
        // Mob can go through things - no need to break stuff
//        println("Collision: ${mob.horizontalCollision}")
//        if (!mob.horizontalCollision) return false
//        println("path finished: ${mob.navigation.currentPath?.isFinished}")
//        val path = mob.navigation.currentPath ?: return false
//        if (path.isFinished) return false
////        println("Checking nextLogical")
//        this.targetBlockPos = getNextLogicalBlockToBreak(path.currentNodePos)
//        return this.targetBlockPos != null
//        for (i in 0 until (path.currentNodeIndex + 2).coerceAtMost(path.length)) {
//            val pathPos = path.getNode(i).toBlockPos()
//            if (mob.distanceTo(pathPos) <= 5) {
//                val toBreak = getBlockToBreak(pathPos, mob.height)
//                if (toBreak != null) {
//                    this.targetBlockPos = toBreak
//                    return true
//                }
//            }
//        }
//        return false
    }


    // Try to target any block that does not allow the mob to pass with its height
    private fun getNextLogicalBlockToBreak(nextPathPos: BlockPos): BlockPos? {
//        println("Mob pos ${mob.pos}, path pos: $nextPathPos")
        //TODO: create a 'clear path' from mob pos to path pos
        // Go through every pos between the mob position and the target position and see if there is anything blocking

        //TODO: tunneling (straight down)
        val direction = mob.pos.directionTo(nextPathPos)
        val xValues = valuesBetween(0, direction.x)
        val yValues = valuesBetween(direction.y, (mob.height.roundUp() - 1))
        val zValues = valuesBetween(0, direction.z)
//        val blocksInDirection = mutableListOf<BlockPos>()
        val blocksInDirection = xValues.flatMap { x ->
            //TODO: optimize to not create a billion lists
            yValues.flatMap { y  ->
                zValues.map { z ->
                    BlockPos(mob.blockX + x, mob.blockY + y, mob.blockZ + z)
                }
            }
        }
        return blocksInDirection
            .filter { !world.isAir(it) }
            .minByOrNull { mob.distanceTo(it) }
//        val nextBlock = mob.blockPos + direction
//        println("mob pos: ${mob.blockPos}, next pos: $nextBlock")
//        if (!world.isAir(nextBlock)) return nextBlock
//        else return null

//        return getBlocksBetween(mob.blockPos, nextPathPos, mob.height.roundUp())
//            .filter { mob.distanceTo(it) <= 5 && !world.isAir(it) }
//            .minByOrNull { mob.distanceTo(it) }

        //        val mobPos = mob.pos
//        //TODO: break diagonal blocks
//        for (i in 0..mob.height.toInt()) {
//            val currentPos = nextPos.up(i)
//            if (!world.isAir(currentPos)) return currentPos
//        }
//        return null
    }

    private fun getBlocksBetween(start: BlockPos, end: BlockPos, height: Int): List<BlockPos> {
        return buildList {
            val xRange = min(start.x, end.x)..max(start.x, end.x)
            // Having a higher height increasing the size of the 'box' created, allowing higher mobs to pass through.
            val yRange = min(start.y, end.y)..max(start.y, end.y) + (height - 1)
            val zRange = min(start.z, end.z)..max(start.z, end.z)
            for (x in xRange) {
                for (y in yRange) {
                    for (z in zRange) {
                        add(BlockPos(x, y, z))
                    }
                }
            }
        }
    }

    override fun shouldContinue(): Boolean {
        return true
//        return !world.isAir(targetBlockPos) && mob.distanceTo(targetBlockPos!!) <= 5 && !mob.navigation.isIdle
    }


    override fun shouldRunEveryTick(): Boolean {
        //TODO: try returning false and ticking the accelerometer twice at tick() because it runs every other tick when this is false
        return true
    }

    override fun tick() {
//        accelerometer.tick()
        accelerometer.tick()
        if (!accelerometer.moving) {
            val path = mob.navigation.currentPath ?: return
            if (path.isFinished) return
            val targetBlockPos = getNextLogicalBlockToBreak(path.currentNodePos) ?: return
            if (!mob.handSwinging) {
                mob.swingHand(mob.activeHand)
            }
            //TODO: to prevent sending a million packets, damage blocks in batches
            world.damageBlock(targetBlockPos, 2)
        }

    }
}
