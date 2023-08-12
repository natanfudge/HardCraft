package io.github.natanfudge.hardcraft.ai

import io.github.natanfudge.hardcraft.utils.valuesBetween
import net.minecraft.entity.ai.pathing.*
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkCache
import kotlin.properties.Delegates

class HardCraftNavigation(entity: MobEntity, world: World?, existingNavigation: EntityNavigation) : MobNavigation(entity, world) {
    private var navigator: HardCraftPathNodeNavigator? = null
    private var range: Int? = null

    init {
        navigator?.wrappedNavigator = existingNavigation.createPathNodeNavigator(range!!)
    }

    override fun createPathNodeNavigator(range: Int): PathNodeNavigator {
        super.createPathNodeNavigator(range)
        // This method gets called too early for us to use outside value, so we assign the wrapped navigator ourself in init{}.
        this.navigator = HardCraftPathNodeNavigator(range)
        this.range = range
        return navigator!!
    }

    override fun getPos(): Vec3d {
        return entity.pos
    }

    override fun isAtValidPosition(): Boolean {
        // This may be incorrect, maybe I should use existingNavigation.isAtValidPosition()
        return true
    }
}

// These constructor parameters are meaningless
class HardCraftPathNodeNavigator(range: Int) : PathNodeNavigator(LandPathNodeMaker(), range) {
    // This is assigned outside because of Minecraft weirdness
    var wrappedNavigator: PathNodeNavigator? = null
    override fun findPathToAny(
        world: ChunkCache,
        mob: MobEntity,
        positions: MutableSet<BlockPos>,
        followRange: Float,
        distance: Int,
        rangeMultiplier: Float
    ): Path? {
        val normalPath = wrappedNavigator?.findPathToAny(world, mob, positions, followRange, distance, rangeMultiplier)
        if (normalPath != null && normalPath.reachesTarget()) return normalPath
        // If normal means don't suffice, break right through
        if (positions.isEmpty()) return null
        return Path(
            positions.map { PathNode(it.x, it.y, it.z) }, positions.last(), true
        )
//        return Path(shortestLine(mob.blockPos, pos), pos, true)
    }


//    //*
//    // \
//    //  \
//    //   *
//    private fun shortestLine(start: BlockPos, end: BlockPos): List<PathNode> {
//        val xValues = valuesBetween(start.x, end.x)
//        val zValues = valuesBetween(start.z, end.z)
//        // First, go diagonally from start to end
//        val diagonalStart = xValues.zip(zValues).map { (x, z) ->
//            PathNode(x, start.y, z)
//        }
//
//        val straightEnd = when {
//            // If the line is exactly diagonal then we can end
//            start.x - end.x == start.z - end.z -> listOf()
//            // If there's more z to go, go along the z axis
//            start.x - end.x < start.z - end.z -> {
//                valuesBetween(diagonalStart.last().z + 1, end.z)
//                    .map { PathNode(end.x, start.y, it) }
//            }
//
//            else -> {
//                // If there's more y to go, go along the y axis.
//                valuesBetween(diagonalStart.last().x + 1, end.x)
//                    .map { PathNode(it, start.y, end.z) }
//            }
//        }
//
//        return diagonalStart + straightEnd
//    }
//
//
//    private fun tunnelDown(start: BlockPos, end: BlockPos): List<PathNode> {
//        val xValues = valuesBetween(start.x, end.x)
//        val yValues = valuesBetween(start.y, end.y)
//        val zValues = valuesBetween(start.z, end.z)
//        // First, go diagonally from start to end
//        val diagonalStart = xValues.zip(zValues).map { (x, z) ->
//            PathNode(x, start.y, z)
//        }
//
//        val straightEnd = when {
//            // If the line is exactly diagonal then we can end
//            start.x - end.x == start.z - end.z -> listOf()
//            // If there's more z to go, go along the z axis
//            start.x - end.x < start.z - end.z -> {
//                valuesBetween(diagonalStart.last().z + 1, end.z)
//                    .map { PathNode(end.x, start.y, it) }
//            }
//
//            else -> {
//                // If there's more y to go, go along the y axis.
//                valuesBetween(diagonalStart.last().x + 1, end.x)
//                    .map { PathNode(it, start.y, end.z) }
//            }
//        }
//
//        return diagonalStart + straightEnd
//    }
//
//    private fun tunnel(start: BlockPos, end: BlockPos): List<PathNode> {
//    }

    private fun breakFoundation(start: BlockPos, end: BlockPos): List<PathNode> {
        TODO("When foundation/support mechanics are implemented, implement this as well.")
    }
}

