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
import io.github.natanfudge.hardcraft.rotm.RevengeOfTheMobs
import io.github.natanfudge.hardcraft.rotm.RotmSpawner
import net.minecraft.server.world.ServerWorld


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

        // Initialize wave-based spawning feature
        RevengeOfTheMobs.init()

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            // Toggle AI debug
            dispatcher.register(
                CommandManager.literal("hcdebugai")
                    .requires { it.hasPermissionLevel(2) }
                    .executes { context ->
                        debugAI = !debugAI
                        context.source.sendFeedback({ Text.literal("HardCraft AI debug ${if (debugAI) "Enabled" else "Disabled"}") }, false)
                        1
                    }
            )

            // RevengeOfTheMobs commands
            dispatcher.register(
                CommandManager.literal("rotm")
                    .requires { it.hasPermissionLevel(2) }
                    .then(CommandManager.literal("nextNight").executes { ctx ->
                        val world = ctx.source.world as ServerWorld
                        RotmSpawner.startNextNight(world)
                        ctx.source.sendFeedback({ Text.literal("ROTM: Next night started (cleared state)") }, false)
                        1
                    })
                    .then(CommandManager.literal("prevNight").executes { ctx ->
                        val world = ctx.source.world as ServerWorld
                        RotmSpawner.startPrevNight(world)
                        ctx.source.sendFeedback({ Text.literal("ROTM: Previous night started (cleared state)") }, false)
                        1
                    })
                    .then(CommandManager.literal("night")
                        .then(CommandManager.argument("index", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                            .executes { ctx ->
                                val world = ctx.source.world as ServerWorld
                                val idx = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "index")
                                RotmSpawner.startNight(world, idx)
                                ctx.source.sendFeedback({ Text.literal("ROTM: Night $idx started (cleared state)") }, false)
                                1
                            }
                        )
                    )
                    .then(CommandManager.literal("nextDay").executes { ctx ->
                        val world = ctx.source.world as ServerWorld
                        RotmSpawner.startNextDay(world)
                        ctx.source.sendFeedback({ Text.literal("ROTM: Skipped to day (cleared state)") }, false)
                        1
                    })
                    .then(CommandManager.literal("spawnSpecial")
                        .then(CommandManager.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
                            .executes { ctx ->
                                val world = ctx.source.world as ServerWorld
                                val count = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "count")
                                RotmSpawner.spawnSpecial(world, count)
                                ctx.source.sendFeedback({ Text.literal("ROTM: Spawned $count special mobs (cleared state)") }, false)
                                1
                            }
                        )
                    )
                    .then(CommandManager.literal("clear").executes { ctx ->
                        val world = ctx.source.world as ServerWorld
                        RotmSpawner.clearAllMobsAndState(world)
                        ctx.source.sendFeedback({ Text.literal("ROTM: Cleared all ROTM mobs and state") }, false)
                        1
                    })
            )
        })
    }
}

