package io.github.natanfudge.hardcraft.support

import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.utils.aggregate
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

private typealias T = CompactBlockPos

/**
 * Tree implementation that allows O(1) operations by using a hashmap.
 * Currently only implemented for CompactBlockPos values for efficiency.
 * With valhalla, CompactBlockPos could be replaced by <T>.
 */
class IndexedTree(rootValue: T) {
    private val root = TreeNode(parent = null, rootValue, mutableListOf())
    private val index = Long2ObjectOpenHashMap<TreeNode>()

    init {
        index.put(rootValue.packedPos, root)
    }

    fun contains(value: T): Boolean {
        return index.contains(value.packedPos)
    }

    /**
     * Attaches the items in [chain] as descendants of [attachmentPoint], like this:
     * `[attachmentPoint] -> chain[0] -> chain[1] -> ... chain[n]`
     */
    fun addChain(attachmentPoint: T, chain: List<T>) {
        if (chain.isEmpty()) {
            HardCraft.Logger.error("Unexpected empty chain added to tree: $chain, attached at $attachmentPoint")
            return
        }
        val attachmentNode = index[attachmentPoint.packedPos] ?: run {
            HardCraft.Logger.error("Attachment point $attachmentPoint not found in tree")
            return
        }

        var parent = attachmentNode
        for (item in chain) {
            val child = TreeNode(parent, item, mutableListOf())
            parent.children.add(child)
            parent = child
            index[item.packedPos] = child
        }
    }


    /**
     * Will remove the node with the specified [value] from the its parent in the tree.
     * **The caller is responsible for making sure [value]'s children are removed as well, or are given a new parent**.
     *
     * Returns true if [value] was removed successfully. The return value is important to handle
     * in cases like removing the root (which is not allowed).
     */
    fun remove(value: T): Boolean {
        val node = index[value.packedPos] ?: return false
        // Generally we don't allow removing the root, and the parent needs to handle this.
        if (node.parent == null) return false
        // Remove from tree
        node.parent.children.remove(node)
        // Remove from map
        index.remove(node.value.packedPos)
        return true
    }

    /**
     * Returns all the direct and indirect children of the node with the specified [value].
     */
    fun descendantsOf(value: T): List<T>? {
        val node = index[value.packedPos] ?: return null
        return aggregate(node) { it.children }.map { it.value }
    }

    /**
     * Returns null if [value] is not in the tree OR it is the root (So you need to check contains())
     */
    fun parentOf(value: T): T? {
        return index[value.packedPos]?.parent?.value
    }


    private fun recursiveAddChildren(root: TreeNode, addTo: MutableList<TreeNode>) {
        addTo.add(root)
        for (child in root.children) {
            recursiveAddChildren(child, addTo)
        }
    }

    /**
     * Converts the tree into a pretty-printed string showing the hierarchical structure.
     */
    fun toPrettyString(): String {
        val sb = StringBuilder()
        buildPrettyString(root, sb, "", "")
        return sb.toString()
    }

    private fun buildPrettyString(node: TreeNode, sb: StringBuilder, prefix: String, childPrefix: String) {
        sb.append(prefix)
        sb.append(node.value)
        sb.append('\n')
        val children = node.children
        val iterator = children.iterator()
        while (iterator.hasNext()) {
            val child = iterator.next()
            val isLast = !iterator.hasNext()
            val newPrefix = if (isLast) "$childPrefix└── " else "$childPrefix├── "
            val newChildPrefix = if (isLast) "$childPrefix    " else "$childPrefix│   "
            buildPrettyString(child, sb, newPrefix, newChildPrefix)
        }
    }
}

private class TreeNode(
    val parent: TreeNode?,
    val value: T,
    val children: MutableList<TreeNode>,
)