package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.commonInit
import io.github.natanfudge.genericutils.register
import io.github.natanfudge.hardcraft.item.HardCraftItemGroup
import io.github.natanfudge.hardcraft.item.HardCraftItems
import kotlinx.serialization.Serializable
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import org.apache.logging.log4j.LogManager


@Serializable
data class Amar(val x: Int, val y: String)

class HardCraft: ModInitializer {
    abstract class Context : ModContext.Superclass(ModId)
    companion object {

        const val Name = "HardCraft"
        const val ModId = "hardcraft"
        val Logger = LogManager.getLogger(Name)
    }



    override fun onInitialize()  = commonInit(ModId) {
        println("HardCraft initializing")
        register(HardCraftItems.All)
        register(HardCraftItemGroup.Instance)

    }
}


// 6. Implement Support physics (see ideas.md)
// 7. Implement Support destruction by mobs (see ideas.md)
// 7.5. Water-resistant mobs
// 8. Push-resistant mobs (see ideas.md)
// 10. Implement new items (see ideas.md)
// 10.5. Add config values:
//      a. Hard/Easy mode
// 11. Think what else I need, if nothing, start working on mob wave generation