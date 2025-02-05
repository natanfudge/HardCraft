package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.MinecraftConstants
import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.health.isDestroyable
import io.github.natanfudge.hardcraft.mixinhandler.demolition
import io.github.natanfudge.hardcraft.mixinhandler.toBlockPos
import io.github.natanfudge.hardcraft.utils.*
import net.minecraft.block.Blocks
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * As long as [tick] is called every tick, tells you whether [mob] is moving with the [moving] property.
 */
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

/**
 * Reaches the target by employing the following tactics:
 * 1. Breaking blocks blocking the way
 * 2. Blocking-up to reach heights
 * 3. Bridging to get across gaps
 * Makes the mob break blocks blocking his movement
 */
class ReachTargetGoal(private val mob: HostileEntity) : Goal() {
    private val world = mob.world as ServerWorld
    private val accelerometer = Accelerometer(mob)
    private val breakThrottler = TickThrottler()

    /**
     * Track how long the mob has been moving, so we can know if it needs to break blocks
     */
    private var nonIdleTicks = 0
    override fun canStart(): Boolean {
        return true
    }


    /**
     *  Find any block that does not allow the mob to pass with its height
     *  This method only takes into account the immediately adjacent blocks
     */
    private fun getNextLogicalBlockToBreak(nextPathPos: BlockPos): BlockPos? {
        val direction = mob.pos.directionTo(nextPathPos)
        val xValues = valuesBetween(0, direction.x)
//        val yValues = valuesBetween(direction.y, (mob.height.roundUp() - 1))
        val yValues = valuesBetween(0, mob.height.roundUp() - 1)
        val zValues = valuesBetween(0, direction.z)
        val blocksInDirection = cartesianProduct(xValues, yValues, zValues) { x, y, z ->
            BlockPos(mob.blockX + x, mob.blockY + y, mob.blockZ + z)
        }
        val allTargets = if (mob.isBelowTarget()) {
            // Try straight up as well, in case a ceiling is preventing this mob from blocking-up
            blocksInDirection + BlockPos(mob.blockX, mob.blockY + mob.height.roundUp(), mob.blockZ)
        } else blocksInDirection

//        for (block in blocksInDirection) {
//            DebugRendering.tint(world, block, McColor.Red.withAlpha(128))
//        }
        return allTargets
            .filter { world.isDestroyable(it) }
            .minByOrNull { mob.distanceTo(it) }
    }

    private val blockUp = BlockUp(mob, world)


    override fun tick() {
        accelerometer.tick()
        if (blockUp.tick()) return
        val player = getClient().player ?: return
        if (mob.pos.distanceTo(player.pos) > 30) return


//        if (this !in DoneMobs) {
//            blockUp.blockUp()
//            DoneMobs.add(this)
//            return
//        }


        // When the mob has just started moving obviously the accelerometer will say he has not moved and the mob will break blocks randomly.
        // we should only consider the mob not moving when he has not been idle for some time, which signifies he's being blocked.
        if (mobIsNotIdle() && !accelerometer.moving) {
//            mob.setJumping(true)

            //TODO: restore
            // damageBlock() and getNextLogicalBlockToBreak() are expensive so we don't do it every tick,
            // rather do it batches by multiplying damage by DoDamageToBlockInterval.
            breakThrottler.runThrottled(DoDamageToBlockInterval) { delta ->
                val path = mob.navigation.currentPath ?: return
                if (path.isFinished) return
                val targetBlockPos = getNextLogicalBlockToBreak(path.currentNodePos) ?: return
                if (!mob.handSwinging) {
                    mob.swingHand(mob.activeHand)
                }
                world.damageBlock(targetBlockPos, delta * mob.demolition)
            }
        }
    }

    /**
     * Checks if the mob has not been idle for full second
     * Must be called every tick to work.
     */
    private fun mobIsNotIdle(): Boolean {
        nonIdleTicks = if (mob.navigation.isIdle) 0 else nonIdleTicks + 1
        return nonIdleTicks >= MinecraftConstants.TicksPerSecond
    }

    override fun shouldContinue(): Boolean {
        return true
    }


    override fun shouldRunEveryTick(): Boolean {
        return true
    }
}

/**
 * Allows the mob to 'block up' - jump and then place a block.
 * [tick] must be called every tick.
 */
class BlockUp(private val mob: HostileEntity, private val world: World) {
    // Technically this should be stored in NBT but not storing it is fine, just jump again.
    private var jumpStartY: Double? = null

    private var jumpStartTick: Long? = null
    //TODO: make it so this only happens when the mob can't find any other way to get to you, so it won't just
    // spam blocks randomly
    fun blockUp() {
        jumpStartY = mob.pos.y
        jumpStartTick = world.time
        mob.jump()
    }
    //TODO: I need to make the mobs break straight up if their head is getting blocked from jumping

    //TODO: IDK why it won't enter the jump ending condition, and set the block.
    /**
     * Returns true if the mob should not do anything else because it is blocking up
     */
    fun tick(): Boolean {
        val player = getClient().player ?: return false
        if (mob.pos.distanceTo(player.pos) > 30) return false

        // Reset jump attempt if enough time has passed
        if (jumpStartTick != null && jumpStartY != null && jumpStartTick!! + 30 < world.time) {
//            println("Resetting timeout")
            jumpStartY = null
            jumpStartTick = null
        }

        val targetIsAbove = mob.isBelowTarget() && jumpStartY == null
        if (targetIsAbove && mob.isOnGround) {
//            println("Jumping from y = ${mob.y}")
            // If the target is too high, block up to him
            blockUp()
        }
//        println("StartY: $jumpStartY, Mob y: ${mob.y}")
        if (jumpStartY != null && mob.pos.y >= jumpStartY!! + 0.9) {
            // Once we reached enough height, place the block
            val pos = mob.pos.toBlockPos().down()
            val below = mob.pos.minusY(2.0).blocksAround()
            if (below.none { world.canSupportOtherBlocks(it) }) {
                println("No block underneath can support. Vector: ${mob.pos.minusY(2.0)}. Blocks: $below")
            }
            if (world.canSupportOtherBlocks(pos)) {
//                println("Block is already taken at $pos")
            }
            // Make sure there is something to place on
            if (below.any { world.canSupportOtherBlocks(it) } && !world.canSupportOtherBlocks(pos)) {
                println("Setting block at $pos")
                world.setBlock(pos, Blocks.DIRT)
                mob.swingHand(mob.activeHand)
            } else {
//                println("Not setting")
            }
            jumpStartY = null
        }
        if (targetIsAbove) {
            mob.navigation.stop()
        }
        return targetIsAbove
    }
}


private fun HostileEntity.isBelowTarget(): Boolean {
    return target != null && y + 1 < target!!.y
}

/**
 * Returns all 4 blocks closest to the specified exact position.
 */
fun Vec3d.blocksAround(): List<BlockPos> {
    val y = floor(y).roundToInt()
    val xUp = ceil(x).roundToInt()
    val xDown = floor(x).roundToInt()
    val zUp = ceil(z).roundToInt()
    val zDown = floor(z).roundToInt()
    return listOf(
        BlockPos(xUp, y, zUp),
        BlockPos(xUp, y, zDown),
        BlockPos(xDown, y, zUp),
        BlockPos(xDown, y, zDown),
    )
}

private const val DoDamageToBlockInterval = 5
