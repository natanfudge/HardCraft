package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import net.minecraft.server.network.ServerPlayerEntity

object ServerEvents {
    /**
     * Runs whenever a player enters a world and is ready to receive chunk data
     * Use this to update clients with server data specific to a world
     */
    @JvmStatic
    fun onPlayerInitialChunkLoad(player: ServerPlayerEntity) {
        CurrentHealthStorage.sendWorldData(player)
    }
}