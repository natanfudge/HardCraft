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

//TODO: 1. Round up TODOS and resolve them
// 1.1: Fix - hitting block with debug damage stick does not make block appear to be damaged
// 1.2: Fix - when a block is broken, the HP of the dropped item should be set to the percentage of damage that the block had, not
// 1.3: Fix - Make mob only create blocks when they have no path to the target. We could set mob.hasRealPath in HardCraftPathNodeNavigator
// 1.4: Fix - Mobs do not attempt to block in the direction they are moving
// 1.5: Before attempting to block up, the mob should check if it has enough space to jump up, and if so it will break the block first.
// the absolute amount of health the block had.
// 2. Ensure we have mob block towering mechanics working well
// 3. Update ideas.md