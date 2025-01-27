package io.github.natanfudge.genericutils.client

import io.github.natanfudge.genericutils.network.PacketContext
import io.github.natanfudge.genericutils.network.S2CPacketType
import net.minecraft.client.world.ClientWorld


class PacketContextClient(override val world: ClientWorld?) : PacketContext<ClientWorld?>

context(ClientInit)
fun <T> S2CPacketType<T>.register(receiveOnClient: (content: T, context: PacketContextClient) -> Unit) {
    TODO()
//        NetworkManager.registerReceiver(NetworkManager.s2c(), id) { buf, _ ->
//            val content = serializer.read(buf)
//            getClient().execute {
//                receiveOnClient(content, PacketContext.Client(MinecraftClient.getInstance().world))
//            }
//        }
}


