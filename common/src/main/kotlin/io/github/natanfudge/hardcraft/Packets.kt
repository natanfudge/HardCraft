package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.network.s2cPacket
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.BlockPosSerializer
import net.minecraft.util.math.BlockPos

object Packets {
    @Serializable
    data class UpdateBlockHealth(@Serializable(with = BlockPosSerializer::class) val pos: BlockPos, val newHealth: Int)

    val updateBlockHealth = s2cPacket<UpdateBlockHealth>("update_block_health")
    context(ClientInit)
    fun initClient() {
        updateBlockHealth.register { content, context ->
            CurrentHealthStorage.set(context.player.world, content.pos, content.newHealth)
        }
    }
}