package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.client.debug.DebugRendering
import io.github.natanfudge.hardcraft.client.debug.TextHandle
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.health.isDestroyable
import io.github.natanfudge.hardcraft.mixinhandler.demolition
import io.github.natanfudge.hardcraft.mixinhandler.floorToBlockPos
import io.github.natanfudge.hardcraft.mixinhandler.roundToBlockPos
import io.github.natanfudge.hardcraft.utils.*
import net.minecraft.block.Blocks
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.Path
import net.minecraft.entity.ai.pathing.PathNode
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

var debugAI = false


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

    private val debugOnlyWorkOnCloseMobs = false


    /**
     *  Find any block that does not allow the mob to pass with its height
     *  This method only takes into account the immediately adjacent blocks
     */
    private fun getNextLogicalBlockToBreak(nextPathPos: Vec3d): BlockPos? {
        val direction = mob.pos.directionTo(nextPathPos)
        val xValues = valuesBetween(0, direction.x.roundToInt())
        val yValues = valuesBetween(0, mob.height.roundUp() - 1)
        val zValues = valuesBetween(0, direction.z.roundToInt())
        val blocksInDirection = cartesianProduct(xValues, yValues, zValues) { x, y, z ->
            BlockPos(mob.blockX + x, mob.blockY + y, mob.blockZ + z)
        }.toMutableList()
        if (mob.isBelowTarget()) {
            // Try straight up as well, in case a ceiling is preventing this mob from blocking-up
            blocksInDirection.add(BlockPos(mob.blockX, mob.blockY + mob.height.roundUp(), mob.blockZ))
        }
        if (mob.isAboveTarget()) {
            // Try digging straight down
            blocksInDirection.add(BlockPos(mob.blockX, mob.blockY - 1, mob.blockZ))
        }

//        for (block in blocksInDirection) {
//            DebugRendering.tint(world, block, McColor.Red.withAlpha(128))
//        }
        return blocksInDirection
            .filter { world.isDestroyable(it) }
            .minByOrNull { mob.distanceTo(it) }
    }

    private val blockUp = BlockUp(mob, world, this)

    //    private var state: HardcraftAIState = HardcraftAIState.Unassigned
    private var textHandle: TextHandle? = null
    private fun setStateDebugText(state: HardcraftAIState) {
        val text = when (state) {
            HardcraftAIState.BlockingUp -> "Blocking Up"
            HardcraftAIState.BreakingForward -> "Breaking Forward"
            HardcraftAIState.Bridging -> "Bridging"
            HardcraftAIState.NoTarget -> "No Target"
            HardcraftAIState.PathingNormally -> "Pathing"
            HardcraftAIState.Unassigned -> "Unassigned"
            HardcraftAIState.BreakingUp -> "Breaking Up"
            HardcraftAIState.Falling -> "Falling"
        }
        setDebugText(text)
    }

    fun setDebugText(text: String) {
        if (debugAI) {
            if (textHandle != null) {
                DebugRendering.removeText(world, textHandle!!)
            }
            textHandle = DebugRendering.addText(world, mob.pos.plusY(1.0), text, time = 1.seconds)
        }
    }

    private fun getAiState(): HardcraftAIState {
        if (mob.target == null) return HardcraftAIState.NoTarget
        if (!mob.hardcraft_getCantReachTarget()) return HardcraftAIState.PathingNormally
        val below = mob.isBelowTarget()
        if (below && spaceExistsToBlockUp()) return HardcraftAIState.BlockingUp
        if (cannotMakeNextStepWithoutFalling()) return HardcraftAIState.Bridging
        if (below) return HardcraftAIState.BreakingUp
        else return HardcraftAIState.BreakingForward
    }

    private fun spaceExistsToBlockUp(): Boolean {
        return overlappingBlockPositions(mob.pos.plusY(mob.height.toDouble() + 1)).all { world.getBlockState(it).isAir }
    }


    /**
     * Returns the position of the next block the mob is going to step on
     */
    private fun getNextStepPos(): BlockPos? {
        val path = mob.navigation.currentPath ?: return null
        val pathTargetPos = path.safeGetCurrentNode()?.pos ?: return null
        return (mob.pos.directionTo(pathTargetPos).withoutY() + mob.pos).minusY(1.0).roundToBlockPos()
    }

    /**
     * getCurrentNode can IOOB for some reason so we wrap it in a try/catch
     */
    private fun Path.safeGetCurrentNode(): PathNode? = try {
        currentNode
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    private fun cannotMakeNextStepWithoutFalling(): Boolean {
        val pos = getNextStepPos() ?: return false
        return world.getBlockState(pos).isAir
    }

    override fun tick() {
        val state = getAiState()
        setStateDebugText(state)


        if (debugOnlyWorkOnCloseMobs) {
            val player = getClient().player ?: return
            if (mob.pos.distanceTo(player.pos) > 30) return
        }


        when (state) {
            HardcraftAIState.BlockingUp -> {
                blockUp.tick()
            }

            HardcraftAIState.BreakingForward, HardcraftAIState.BreakingUp -> {
                // damageBlock() and getNextLogicalBlockToBreak() are expensive so we don't do it every tick,
                // rather do it batches by multiplying damage by DoDamageToBlockInterval.
                breakThrottler.runThrottled(DoDamageToBlockInterval) { delta ->
                    val path = mob.navigation.currentPath ?: run {
                        setDebugText("No Path")
                        return
                    }
                    if (path.isFinished) {
                        setDebugText("Path is finished")
                        return
                    }
                    val pathTargetPos = path.safeGetCurrentNode()?.pos ?: return
                    val targetBlockPos = getNextLogicalBlockToBreak(pathTargetPos) ?: return
                    if (!mob.handSwinging) {
                        mob.swingHand(mob.activeHand)
                    }
                    if (debugAI) {
//                        DebugRendering.tintBlock(world, targetBlockPos, McColor.Red)
                    }
                    world.damageBlock(targetBlockPos, delta * mob.demolition)
                }
            }

            HardcraftAIState.Bridging -> {
                val nextStepPos = getNextStepPos() ?: return
                world.setBlock(nextStepPos, Blocks.DIRT)
                mob.swingHand(mob.activeHand)
            }

            else -> {}
        }
    }

    override fun shouldContinue(): Boolean {
        return true
    }


    override fun shouldRunEveryTick(): Boolean {
        return true
    }
}

sealed interface HardcraftAIState {
    object Unassigned : HardcraftAIState
    object NoTarget : HardcraftAIState
    object PathingNormally : HardcraftAIState
    object BlockingUp : HardcraftAIState
    object Bridging : HardcraftAIState
    object BreakingForward : HardcraftAIState
    object BreakingUp : HardcraftAIState
    object Falling : HardcraftAIState
}

/**
 * Allows the mob to 'block up' - jump and then place a block.
 * [tick] must be called every tick.
 */
class BlockUp(private val mob: HostileEntity, private val world: World, private val goal: ReachTargetGoal) {
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
        // Reset jump attempt if enough time has passed
        if (jumpStartTick != null && jumpStartY != null && jumpStartTick!! + 30 < world.time) {
            jumpStartY = null
            jumpStartTick = null
        }

        val targetIsAbove = mob.isBelowTarget()
        if (targetIsAbove && mob.isOnGround) {
            // If the target is too high, block up to him
            blockUp()
            goal.setDebugText("Jumping Up")
        }
        if (jumpStartY != null && mob.pos.y >= jumpStartY!! + 0.9) {
            // Once we reached enough height, place the block
            val pos = mob.pos.floorToBlockPos().down()
            val below = overlappingBlockPositions(mob.pos.minusY(2.0))
            // Make sure there is something to place on
            if (below.any { world.canSupportOtherBlocks(it) } && !world.canSupportOtherBlocks(pos)) {
                world.setBlock(pos, Blocks.DIRT)
                mob.swingHand(mob.activeHand)
                goal.setDebugText("Placing Block")
            } else {
                goal.setDebugText("Falling")
            }
            jumpStartY = null
        } else {
            goal.setDebugText("Going up after jump, startY=$jumpStartY, currentY = ${mob.pos.y}")
        }
        if (targetIsAbove) {
            mob.navigation.stop()
        }
        return targetIsAbove
    }
}


private fun HostileEntity.isBelowTarget(): Boolean {
    return target != null && y + height < target!!.y
}

private fun HostileEntity.isAboveTarget(): Boolean {
    return target != null && y > target!!.y + target!!.height
}


private const val DoDamageToBlockInterval = 5

/**
 * Gets the up to 4 block positions that intersect with the 1x1 area around [pos]
 */
private fun overlappingBlockPositions(pos: Vec3d): List<BlockPos> {
    val y = floor(pos.y).toInt()

    val x0 = floor(pos.x - 0.5).toInt()
    val x1 = floor(pos.x + 0.5).toInt()
    val z0 = floor(pos.z - 0.5).toInt()
    val z1 = floor(pos.z + 0.5).toInt()

    return when {
        x0 == x1 && z0 == z1 ->                   // inside a single block
            listOf(BlockPos(x0, y, z0))

        x0 == x1 ->                               // spans two blocks along Z
            listOf(
                BlockPos(x0, y, z0),
                BlockPos(x0, y, z1)
            )

        z0 == z1 ->                               // spans two blocks along X
            listOf(
                BlockPos(x0, y, z0),
                BlockPos(x1, y, z0)
            )

        else ->                                   // spans four blocks
            listOf(
                BlockPos(x0, y, z0),
                BlockPos(x1, y, z0),
                BlockPos(x0, y, z1),
                BlockPos(x1, y, z1)
            )
    }
}
