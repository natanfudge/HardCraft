package io.github.natanfudge.hardcraft

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameRules
import net.minecraft.world.Heightmap
import kotlin.math.roundToInt
import kotlin.random.Random

//TODO: skeletons need to have bows

object RevengeOfTheMobs {
    data class Wave(val zombies: Int, val creepers: Int, val spiders: Int, val skeletons: Int)
    data class Config(
        val totalWaves: Int = 10,
        val waves: List<Wave> = List(10) { i ->
            // Simple escalating defaults
            val base = 5 + i * 2
            Wave(zombies = base, creepers = base / 2, spiders = base, skeletons = base / 2)
        },
        val minSpawnIntervalTicks: Int = 20,
        val maxSpawnIntervalTicks: Int = 80,
        val minSpawnDistance: Int = 16,
        val maxSpawnDistance: Int = 32,
    )

    // Public so HUD can read it
    val config = Config()

    private data class WorldState(
        var lastWasNight: Boolean = false,
        var currentWaveIndex: Int = 0, // 1-based while active, 0 when none
        var remainingZombies: Int = 0,
        var remainingCreepers: Int = 0,
        var remainingSpiders: Int = 0,
        var remainingSkeletons: Int = 0,
        var nextSpawnAt: Long = Long.MAX_VALUE,
    )

    private val worldStates = mutableMapOf<ServerWorld, WorldState>()

    fun initClient() {
        // HUD overlay for RevengeOfTheMobs waves
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, tickDelta ->
            val client = MinecraftClient.getInstance()
            val world = client.world ?: return@HudRenderCallback
            val font = client.textRenderer ?: return@HudRenderCallback
            val timeOfDay = world.timeOfDay % 24000L
            val isNight = timeOfDay >= 13000L && timeOfDay < 23000L
            val dayIndex = (world.timeOfDay / 24000L).toInt() + 1
            val total = RevengeOfTheMobs.config.totalWaves
            val waveNum = if (dayIndex <= total) dayIndex else total

            val x = 8
            var y = 8

            val waveText = "Wave $waveNum/$total"
            drawContext.drawText(font, waveText, x, y, 0xFFFFFF, true)
            y += 10

            if (!isNight) {
                // Time until next night (start of wave)
                val ticksUntilNight = if (timeOfDay < 13000L) (13000L - timeOfDay) else (13000L + (24000L - timeOfDay))
                val timeStr = formatTicksAsTime(ticksUntilNight)
                drawContext.drawText(font, "Night in: $timeStr", x, y, 0xAAAAFF, true)
                y += 10
            } else {
                // Time until sunrise (end of wave)
                val ticksUntilSunrise = if (timeOfDay < 23000L) (23000L - timeOfDay) else 0L
                val timeStr = formatTicksAsTime(ticksUntilSunrise)
                drawContext.drawText(font, "Sunrise in: $timeStr", x, y, 0xFFAA55, true)
                y += 10
            }
        })
    }

    private fun formatTicksAsTime(ticks: Long): String {
        val totalSeconds = (ticks / 20L).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun init() {
        // 1) Disable natural mob spawning via gamerule on server start
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            for (world in server.worlds) {
                world.gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, server)
            }
        })

        // 2) Each night spawn configured waves; schedule per world via tick
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick { world ->
            if (world.isClient) return@EndWorldTick
            val sw = world as ServerWorld
            val state = worldStates.getOrPut(sw) { WorldState() }
            val timeOfDay = sw.timeOfDay % 24000L
            val isNight = timeOfDay >= 13000L
            val dayIndex = (sw.timeOfDay / 24000L).toInt() + 1

            // Determine which wave index applies to this night
            val waveIndexForThisNight = if (dayIndex <= config.totalWaves) dayIndex else 0

            // Transition: Day -> Night starts a wave if available
            if (!state.lastWasNight && isNight) {
                if (waveIndexForThisNight in 1..config.totalWaves) {
                    state.currentWaveIndex = waveIndexForThisNight
                    val w = config.waves[waveIndexForThisNight - 1]
                    state.remainingZombies = w.zombies
                    state.remainingCreepers = w.creepers
                    state.remainingSpiders = w.spiders
                    state.remainingSkeletons = w.skeletons
                    state.nextSpawnAt = sw.time + randomInterval()
                } else {
                    // No more waves
                    state.currentWaveIndex = 0
                    state.remainingZombies = 0
                    state.remainingCreepers = 0
                    state.remainingSpiders = 0
                    state.remainingSkeletons = 0
                    state.nextSpawnAt = Long.MAX_VALUE
                }
            }

            // Transition: Night -> Day clears any ongoing wave
            if (state.lastWasNight && !isNight) {
                state.currentWaveIndex = 0
                state.remainingZombies = 0
                state.remainingCreepers = 0
                state.remainingSpiders = 0
                state.remainingSkeletons = 0
                state.nextSpawnAt = Long.MAX_VALUE
            }

            // While night and there are remaining spawns, perform spawns at random intervals
            if (isNight && state.currentWaveIndex != 0 && hasRemaining(state)) {
                if (sw.time >= state.nextSpawnAt) {
                    // Spawn one mob according to remaining pool
                    spawnOneAroundRandomPlayer(sw, state)
                    // Schedule next spawn
                    state.nextSpawnAt = sw.time + randomInterval()
                }
            }

            state.lastWasNight = isNight
        })
    }

    private fun hasRemaining(state: WorldState): Boolean {
        return state.remainingZombies > 0 || state.remainingCreepers > 0 || state.remainingSpiders > 0 || state.remainingSkeletons > 0
    }

    private fun randomInterval(): Int {
        val min = config.minSpawnIntervalTicks
        val max = config.maxSpawnIntervalTicks
        return if (max <= min) min else Random.nextInt(min, max + 1)
    }

    private fun spawnOneAroundRandomPlayer(world: ServerWorld, state: WorldState) {
        val players = world.players
        if (players.isEmpty()) return
        val player = players.random()
        val angle = Random.nextDouble(0.0, Math.PI * 2)
        val dist = if (config.maxSpawnDistance <= config.minSpawnDistance) config.minSpawnDistance.toDouble() else Random.nextDouble(
            config.minSpawnDistance.toDouble(), (config.maxSpawnDistance + 1).toDouble()
        )
        val dx = Math.cos(angle) * dist
        val dz = Math.sin(angle) * dist
        val targetX = player.x + dx
        val targetZ = player.z + dz
        val xi = MathHelper.floor(targetX)
        val zi = MathHelper.floor(targetZ)
        val y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, xi, zi)
        val pos = BlockPos(xi, y, zi)
        val spawnPos = Vec3d.ofCenter(pos)

        val type = pickNextType(state) ?: return
        val entity = type.create(world) ?: return
        entity.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, Random.nextFloat() * 360f, 0f)
        // Use SpawnReason.EVENT to avoid natural spawn rules
        world.spawnEntity(entity)
    }

    private fun pickNextType(state: WorldState): EntityType<*>? {
        // Build a list with counts to weight choice
        val choices = mutableListOf<EntityType<*>>()
        if (state.remainingZombies > 0) repeat(state.remainingZombies) { choices.add(EntityType.ZOMBIE) }
        if (state.remainingCreepers > 0) repeat(state.remainingCreepers) { choices.add(EntityType.CREEPER) }
        if (state.remainingSpiders > 0) repeat(state.remainingSpiders) { choices.add(EntityType.SPIDER) }
        if (state.remainingSkeletons > 0) repeat(state.remainingSkeletons) { choices.add(EntityType.SKELETON) }
        if (choices.isEmpty()) return null
        val choice = choices.random()
        when (choice) {
            EntityType.ZOMBIE -> state.remainingZombies--
            EntityType.CREEPER -> state.remainingCreepers--
            EntityType.SPIDER -> state.remainingSpiders--
            EntityType.SKELETON -> state.remainingSkeletons--
        }
        return choice
    }
}