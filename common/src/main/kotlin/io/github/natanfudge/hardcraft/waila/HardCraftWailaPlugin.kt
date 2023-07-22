package io.github.natanfudge.hardcraft.waila

import io.github.natanfudge.hardcraft.getExistingBlockCurrentHealth
import io.github.natanfudge.hardcraft.getExistingMaxBlockHealth
import mcp.mobius.waila.api.*
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.FurnaceBlock
import net.minecraft.block.GrassBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.text.Text

class HardCraftWailaPlugin : IWailaPlugin {
    override fun register(registrar: IRegistrar) {
        registrar.addComponent(HardcraftBlockComponentProvider, TooltipPosition.BODY, AbstractBlock::class.java)
    }
}

object HardcraftBlockComponentProvider : IBlockComponentProvider {
    override fun appendBody(tooltip: ITooltip, accessor: IBlockAccessor, config: IPluginConfig) {
        val world = accessor.world
        val pos = accessor.position
        tooltip.addLine(Text.literal("Health: ${world.getExistingBlockCurrentHealth(pos)}/${world.getExistingMaxBlockHealth(pos)}"))
    }
}



