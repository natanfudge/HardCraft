package io.github.natanfudge.hardcraft.client

import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.hardcraft.rotm.RevengeOfTheMobs
import io.github.natanfudge.hardcraft.client.debug.DebugRendering
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.getBlockCurrentHealth
import io.github.natanfudge.hardcraft.health.getBlockMaxHealth
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text


class HardCraftClient : ClientModInitializer {
    override fun onInitializeClient() = with(ClientInit) {
        ItemTooltipCallback.EVENT.register { stack, context, lines ->
            val health = stack.getBlockCurrentHealth() ?: return@register
            val maxHealth = stack.getBlockMaxHealth() ?: return@register

            if (health != maxHealth) {
                lines.add(Text.literal("$health/$maxHealth HP"))
            }
        }

        DebugRendering.registerClient()

        PacketsClient.init()

        RevengeOfTheMobs.initClient()

    }


}

