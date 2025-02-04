package io.github.natanfudge.hardcraft.test

import io.github.natanfudge.hardcraft.support.VirtualBlock
import io.github.natanfudge.hardcraft.support.VirtualWorld
import io.github.natanfudge.hardcraft.support.WorldSupportCache
import net.minecraft.util.math.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue



class WorldSupportCacheFloatingBlocksTest {

    @Test
    fun `removing block with no connected blocks returns empty list`() {
        // Single block above bedrock, no need to include removed block
        val blocks = mapOf(
            BlockPos(0, 1, 0) to VirtualBlock.STONE,
            BlockPos(0, 0, 0) to VirtualBlock.BEDROCK
        )
        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)

        // Establish the initial structure
        cache.findPathToBedrock(BlockPos(0, 1, 0), emptySet())

        // Check removal of unconnected block at (5,1,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(5, 1, 0))
        assertTrue(floatingBlocks.isEmpty())
    }

    @Test
    fun `removing supported block leaves structure floating`() {
        // World state after middle block (0,1,0) was removed
        val blocks = buildMap {
            put(BlockPos(0, 2, 0), VirtualBlock.STONE) // Top block
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK) // Bottom bedrock
        }

        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)

        // Establish initial structure
        cache.findPathToBedrock(BlockPos(0, 2, 0), emptySet())

        // Check effect of removing (0,1,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(0, 1, 0))

        // Top block should be floating
        assertEquals(listOf(BlockPos(0, 2, 0)), floatingBlocks)
    }

    @Test
    fun `removing base block makes structure float`() {
        // World state after base block (0,1,0) was removed
        val blocks = buildMap {
            put(BlockPos(0, 2, 0), VirtualBlock.STONE) // Top
            put(BlockPos(1, 1, 0), VirtualBlock.STONE) // Horizontal extension
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK) // Bottom
        }

        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)

        // Establish initial paths
        cache.findPathToBedrock(BlockPos(0, 2, 0), emptySet())
        cache.findPathToBedrock(BlockPos(1, 1, 0), emptySet())

        // Check effect of removing (0,1,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(0, 1, 0))

        // Both top and horizontal blocks should be floating
        assertEquals(
            setOf(BlockPos(0, 2, 0), BlockPos(1, 1, 0)),
            floatingBlocks.toSet()
        )
    }

    @Test
    fun `structure with alternative support path remains stable old`() {
        // World state after middle-left block (0,1,0) was removed
        val blocks = buildMap {
            put(BlockPos(0, 2, 0), VirtualBlock.STONE) // Top left
            put(BlockPos(2, 2, 0), VirtualBlock.STONE) // Top right
            put(BlockPos(1, 1, 0), VirtualBlock.STONE) // Middle center
            put(BlockPos(2, 1, 0), VirtualBlock.STONE) // Middle right
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK) // Bottom left
            put(BlockPos(2, 0, 0), VirtualBlock.BEDROCK) // Bottom right
        }

        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)



        // Check effect of removing (0,1,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(0, 1, 0))

        // Nothing should float as there's still a path to bedrock through the right side
        assertTrue(floatingBlocks.isNotEmpty())
    }
    @Test
    fun `structure with alternative support path remains stable`() {
        // World state after middle-left block (0,1,0) was removed
        val blocks = buildMap {
            put(BlockPos(0, 2, 0), VirtualBlock.STONE)
            put(BlockPos(2, 2, 0), VirtualBlock.STONE)
            put(BlockPos(1, 1, 0), VirtualBlock.STONE)
            put(BlockPos(2, 1, 0), VirtualBlock.STONE)
            put(BlockPos(1, 2, 0), VirtualBlock.STONE)
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK)
            put(BlockPos(2, 0, 0), VirtualBlock.BEDROCK)
        }

        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)



        // Check effect of removing (0,1,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(0, 1, 0))

        // Nothing should float as there's still a path to bedrock through the right side
        assertTrue(floatingBlocks.isEmpty())
    }

    @Test
    fun `complex structure partial collapse`() {
        // World state after middle trunk block (0,2,0) was removed
        val blocks = buildMap {
            // Main trunk without the removed block
            put(BlockPos(0, 3, 0), VirtualBlock.STONE)
            put(BlockPos(0, 1, 0), VirtualBlock.STONE)
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK)

            // Left branch
            put(BlockPos(-1, 2, 0), VirtualBlock.STONE)
            put(BlockPos(-2, 2, 0), VirtualBlock.STONE)

            // Right branch (with separate bedrock support)
            put(BlockPos(1, 2, 0), VirtualBlock.STONE)
            put(BlockPos(2, 2, 0), VirtualBlock.STONE)
            put(BlockPos(2, 1, 0), VirtualBlock.STONE)
            put(BlockPos(2, 0, 0), VirtualBlock.BEDROCK)
        }

        val world = VirtualWorld(blocks)
        val cache = WorldSupportCache(world)

        // Establish all paths
        for (x in -2..2) {
            for (y in 1..3) {
                val pos = BlockPos(x, y, 0)
                if (blocks.containsKey(pos)) {
                    cache.findPathToBedrock(pos, emptySet())
                }
            }
        }

        // Check effect of removing (0,2,0)
        val floatingBlocks = cache.getFloatingBlocksAfterRemovalAt(BlockPos(0, 2, 0))

        // Left branch and top block should float, right branch should remain stable
        assertEquals(
            setOf(
                BlockPos(-1, 2, 0),
                BlockPos(-2, 2, 0),
                BlockPos(0, 3, 0)
            ),
            floatingBlocks.toSet()
        )
    }
}