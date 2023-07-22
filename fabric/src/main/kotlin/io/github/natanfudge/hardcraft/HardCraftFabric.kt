package io.github.natanfudge.hardcraft

import net.fabricmc.api.ModInitializer
import io.github.natanfudge.hardcraft.HardCraft

object HardCraftFabric: ModInitializer {
    override fun onInitialize() {
        HardCraft.init()
    }
}
