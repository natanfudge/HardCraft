package io.github.natanfudge.hardcraft.client.health

import io.github.natanfudge.hardcraft.health.CHSData
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import io.github.natanfudge.hardcraft.health._clientStorage
import net.minecraft.client.world.ClientWorld

/**
 * Receives health info on the client on a player
 */
fun CurrentHealthStorage.Companion.load(world: ClientWorld, values: CHSData) {
    _clientStorage = CurrentHealthStorage(world, values)
}