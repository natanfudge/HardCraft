package io.github.natanfudge.hardcraft.block

import io.github.natanfudge.genericutils.Registerable
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

/**
 * An invisible, solid, indestructible block used for building enclosures.
 */
object EnclosureBlock : Block(
    AbstractBlock.Settings.create()
        .strength(-1.0f, 3_600_000f) // Unbreakable like bedrock
        .dropsNothing()
        .nonOpaque() // Do not occlude neighbor faces (like barrier), but keep full collision
), Registerable<Block> {
    override val idPath: String = "enclosure_block"
    override val registry: Registry<Block> = Registries.BLOCK
    override val component: Block get() = this

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.INVISIBLE
}