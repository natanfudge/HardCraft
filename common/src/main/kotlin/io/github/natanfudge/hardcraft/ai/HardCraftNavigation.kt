package io.github.natanfudge.hardcraft.ai

import net.minecraft.entity.ai.pathing.*
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkCache

/**
 * HardCraft's replacement for vanilla's navigation for hostile mobs.
 * This navigation makes mob go in a straight line to their target when no other option exists.
 * When the mob will get inevitably blocked by blocks in that case, [BreakBlockGoal] exists to make him to try to break it and eventually reach his target.
 */
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
private class HardCraftPathNodeNavigator(range: Int) : PathNodeNavigator(LandPathNodeMaker(), range) {
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
        // First, try doing it the vanilla way
        val normalPath = wrappedNavigator?.findPathToAny(world, mob, positions, followRange, distance, rangeMultiplier)
        if (normalPath != null && normalPath.reachesTarget()) return normalPath

        // positions shouldn't be empty but check anyway
        if (positions.isEmpty()) return null
        // If normal means don't suffice, break right through.
        // Specifying simply the destination position will make the mob go in a straight line to the target.
        return Path(
            positions.map { PathNode(it.x, it.y, it.z) }, positions.last(), true
        )
    }


    private fun breakFoundation(start: BlockPos, end: BlockPos): List<PathNode> {
        TODO("When foundation/support mechanics are implemented, implement this as well.")
    }
}

