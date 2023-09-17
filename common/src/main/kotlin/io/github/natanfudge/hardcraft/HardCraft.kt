package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.client.ClientInit
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.encodeToNbt
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
@Serializable
data class Amar(val x: Int, val y: String)

object HardCraft {
    const val Name = "HardCraft"
    const val ModId = "hardcraft"
    val Logger = LogManager.getLogger(Name)

    fun init() {
        val toNbt = Nbt.encodeToNbt(Amar(1,"Asdf"))
        println("Halo Hardcraft! my nbt is ${toNbt}")
    }
}

object HardCraftClient {
    fun init()  = with(ClientInit){
        Packets.initClient()
    }
}

//TODO: next steps.
// 1. Solve bug: only one block renders damage at a time
// 2. Implement debug items:
//   a. Debug: repair block. Crouch+right click to cycle through 100 / 50% / 100%
//   b. Debug: damage block. Crouch + rightclick to cycle through 100 / 50% / 100%
// 3. Implement config screen
// 4. Add config values:
//      a. Hard/Easy mode
// 5. Implement Itemstack block-based damage
//  a. On block destroyed by player / destroyed by mob in easy mode - set itemstack damage to block damage
//  b. On damage itemstack placed - set block damage to itemstack damage
// 6. Implement Support physics (see ideas.md)
// 7. Implement Support destruction by mobs (see ideas.md)
// 8. Nerf pushes like pistons (see ideas.md)
// 9. Balance health of blocks (see ideas.md)
// 10. Implement new items (see ideas.md)
// 11. Think what else I need, if nothing, start working on mob wave generation