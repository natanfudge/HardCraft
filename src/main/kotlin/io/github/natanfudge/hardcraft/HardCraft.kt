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

//TODO:
// 1.6: Increase enemy sight range so they don't just ignore you if you are above them
// the absolute amount of health the block had.
// 2. Ensure we have mob block towering mechanics working well
// 3. Update ideas.md