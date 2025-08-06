package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.commonInit
import io.github.natanfudge.genericutils.register
import io.github.natanfudge.hardcraft.item.HardCraftItemGroup
import io.github.natanfudge.hardcraft.item.HardCraftItems
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager


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
// 1.2 Fix bad health value (see log)
// 1.3: Fix - Make mob only create blocks when they have no path to the target. We could set mob.hasRealPath in HardCraftPathNodeNavigator
// 1.4: Fix - Mobs do not attempt to block in the direction they are moving
// 1.5: Before attempting to block up, the mob should check if it has enough space to jump up, and if so it will break the block first.
// 1.6: Increase enemy sight range so they don't just ignore you if you are above them
// the absolute amount of health the block had.
// 2. Ensure we have mob block towering mechanics working well
// 3. Update ideas.md