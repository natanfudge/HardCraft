package io.github.natanfudge.genericutils.network

import dev.architectury.networking.NetworkManager
import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.createBytebuf
import io.github.natanfudge.genericutils.csId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.Buf
import kotlinx.serialization.serializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

sealed interface PacketContext<W: World?> {
    val world: W
    class Client(override val world: ClientWorld?) : PacketContext<ClientWorld?>
    class Server(override val world: ServerWorld) : PacketContext<ServerWorld>
}

inline fun <reified T> c2sPacket(path: String, format: Buf = Buf) = C2SPacketType<T>(csId(path), format.serializersModule.serializer(), format)
inline fun <reified T> s2cPacket(path: String, format: Buf = Buf) = S2CPacketType<T>(csId(path), format.serializersModule.serializer(), format)

class C2SPacketType<T>(private val id: Identifier, private val serializer: KSerializer<T>,private  val format: Buf) {
    context(CommonInit)
    fun register(receiveOnServer: (content: T, context: PacketContext.Server) -> Unit) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), id) { buf, context ->
            val content = format.decodeFromByteBuf(serializer, buf)
            val server = context.player.server ?: return@registerReceiver
            server.execute {
                receiveOnServer(content, PacketContext.Server(context.player.world as ServerWorld))
            }
        }
    }
    fun send(value: T) = NetworkManager.sendToServer(id, createBytebuf().also { format.encodeToByteBuf(serializer, value, it) })
}



class S2CPacketType<T>(private val id: Identifier,private  val serializer: KSerializer<T>,private  val format: Buf) {
    context(ClientInit)
    fun register(receiveOnClient: (content: T, context: PacketContext.Client) -> Unit) {
        NetworkManager.registerReceiver(NetworkManager.s2c(), id) { buf, context ->
            val content = format.decodeFromByteBuf(serializer, buf)
            getClient().execute {
                receiveOnClient(content, PacketContext.Client(MinecraftClient.getInstance().world))
            }
        }
    }


    private fun encode(value: T) = createBytebuf().also { format.encodeToByteBuf(serializer, value, it) }
    fun send(value: T, players: List<ServerPlayerEntity>) {
        NetworkManager.sendToPlayers(players, id, encode(value))
    }

    fun send(value: T, player: ServerPlayerEntity) {
        NetworkManager.sendToPlayer(player, id, encode(value))
    }

    private fun getObservers(world: ServerWorld, pos: BlockPos): List<ServerPlayerEntity>  {
       return world.chunkManager.threadedAnvilChunkStorage.getPlayersWatchingChunk(ChunkPos(pos), false);
    }

    fun sendToObservers(value: T, world: ServerWorld, pos: BlockPos) {
        send(value, getObservers(world, pos))
    }
    fun sendToWorld(value: T, world: ServerWorld) {
        send(value, world.players)
    }

}



