package io.github.natanfudge.hardcraft.test

import io.github.natanfudge.hardcraft.support.BEDROCK_ROOT
import io.github.natanfudge.hardcraft.support.VirtualBlock
import io.github.natanfudge.hardcraft.support.VirtualWorld
import io.github.natanfudge.hardcraft.support.WorldSupportCache
import io.github.natanfudge.hardcraft.support.WorldSupportCache.BedrockSearchResult
import net.minecraft.util.math.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorldSupportCacheTest {
    private lateinit var cache: WorldSupportCache

    @Test
    fun `test direct path to bedrock`() {
        // Create a simple vertical path to bedrock
        val path = listOf(
            BlockPos(0, 5, 0),
            BlockPos(0, 4, 0),
            BlockPos(0, 3, 0),
            BlockPos(0, 2, 0),
            BlockPos(0, 1, 0),
            BlockPos(0, 0, 0)
        )

        val blocks = buildMap {
            path.forEach { pos ->
                put(pos, if (pos.y == 0) VirtualBlock.BEDROCK else VirtualBlock.STONE)
            }
        }

        val world = VirtualWorld(blocks)
        cache = WorldSupportCache(world)

        val start = BlockPos(0, 5, 0)
        val result = cache.findPathToBedrock(start, emptySet())

        assertTrue(result is BedrockSearchResult.FoundFooting)
        assertEquals(BEDROCK_ROOT, (result as BedrockSearchResult.FoundFooting).treeNode)
        // Verify path length (should include all blocks except bedrock)
        assertEquals(path.size - 1, result.pathToTree.size)
    }

    @Test
    fun `test floating blocks`() {
        val floatingStructure = listOf(
            BlockPos(0, 10, 0),
            BlockPos(1, 10, 0),
            BlockPos(0, 10, 1),
            BlockPos(1, 10, 1)
        )

        val blocks = buildMap {
            floatingStructure.forEach { pos ->
                put(pos, VirtualBlock.STONE)
            }
        }

        val world = VirtualWorld(blocks)
        cache = WorldSupportCache(world)

        val start = BlockPos(0, 10, 0)
        val result = cache.findPathToBedrock(start, emptySet())

        assertTrue(result is BedrockSearchResult.IsFloating)
        assertEquals(
            floatingStructure.toSet(),
            (result as BedrockSearchResult.IsFloating).floatingConnectedComponent.toSet()
        )
    }

    @Test
    fun `test path through existing tree node`() {
        // Create a structure where we first establish a path to bedrock
        // Then try to find a path from a block that can connect to this established path
        val blocks = buildMap {
            // Base structure (will be established first)
            put(BlockPos(0, 3, 0), VirtualBlock.STONE)
            put(BlockPos(0, 2, 0), VirtualBlock.STONE)
            put(BlockPos(0, 1, 0), VirtualBlock.STONE)
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK)

            // Connection path
            put(BlockPos(0, 5, 0), VirtualBlock.STONE)
            put(BlockPos(0, 4, 0), VirtualBlock.STONE)
        }

        val world = VirtualWorld(blocks)
        cache = WorldSupportCache(world)

        // First establish the base structure in the tree
        cache.findPathToBedrock(BlockPos(0, 3, 0), emptySet())

        // Now try to find path from the higher block
        val start = BlockPos(0, 5, 0)
        val result = cache.findPathToBedrock(start, emptySet())

        assertTrue(result is BedrockSearchResult.FoundFooting)
        assertTrue((result as BedrockSearchResult.FoundFooting).pathToTree.isNotEmpty())
    }

    @Test
    fun `test start block is bedrock`() {
        val start = BlockPos(0, 0, 0)
        val blocks = mapOf(start to VirtualBlock.BEDROCK)

        val world = VirtualWorld(blocks)
        cache = WorldSupportCache(world)

        val result = cache.findPathToBedrock(start, emptySet())
        assertTrue(result is BedrockSearchResult.IsBedrock)
    }

    @Test
    fun `test floating`() {
        val blocks = buildMap {
            // Main structure that will become orphaned
            put(BlockPos(0, 3, 0), VirtualBlock.STONE)
            put(BlockPos(0, 2, 0), VirtualBlock.STONE)
            put(BlockPos(0, 1, 0), VirtualBlock.STONE)
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK)
        }

        val world = VirtualWorld(blocks)
        cache = WorldSupportCache(world)


        val start = BlockPos(0, 5, 0)
        val result = cache.findPathToBedrock(start, setOf(BlockPos(0, 3, 0), BlockPos(0, 4, 0)))

        assertIs<BedrockSearchResult.IsFloating>(result)
    }

    @Test
    fun `test complex structure with multiple paths`() {
        // Create a structure with multiple possible paths to bedrock
        val structure = buildMap {
            // Main vertical path
            put(BlockPos(0, 5, 0), VirtualBlock.STONE)
            put(BlockPos(0, 4, 0), VirtualBlock.STONE)
            put(BlockPos(0, 3, 0), VirtualBlock.STONE)
            put(BlockPos(0, 2, 0), VirtualBlock.STONE)
            put(BlockPos(0, 1, 0), VirtualBlock.STONE)
            put(BlockPos(0, 0, 0), VirtualBlock.BEDROCK)

            // Branch path
            put(BlockPos(1, 3, 0), VirtualBlock.STONE)
            put(BlockPos(2, 3, 0), VirtualBlock.STONE)
            put(BlockPos(2, 2, 0), VirtualBlock.STONE)
            put(BlockPos(2, 1, 0), VirtualBlock.STONE)
            put(BlockPos(2, 0, 0), VirtualBlock.BEDROCK)
        }

        val world = VirtualWorld(structure)
        cache = WorldSupportCache(world)

        // First find path for the branch to establish it
        cache.findPathToBedrock(BlockPos(2, 3, 0), emptySet())

        // Then try to find path from the top of the main path
        val start = BlockPos(0, 5, 0)
        val result = cache.findPathToBedrock(start, emptySet())

        assertTrue(result is BedrockSearchResult.FoundFooting)
        // We know a path exists since we can reach bedrock
        assertTrue((result as BedrockSearchResult.FoundFooting).pathToTree.isNotEmpty())
    }
}