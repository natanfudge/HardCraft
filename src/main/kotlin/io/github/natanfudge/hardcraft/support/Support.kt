@file:Suppress("NOTHING_TO_INLINE")

package io.github.natanfudge.hardcraft.support

import io.github.natanfudge.hardcraft.HardCraft
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//TODO: next step: drop them blocks down!


object Support {
    private val worldCaches = mutableMapOf<World, WorldSupportCache>()

    //    @Synchronized
    fun floatingBlocksAfterRemoval(world: World, pos: BlockPos): List<BlockPos> {
        return worldCaches.computeIfAbsent(world) { WorldSupportCache(RealWorld(world)) }
            .getFloatingBlocksAfterRemovalAt(pos)
    }

}

// I believe no other block should conflict with this

/**
 * See Support.md
 */
class WorldSupportCache(private val world: IWorld) {
    /**
     * Makes floating checks faster
     */
    private val tree = IndexedTree(BEDROCK_ROOT)

    fun getFloatingBlocksAfterRemovalAt(toRemove: BlockPos): List<BlockPos> {
//        HardCraft.Logger.warn ("Tree before: ${tree.toPrettyString()}")
        val compactToRemove = toRemove.compact()
        if (compactToRemove == BEDROCK_ROOT) {
            HardCraft.Logger.error("Unexpected attempt to remove the pos entry that is reserved for the root")
            return listOf()
        }
        // If there is no existing entry for toRemove, then nothing is being 'orphaned'
        val orphans = tree.descendantsOf(compactToRemove)?.map { it.toBlockPos() }?.toSet() ?: setOf()
        val floating = mutableSetOf<BlockPos>()

        tree.remove(compactToRemove)

        forEachNeighbor(toRemove) { neighbor ->
            val compactNeighbor = neighbor.compact()
            if (tree.contains(compactNeighbor)) {
                // If the neighbor has no parent its the root and we don't care
                val neighborParent = tree.parentOf(compactNeighbor) ?: return@forEachNeighbor
                if (neighborParent != compactToRemove) {
                    // If we know of a path for this neighbor that doesn't include toRemove, we can safely
                    // say this neighbor is not floating
                    return@forEachNeighbor
                }
            }

            if (floating.contains(neighbor)) {
                // We already found out that this neighbor and its connected component is floating
                return@forEachNeighbor
            }

            // Don't care about air
            if (!world.canSupportOtherBlocks(neighbor)) return@forEachNeighbor
            // No information about this neighbor, OR the neighbor has been orphaned now so we need to find it new footing

            when (val path = findPathToBedrock(neighbor, orphans)) {
                is BedrockSearchResult.FoundFooting -> {
                    attachPathToTree(path)
                }

                BedrockSearchResult.IsBedrock -> {
                    // Do nothing
                }

                is BedrockSearchResult.IsFloating -> {
                    floating.addAll(path.floatingConnectedComponent)
                }
            }
        }

        for (floatingBlock in floating) {
            tree.remove(floatingBlock.compact())
        }
//        HardCraft.Logger.warn("Tree After: ${tree.toPrettyString()}")

        return floating.toList()
    }

    private fun attachPathToTree(footing: BedrockSearchResult.FoundFooting) {
        tree.addChain(footing.treeNode, footing.pathToTree.asReversed().map { it.compact() })
    }


    sealed interface BedrockSearchResult {
        class FoundFooting(val pathToTree: List<BlockPos>, val treeNode: CompactBlockPos) : BedrockSearchResult
        class IsFloating(val floatingConnectedComponent: List<BlockPos>) : BedrockSearchResult
        object IsBedrock : BedrockSearchResult
    }

    /**
     * Does a sort of DFS that goes downwards as much as possible to find bedrock quickly.
     * If a path is found, will a path from [start] to bedrock.
     * If no path is found, will return the connected component of [start] - all blocks that [start] is connected to.
     */
    fun findPathToBedrock(start: BlockPos, orphanedBlocks: Set<BlockPos>): BedrockSearchResult {
        // Deal with the edge case of starting in bedrock
        if (world.getBlock(start).isWorldBottom()) return BedrockSearchResult.IsBedrock

        val visited = mutableSetOf<BlockPos>(start)
        val path = Stack<BlockPos>()
        path.push(start)
        loop@ while (path.isNotEmpty()) {
            val current = path.peek()
            forEachNeighbor(current) { neighbor ->
                // Need to limit the distance so this won't loop infinitely
                // Don't care about paths we tried already
                if (neighbor !in visited) {
                    // We've reached an element in the tree, we will end the path with it
                    // Orphaned blocks don't count! They might not be connected to bedrock themselves.
                    if (tree.contains(neighbor.compact()) && !orphanedBlocks.contains(neighbor)) {
                        println("Found tree footing after ${visited.size} steps")
                        return BedrockSearchResult.FoundFooting(path, neighbor.compact())
                    }
                    val block = world.getBlock(neighbor)
                    if (block.isWorldBottom()) {
                        // We've reached bedrock, we will end with BEDROCK_ROOT
                        println("Found bedrock footing after ${visited.size} steps")
                        return BedrockSearchResult.FoundFooting(path, BEDROCK_ROOT)
                    }
//                    if (path.size >= MaxPathLength) {
//                        val x = 2
//                    }
                    // We're not gonna try searching paths that are too long
                    if (block.canSupportOtherBlocks() && path.size < MaxPathLength) {
//                        if (block.isWorldBottom()) {
//
//                        } else if(){
                        path.push(neighbor)
                        visited.add(neighbor)
                        // Important - go further down, don't try the other directions if we managed to advance here
                        continue@loop
//                        }
                    } // Don't care about air

                }
            }
            // Nothing worked, pull out one step back
            path.pop()
        }
        println("Found floating after ${visited.size} steps")
        return BedrockSearchResult.IsFloating(visited.toList())
    }

//    private fun areFarAway(start: BlockPos, end: BlockPos): Boolean {
//        // Don't need to check y because it's limited anyway
//        return abs(end.x - start.x) > MaxSearchDistance || abs(end.z - start.z) > MaxSearchDistance
//    }

    /**
     * Goes over every neighbor, ordered down-north-south-east-west-up
     */
    @OptIn(ExperimentalContracts::class)
    private inline fun forEachNeighbor(pos: BlockPos, action: (BlockPos) -> Unit) {
        contract {
            callsInPlace(action, InvocationKind.AT_LEAST_ONCE)
        }
        // Intentionally try 'down' first
        action(pos.down())
        action(pos.north())
        action(pos.south())
        action(pos.east())
        action(pos.west())
        // Intentionally try 'up' last
        action(pos.up())
    }

}

private const val MaxSearchDistance = 128
private const val MaxPathLength = 1024


