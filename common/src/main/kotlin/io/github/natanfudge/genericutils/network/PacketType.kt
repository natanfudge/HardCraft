package io.github.natanfudge.genericutils.network

import dev.architectury.networking.NetworkManager
import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.client.getClient
import io.github.natanfudge.genericutils.createBytebuf
import io.github.natanfudge.genericutils.modId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.Buf
import kotlinx.serialization.serializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

sealed interface PacketContext<W : World?> {
    val world: W

    class Client(override val world: ClientWorld?) : PacketContext<ClientWorld?>
    class Server(override val world: ServerWorld) : PacketContext<ServerWorld>
}
context (ModContext)
inline fun <reified T> c2sPacket(path: String, format: Buf = Buf) = C2SPacketType<T>(modId(path), format.serializersModule.serializer(), format)
context (ModContext)
inline fun <reified T> s2cPacket(path: String, format: Buf = Buf) =
    s2cPacket<T>(path, AutomaticPacketSerializer(format.serializersModule.serializer(), format))
context (ModContext)
inline fun <reified T> s2cPacket(path: String, serializer: PacketSerializer<T>) = S2CPacketType(modId(path), serializer)

class C2SPacketType<T>(private val id: Identifier, private val serializer: KSerializer<T>, private val format: Buf) {
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

interface PacketSerializer<T> {
    fun write(value: T, buf: PacketByteBuf)
    fun read(buf: PacketByteBuf): T
}

class AutomaticPacketSerializer<T>(private val kSerializer: KSerializer<T>, private val format: Buf) : PacketSerializer<T> {
    override fun write(value: T, buf: PacketByteBuf) {
        format.encodeToByteBuf(kSerializer, value, buf)
    }

    override fun read(buf: PacketByteBuf): T {
        return format.decodeFromByteBuf(kSerializer, buf)
    }

}


class S2CPacketType<T>(private val id: Identifier, private val serializer: PacketSerializer<T>) {
    context(ClientInit)
    fun register(receiveOnClient: (content: T, context: PacketContext.Client) -> Unit) {
        NetworkManager.registerReceiver(NetworkManager.s2c(), id) { buf, _ ->
            val content = serializer.read(buf)
            getClient().execute {
                receiveOnClient(content, PacketContext.Client(MinecraftClient.getInstance().world))
            }
        }
    }


    private fun encode(value: T) = createBytebuf().also { serializer.write(value, it) }
    fun send(value: T, players: List<ServerPlayerEntity>) {
        NetworkManager.sendToPlayers(players, id, encode(value))
    }

    fun send(value: T, player: ServerPlayerEntity) {
        NetworkManager.sendToPlayer(player, id, encode(value))
    }

    private fun getObservers(world: ServerWorld, pos: BlockPos): List<ServerPlayerEntity> {
        return world.chunkManager.threadedAnvilChunkStorage.getPlayersWatchingChunk(ChunkPos(pos), false);
    }

    fun sendToObservers(value: T, world: ServerWorld, pos: BlockPos) {
        send(value, getObservers(world, pos))
    }

    fun sendToWorld(value: T, world: ServerWorld) {
        send(value, world.players)
    }

}



