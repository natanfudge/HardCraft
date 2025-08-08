package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.distanceTo
import io.github.natanfudge.hardcraft.client.McColor
import io.github.natanfudge.hardcraft.client.debug.DebugRendering
import io.github.natanfudge.hardcraft.client.debug.TextHandle
import io.github.natanfudge.hardcraft.health.damageBlock
import io.github.natanfudge.hardcraft.health.isDestroyable
import io.github.natanfudge.hardcraft.mixinhandler.demolition
import io.github.natanfudge.hardcraft.mixinhandler.floorToBlockPos
import io.github.natanfudge.hardcraft.utils.*
import net.minecraft.block.Blocks
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.Path
import net.minecraft.entity.ai.pathing.PathNode
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.nextTowards
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

    //TODO: make tints disappear the same way texts do

    private val debugOnlyWorkOnCloseMobs = false

    //TODO: also getting next block placement is not working well either, zombies still fall sometimes.

    /**
     *  Find any block that does not allow the mob to pass with its height
     *  This method only takes into account the immediately adjacent blocks
     */
    private fun getNextLogicalBlockToBreak(nextPathPos: Vec3d): BlockPos? {
//        val direction = mob.pos.directionTo(nextPathPos)
//        val xValues = valuesBetween(0, direction.x.roundToInt())
//        val yValues = valuesBetween(0, mob.height.roundUp() - 1)
//        val zValues = valuesBetween(0, direction.z.roundToInt())
//        val blocksInDirection = cartesianProduct(xValues, yValues, zValues) { x, y, z ->
//            BlockPos(mob.blockX + x, mob.blockY + y, mob.blockZ + z)
//        }.toMutableSet()
//        if (mob.isBelowTarget()) {
//            // Try straight up as well, in case a ceiling is preventing this mob from blocking-up
//            blocksInDirection.add(BlockPos(mob.blockX, mob.blockY + mob.height.roundUp(), mob.blockZ))
//        }
//        if (mob.isAboveTarget()) {
//            // Try digging straight down
//            blocksInDirection.add(BlockPos(mob.blockX, mob.blockY - 1, mob.blockZ))
//        }
//
//        // Sometimes wide mobs like spiders get stuck, this gives them another option to break blocks that are stopping them
//        blocksInDirection.addAll(overlappingBlockPositions(mob.pos))

        val blocking = blocksBlocking(nextPathPos)

//        for (block in blocksInDirection) {
//            DebugRendering.tint(world, block, McColor.Red.withAlpha(128))
//        }
        return blocking
            .filter { world.isDestroyable(it) }
            .minByOrNull { mob.distanceTo(it) }
    }

    /**
     * Given a mob with a certain position mob.position and bounding box mob.boundingBox,
     * and a target position target, return the list of block positions that come between the mob and the target position,
     * and that are directly adjacent to the mob.
     */
    private fun blocksBlocking(target: Vec3d): List<BlockPos> {
        val bb = mob.boundingBox
        val start = mob.pos
        val dir = target.subtract(start)
        val lenSq = dir.lengthSquared()
        if (lenSq == 0.0) return emptyList()

        // Which directions are we going?
        val stepX = dir.x.compareTo(0.0)  // -1, 0, or 1
        val stepY = dir.y.compareTo(0.0)
        val stepZ = dir.z.compareTo(0.0)

        // Blocks overlapped by the mob's AABB
        val minX = floor(bb.minX).toInt()
        val maxX = floor(nextDown(bb.maxX)).toInt()
        val minY = floor(bb.minY).toInt()
        val maxY = floor(nextDown(bb.maxY)).toInt()
        val minZ = floor(bb.minZ).toInt()
        val maxZ = floor(nextDown(bb.maxZ)).toInt()

        // Neighbor layer coordinates (the first layer "outside" the box on each axis)
        val faceX = if (stepX > 0) maxX + 1 else if (stepX < 0) minX - 1 else null
        val faceY = if (stepY > 0) maxY + 1 else if (stepY < 0) minY - 1 else null
        val faceZ = if (stepZ > 0) maxZ + 1 else if (stepZ < 0) minZ - 1 else null

        // Collect adjacent blocks from the faces that are actually towards the target
        val candidates = LinkedHashSet<BlockPos>()

        // X-facing layer
        faceX?.let { fx ->
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    candidates.add(BlockPos(fx, y, z))
                }
            }
        }
        // Y-facing layer
        faceY?.let { fy ->
            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    candidates.add(BlockPos(x, fy, z))
                }
            }
        }
        // Z-facing layer
        faceZ?.let { fz ->
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    candidates.add(BlockPos(x, y, fz))
                }
            }
        }

        if (candidates.isEmpty()) return emptyList()

        // Keep only blocks that are actually "between": in front of the mob (0<=t<=1)
        // using the projection t = dot((blockCenter - start), dir) / |dir|^2
        val filtered = candidates.mapNotNull { bp ->
            val center = Vec3d(bp.x + 0.5, bp.y + 0.5, bp.z + 0.5)
            val t = center.subtract(start).dotProduct(dir) / lenSq
            if (t in 0.0..1.0) bp to t else null
        }

        // Sort by progression along the ray (nearest first)
        return filtered.sortedBy { it.second }.map { it.first }
    }

    /**
     * Like Math.nextDown but for doubles on older mappings; you can also use java.lang.Math.nextDown
     * if available. This prevents treating exact integer-aligned maxX/maxY/maxZ as spilling into the
     * next block above.
     */
    private fun nextDown(x: Double): Double {
        return x.nextTowards(Double.NEGATIVE_INFINITY)
    }

//    private val blockUp = BlockUp(mob, world, this)

    //    private var state: HardcraftAIState = HardcraftAIState.Unassigned
    private var textHandle: TextHandle? = null
    private fun setStateDebugText(state: HardcraftAIState) {
        val text = when (state) {
//            HardcraftAIState.BlockingUp -> "Blocking Up"
            HardcraftAIState.BreakingForward -> "Breaking Forward"
            HardcraftAIState.Bridging -> "Bridging"
            HardcraftAIState.NoTarget -> "No Target"
            HardcraftAIState.PathingNormally -> "Pathing"
            HardcraftAIState.Unassigned -> "Unassigned"
            HardcraftAIState.BreakingUp -> "Breaking Up"
            HardcraftAIState.Falling -> "Falling"
            is HardcraftAIState.JumpingToPlaceBlockBelow -> "Jumping to Place"
            HardcraftAIState.PlacingBlockBelow -> "Placing Below"
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
        // After we jump up, we don't want to forget we want to place a block
        if (prevState is HardcraftAIState.JumpingToPlaceBlockBelow) {
            val (startY, startTick) = prevState as HardcraftAIState.JumpingToPlaceBlockBelow

            if (world.time < startTick + 30) {
                if (mob.pos.y >= startY + 0.9) {
                    // Got high enough - place block
                    return HardcraftAIState.PlacingBlockBelow
                } else {
                    // Not high enough - wait
                    return prevState
                }
            }
            // Else - too much time has passed and we will try to do something else
        }
        if (mob.target == null) return HardcraftAIState.NoTarget
        if (!mob.hardcraft_getCantReachTarget()) return HardcraftAIState.PathingNormally
        val below = mob.isBelowTarget()
        if (below && spaceExistsToBlockUp() && mob.isOnGround) {
            return HardcraftAIState.JumpingToPlaceBlockBelow(mob.pos.y, world.time)
        }
        // If on the same level, start bridging
        if (mob.pos.y.floorToInt() == mob.target!!.pos.y.roundToInt() && cannotMakeNextStepWithoutFalling()) return HardcraftAIState.Bridging
        if (below) return HardcraftAIState.BreakingUp
        else return HardcraftAIState.BreakingForward
    }

    private fun spaceExistsToBlockUp(): Boolean {
        return overlappingBlockPositions(mob.pos.plusY(mob.height.toDouble() + 1)).all { world.getBlockState(it).isAir }
    }


    /**
     * Returns the position of the next block the mob is going to step on
     */
    private val DIAGONAL_SLOPE_TOL = 0.2   // 0.0 = only perfect 45°, 0.2 = ±~11°
    private val CORNER_T_TOL = 0.02        // tX and tZ within 2%
    private val EDGE_TOL = 0.20            // within 0.20 of a grid line counts as "on the edge"

    private fun getNextStepPos(): BlockPos? {
        val path = mob.navigation.currentPath ?: return null
        val target = path.safeGetCurrentNode()?.pos ?: return null

        val start = mob.pos
        val dx = target.x - start.x
        val dz = target.z - start.z
        if (dx == 0.0 && dz == 0.0) return null

        val cx = floor(start.x).toInt()
        val cz = floor(start.z).toInt()
        val y = mob.blockPos.y - 1 // keep your original Y choice

        // --- edge check: return current cell if sufficiently centered ---
        val fracX = start.x - floor(start.x)      // [0,1)
        val fracZ = start.z - floor(start.z)      // [0,1)
        val nearEdgeX = fracX < EDGE_TOL || fracX > 1.0 - EDGE_TOL
        val nearEdgeZ = fracZ < EDGE_TOL || fracZ > 1.0 - EDGE_TOL
        if (!nearEdgeX && !nearEdgeZ) {
            return BlockPos(cx, y, cz)
        }
        // ---------------------------------------------------------------

        val stepX = when {
            dx > 0.0 -> 1
            dx < 0.0 -> -1
            else -> 0
        }
        val stepZ = when {
            dz > 0.0 -> 1
            dz < 0.0 -> -1
            else -> 0
        }
        if (stepX == 0 && stepZ == 0) return BlockPos(cx, y, cz)

        // Next grid lines in the direction of travel
        val nextGridX = if (stepX > 0) cx + 1.0 else cx.toDouble()
        val nextGridZ = if (stepZ > 0) cz + 1.0 else cz.toDouble()

        // Parametric t to those lines along the ray start + t*(dx, dz)
        val tX = if (stepX != 0) (nextGridX - start.x) / dx else Double.POSITIVE_INFINITY
        val tZ = if (stepZ != 0) (nextGridZ - start.z) / dz else Double.POSITIVE_INFINITY

        // Prefer diagonal if heading is roughly 45° or the boundary times are nearly equal
        if (stepX != 0 && stepZ != 0) {
            val adx = abs(dx)
            val adz = abs(dz)
            val maxd = maxOf(adx, adz)
            val slopeClose = maxd > 0 && abs(adx - adz) <= DIAGONAL_SLOPE_TOL * maxd
            val tClose = (tX.isFinite() && tZ.isFinite() &&
                    abs(tX - tZ) <= CORNER_T_TOL * maxOf(tX, tZ))
            if (slopeClose || tClose) {
                return BlockPos(cx + stepX, y, cz + stepZ)
            }
        }

        return when {
            tX < tZ -> BlockPos(cx + stepX, y, cz)
            tZ < tX -> BlockPos(cx, y, cz + stepZ)
            else -> { // exact corner: deterministic tie-breaker
                if (abs(dx) >= abs(dz)) BlockPos(cx + stepX, y, cz)
                else BlockPos(cx, y, cz + stepZ)
            }
        }
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

    private var prevState: HardcraftAIState = HardcraftAIState.Unassigned

    override fun tick() {
//        println("Pos:" + MinecraftClient.getInstance().player!!.pos + " BlockPos: " + MinecraftClient.getInstance().player!!.blockPos)
        val state = getAiState()
        this.prevState = state
        setStateDebugText(state)


        if (debugOnlyWorkOnCloseMobs) {
            val player = getClient().player ?: return
            if (mob.pos.distanceTo(player.pos) > 30) return
        }


        when (state) {
//            HardcraftAIState.BlockingUp -> {
//                blockUp.tick()
//            }

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
                        if(previouslyTargetedBlock != null && previouslyTargetedBlock != targetBlockPos) {
                            DebugRendering.untintBlock(world, previouslyTargetedBlock!!)
                        }
                        DebugRendering.tintBlock(world, targetBlockPos, McColor.Red, 0.3.seconds)
                        this.previouslyTargetedBlock = targetBlockPos
                    }
                    world.damageBlock(targetBlockPos, delta * mob.demolition)
                    println("Dealt ${delta * mob.demolition} damage at $targetBlockPos")
                }
            }

            HardcraftAIState.Bridging -> {
                val nextStepPos = getNextStepPos() ?: return
                world.setBlock(nextStepPos, Blocks.DIRT)
                mob.swingHand(mob.activeHand)
            }

            is HardcraftAIState.JumpingToPlaceBlockBelow -> {
                if (mob.isOnGround) {
                    mob.jump()
                }
                mob.navigation.stop()
                setDebugText("Jumping, startY=${state.startY}, currentY=${mob.pos.y}")
            }

            HardcraftAIState.PlacingBlockBelow -> {
                val pos = mob.pos.floorToBlockPos().down()
                val below = overlappingBlockPositions(mob.pos.minusY(2.0))
                // Make sure there is something to place on
                if (below.any { world.canSupportOtherBlocks(it) } && !world.canSupportOtherBlocks(pos)) {
                    world.setBlock(pos, Blocks.DIRT)
                    mob.swingHand(mob.activeHand)
                }
            }


            else -> {}
        }
    }

    private var previouslyTargetedBlock : BlockPos? = null

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

    //    object BlockingUp : HardcraftAIState
    object Bridging : HardcraftAIState
    object BreakingForward : HardcraftAIState
    object BreakingUp : HardcraftAIState
    object Falling : HardcraftAIState
    data class JumpingToPlaceBlockBelow(val startY: Double, val startTick: Long) : HardcraftAIState
    object PlacingBlockBelow : HardcraftAIState
}

///**
// * Allows the mob to 'block up' - jump and then place a block.
// * [tick] must be called every tick.
// */
//class BlockUp(private val mob: HostileEntity, private val world: World, private val goal: ReachTargetGoal) {
//    // Technically this should be stored in NBT but not storing it is fine, just jump again.
//    private var jumpStartY: Double? = null
//
//    private var jumpStartTick: Long? = null
//    fun blockUp() {
//        jumpStartY = mob.pos.y
//        jumpStartTick = world.time
//        mob.jump()
//    }
//
//    /**
//     * Returns true if the mob should not do anything else because it is blocking up
//     */
//    fun tick(): Boolean {
//        // Reset jump attempt if enough time has passed
//        if (jumpStartTick != null && jumpStartY != null && jumpStartTick!! + 30 < world.time) {
//            jumpStartY = null
//            jumpStartTick = null
//        }
//
//        val targetIsAbove = mob.isBelowTarget()
//        if (targetIsAbove && mob.isOnGround) {
//            // If the target is too high, block up to him
//            blockUp()
//            goal.setDebugText("Jumping Up")
//        }
//        if (jumpStartY != null && mob.pos.y >= jumpStartY!! + 0.9) {
//            // Once we reached enough height, place the block
//
//        } else {
//            goal.setDebugText("Going up after jump, startY=$jumpStartY, currentY = ${mob.pos.y}")
//        }
//        if (targetIsAbove) {
//            mob.navigation.stop()
//        }
//        return targetIsAbove
//    }
//}


private fun HostileEntity.isBelowTarget(): Boolean {
    return target != null && y  < target!!.y
}

private fun HostileEntity.isAboveTarget(): Boolean {
    return target != null && y > target!!.y
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
