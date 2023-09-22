package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.CommonInit
import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.commonInit
import io.github.natanfudge.genericutils.register
import io.github.natanfudge.hardcraft.item.DebugCurrentHealthItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.encodeToNbt
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Serializable
data class Amar(val x: Int, val y: String)

object HardCraft {
    abstract class Context : ModContext.Superclass(ModId)

    const val Name = "HardCraft"
    const val ModId = "hardcraft"
    val Logger = LogManager.getLogger(Name)

    fun init() = commonInit(ModId) {
        println("HardCraft initializing")
        register(
            DebugCurrentHealthItem.Damage, DebugCurrentHealthItem.Repair
        )
    }
}

object HardCraftClient {
    fun init() = with(ClientInit) {
        Packets.initClient()
    }
}

//TODO: next steps.
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