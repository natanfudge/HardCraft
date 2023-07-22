package io.github.natanfudge.hardcraft.waila

import io.github.natanfudge.hardcraft.health.getBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.getMaxBlockHealth
import mcp.mobius.waila.api.*
import net.minecraft.block.AbstractBlock
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
        val currentHealth = world.getBlockCurrentHealth(pos) ?: return
        val maxHealth = world.getMaxBlockHealth(pos) ?: return
        tooltip.addLine(Text.literal("Health: $currentHealth/$maxHealth"))
    }
}



