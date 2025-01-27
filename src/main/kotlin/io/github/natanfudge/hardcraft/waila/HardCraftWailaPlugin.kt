package io.github.natanfudge.hardcraft.waila

import io.github.natanfudge.hardcraft.health.getCurrentBlockHealth
import io.github.natanfudge.hardcraft.health.getMaxBlockHealth
import mcp.mobius.waila.api.IBlockAccessor
import mcp.mobius.waila.api.IBlockComponentProvider
import mcp.mobius.waila.api.IPluginConfig
import mcp.mobius.waila.api.IRegistrar
import mcp.mobius.waila.api.ITooltip
import mcp.mobius.waila.api.IWailaPlugin
import mcp.mobius.waila.api.TooltipPosition
import net.minecraft.block.AbstractBlock
import net.minecraft.text.Text

class HardCraftWailaPlugin : IWailaPlugin {
    override fun register(registrar: IRegistrar) {
        registrar.addComponent(HardcraftBlockComponentProvider, TooltipPosition.BODY, AbstractBlock::class.java)
    }
}

/**
 * Shows "Health: " info of blocks in waila
 */
object HardcraftBlockComponentProvider : IBlockComponentProvider {
    override fun appendBody(tooltip: ITooltip, accessor: IBlockAccessor, config: IPluginConfig) {
        val world = accessor.world
        val pos = accessor.position
        val currentHealth = world.getCurrentBlockHealth(pos) ?: return
        val maxHealth = world.getMaxBlockHealth(pos) ?: return
        println("According to waila, health at ${pos} is $currentHealth/$maxHealth")
        tooltip.addLine(Text.literal("Health: $currentHealth/$maxHealth"))
    }
}



