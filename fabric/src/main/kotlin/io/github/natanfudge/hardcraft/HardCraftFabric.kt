package io.github.natanfudge.hardcraft

import net.fabricmc.api.ModInitializer
import io.github.natanfudge.hardcraft.HardCraft
import net.fabricmc.api.ClientModInitializer
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent

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
