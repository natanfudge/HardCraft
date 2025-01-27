package io.github.natanfudge.hardcraft.client

import io.github.natanfudge.genericutils.client.ClientInit
import net.fabricmc.api.ClientModInitializer

class HardCraftClient : ClientModInitializer {
    override fun onInitializeClient() = with(ClientInit) {
        PacketsClient.init()
    }
}