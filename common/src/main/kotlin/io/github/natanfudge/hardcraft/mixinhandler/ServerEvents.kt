package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import net.minecraft.server.network.ServerPlayerEntity

object ServerEvents {
    /**
     * Runs whenever a player loads a chunk, on the server only.
     * Use this to update clients with server data specific to a chunk such as information of a block
     */
    @JvmStatic
    fun onPlayerLoadChunks(player: ServerPlayerEntity) {
        CurrentHealthStorage.sendWorldData(player)
    }
}