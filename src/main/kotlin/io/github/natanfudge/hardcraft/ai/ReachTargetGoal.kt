package io.github.natanfudge.hardcraft.ai

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
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt


/**
 * Reaches the target by employing the following tactics:
 * 1. Breaking blocks blocking the way
 * 2. Blocking-up to reach heights
 * 3. Bridging to get across gaps
 * Makes the mob break blocks blocking his movement
 */
class ReachTargetGoal(private val mob: HostileEntity) : Goal() {
    private val world = mob.world as ServerWorld
    private val breakThrottler = TickThrottler()


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
        //TODO: this is just a debug measure
        val player = getClient().player ?: return
        if (mob.pos.distanceTo(player.pos) > 30) return


//        if (this !in DoneMobs) {
//            blockUp.blockUp()
//            DoneMobs.add(this)
//            return
//        }

        // Only perform special actions to get the target if there's no other way
        if (mob.target != null && mob.hardcraft_getCantReachTarget()) {
            if (blockUp.tick()) return

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
    fun blockUp() {
        jumpStartY = mob.pos.y
        jumpStartTick = world.time
        mob.jump()
    }

    /**
     * Returns true if the mob should not do anything else because it is blocking up
     */
    fun tick(): Boolean {
        val player = getClient().player ?: return false
        if (mob.pos.distanceTo(player.pos) > 30) return false

        // Reset jump attempt if enough time has passed
        if (jumpStartTick != null && jumpStartY != null && jumpStartTick!! + 30 < world.time) {
            jumpStartY = null
            jumpStartTick = null
        }

        val targetIsAbove = mob.isBelowTarget() && jumpStartY == null
        if (targetIsAbove && mob.isOnGround) {
            // If the target is too high, block up to him
            blockUp()
        }
        if (jumpStartY != null && mob.pos.y >= jumpStartY!! + 0.9) {
            // Once we reached enough height, place the block
            val pos = mob.pos.toBlockPos().down()
            val below = mob.pos.minusY(2.0).blocksAround()
//            if (below.none { world.canSupportOtherBlocks(it) }) {
//                println("No block underneath can support. Vector: ${mob.pos.minusY(2.0)}. Blocks: $below")
//            }
            // Make sure there is something to place on
            if (below.any { world.canSupportOtherBlocks(it) } && !world.canSupportOtherBlocks(pos)) {
//                println("Setting block at $pos")
                world.setBlock(pos, Blocks.DIRT)
                mob.swingHand(mob.activeHand)
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
