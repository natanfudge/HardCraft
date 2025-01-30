package io.github.natanfudge.hardcraft.client

import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.hardcraft.Packets.loadBlockHealth
import io.github.natanfudge.hardcraft.Packets.updateBlockHealth
import io.github.natanfudge.hardcraft.client.health.load
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage

object PacketsClient {
    context(ClientInit)
    fun init() {
        updateBlockHealth.register { content, context ->
            CurrentHealthStorage.set(context.world!!, content.pos, content.newHealth)
        }

        loadBlockHealth.register { content, context ->
            CurrentHealthStorage.load(context.world!!, content.healthValues)
        }

    }
}