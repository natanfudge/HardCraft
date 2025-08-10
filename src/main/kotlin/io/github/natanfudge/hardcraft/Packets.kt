package io.github.natanfudge.hardcraft


import io.github.natanfudge.genericutils.network.PacketSerializer
import io.github.natanfudge.genericutils.network.s2cPacket
import io.github.natanfudge.hardcraft.health.CHSData
import io.github.natanfudge.hardcraft.health.CHSDataFromBuf
import io.github.natanfudge.hardcraft.health.writeToBuf
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos


object Packets: HardCraft.Context() {
    @Serializable
    data class UpdateBlockHealth(@Contextual val pos: BlockPos, val newHealth: Int?)
    @Serializable
    data class LoadBlockHealth( val healthValues: CHSData)

    @Serializable
    data class RotmCounts(val leftover: Int, val total: Int)

    /** Updates the current health of a single block */
    val updateBlockHealth = s2cPacket<UpdateBlockHealth>("update_block_health")

    /** ROTM HUD counts update */
    val rotmCounts = s2cPacket<RotmCounts>("rotm_counts")

    /**
     * Provides all block health value of a world to a player who loaded a world
     */
    val loadBlockHealth = s2cPacket<LoadBlockHealth>("load_block_health", serializer = object: PacketSerializer<LoadBlockHealth> {
        override fun write(value: LoadBlockHealth, buf: PacketByteBuf) {
            value.healthValues.writeToBuf(buf)
        }

        override fun read(buf: PacketByteBuf): LoadBlockHealth {
            return LoadBlockHealth(CHSDataFromBuf(buf))
        }

    })

}