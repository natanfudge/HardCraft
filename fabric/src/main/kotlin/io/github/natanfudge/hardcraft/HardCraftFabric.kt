package io.github.natanfudge.hardcraft

import net.fabricmc.api.ModInitializer
import io.github.natanfudge.hardcraft.HardCraft
import net.fabricmc.api.ClientModInitializer

object HardCraftFabric: ModInitializer {
    override fun onInitialize() {
        HardCraft.init()
    }
}

object HardCraftFabricClient: ClientModInitializer {
    override fun onInitializeClient() {
        HardCraftClient.init()
    }
}
