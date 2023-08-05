@file:UseSerializers(BlockPosSerializer::class)
package io.github.natanfudge.hardcraft


import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.network.s2cPacket
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.minecraft.BlockPosSerializer
import net.minecraft.util.math.BlockPos


object Packets {
    @Serializable
    data class UpdateBlockHealth( val pos: BlockPos, val newHealth: Int)
    @Serializable
    data class LoadBlockHealth( val healthValues: Map<BlockPos, Int>)

    /** Updates the current health of a single block */
    val updateBlockHealth = s2cPacket<UpdateBlockHealth>("update_block_health")

    /**
     * Provides all block health value of a world to a player who loaded a world
     */
    val loadBlockHealth = s2cPacket<LoadBlockHealth>("load_block_health")
    context(ClientInit)
    fun initClient() {
        updateBlockHealth.register { content, context ->
            CurrentHealthStorage.set(context.world!!, content.pos, content.newHealth)
        }

        loadBlockHealth.register { content, context ->
            CurrentHealthStorage.load(context.world!!, content.healthValues)
        }

    }
}