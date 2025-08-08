package io.github.natanfudge.hardcraft

import io.github.natanfudge.genericutils.ModContext
import io.github.natanfudge.genericutils.commonInit
import io.github.natanfudge.genericutils.register
import io.github.natanfudge.hardcraft.ai.debugAI
import io.github.natanfudge.hardcraft.block.EnclosureBlock
import io.github.natanfudge.hardcraft.item.HardCraftItemGroup
import io.github.natanfudge.hardcraft.item.HardCraftItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
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
        register(EnclosureBlock)

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("hcdebugai")
                    .requires { it.hasPermissionLevel(2) }
                    .executes { context ->
                        debugAI =!debugAI
                        context.source.sendFeedback({ Text.literal("HardCraft AI debug ${if(debugAI) "Enabled" else "Disabled"}") }, false)
                        1
                    }
            )
        })
    }
}

