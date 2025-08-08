package io.github.natanfudge.genericutils.network

import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.createBytebuf
import io.github.natanfudge.genericutils.modId
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.Buf
import kotlinx.serialization.serializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

class ServerPacketContext(val world: ServerWorld)

class ClientPacketContext(val world: ClientWorld?)

//class PacketContextClient(override val world: ClientWorld?) : PacketContext<ClientWorld?>

//hardcraft:load_block_health
//hardcraft:load_block_health


@Environment(EnvType.CLIENT)
fun <T> C2SPacketType<T>.send(value: T) {
    val buf = PacketByteBuf(Unpooled.buffer())
    format.encodeToByteBuf(serializer, value, buf)
    ClientPlayNetworking.send(id, buf)  // Assuming this method exists or you adapt accordingly for client-side sending
}

//interface PacketContext<W : World?> {
//    val world: W
//
//    class Server(override val world: ServerWorld) : PacketContext<ServerWorld>
//}
context (ctx: ModContext)
inline fun <reified T> c2sPacket(path: String, format: Buf = Buf) =
    C2SPacketType<T>(modId(path), format.serializersModule.serializer(), format)

context (ctx: ModContext)
inline fun <reified T> s2cPacket(path: String, format: Buf = Buf) =
    s2cPacket<T>(path, AutomaticPacketSerializer(format.serializersModule.serializer(), format))

context (ctx: ModContext)
inline fun <reified T> s2cPacket(path: String, serializer: PacketSerializer<T>) = S2CPacketType(modId(path), serializer)

class C2SPacketType<T>(val id: Identifier, val serializer: KSerializer<T>, val format: Buf) {
    context(ctx: CommonInit)
    fun register(receiveOnServer: (content: T, context: ServerPacketContext) -> Unit) {
        ServerPlayNetworking.registerGlobalReceiver(id) { server, player, handler, buf, responseSender ->
            val content = format.decodeFromByteBuf(serializer, buf)
            // Assuming PacketContext.Server exists and has an appropriate constructor or factory method
            val serverContext = ServerPacketContext(player.world as ServerWorld)

            server.execute {
                receiveOnServer(content, serverContext)
            }
        }
    }


}

interface PacketSerializer<T> {
    fun write(value: T, buf: PacketByteBuf)
    fun read(buf: PacketByteBuf): T
}

class AutomaticPacketSerializer<T>(private val kSerializer: KSerializer<T>, private val format: Buf) :
    PacketSerializer<T> {
    override fun write(value: T, buf: PacketByteBuf) {
        format.encodeToByteBuf(kSerializer, value, buf)
    }

    override fun read(buf: PacketByteBuf): T {
        return format.decodeFromByteBuf(kSerializer, buf)
    }

}


class S2CPacketType<T>(val id: Identifier, val serializer: PacketSerializer<T>) {
    private inner class FabricPacketWrapper(val value: T) : FabricPacket {
        override fun write(buf: PacketByteBuf?) {
            serializer.write(value, buf!!)
        }

        override fun getType(): PacketType<*>? {
            return fabricType
        }
    }


    private val fabricType = PacketType.create<FabricPacketWrapper>(id) {
        FabricPacketWrapper(serializer.read(it))
    }

    private fun encode(value: T) = createBytebuf().also { serializer.write(value, it) }

    context(ctx: ClientInit)
    @Environment(EnvType.CLIENT)
    fun register(receiveOnClient: (content: T, context: ClientPacketContext) -> Unit) {
        ClientPlayNetworking.registerGlobalReceiver(fabricType) { packet, player, response ->
            receiveOnClient(packet.value, ClientPacketContext(MinecraftClient.getInstance().world))
        }
    }


    fun send(value: T, players: List<ServerPlayerEntity>) {
        for (player in players) {
            ServerPlayNetworking.send(player, id, encode(value))
        }
    }

    fun send(value: T, player: ServerPlayerEntity) {
        ServerPlayNetworking.send(player, id, encode(value))
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



