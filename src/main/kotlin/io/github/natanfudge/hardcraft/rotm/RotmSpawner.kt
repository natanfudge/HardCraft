package io.github.natanfudge.hardcraft.rotm

import io.github.natanfudge.hardcraft.Packets
import io.github.natanfudge.hardcraft.ai.makeSmart
import io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.SkeletonEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameRules
import net.minecraft.world.Heightmap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object RotmSpawner {
    private data class WorldState(
        var lastWasNight: Boolean = false,
        var currentWaveIndex: Int = 0,
        var totalToSpawn: Int = 0,
        var spawnedSoFar: Int = 0,
        var nextSpawnAt: Long = Long.MAX_VALUE,
        var waveStartAt: Long = 0L,
        var waveEndAt: Long = 0L,
        val spawnedMobIds: MutableSet<java.util.UUID> = mutableSetOf()
    )

    private val worldStates = mutableMapOf<ServerWorld, WorldState>()

    private fun sendCounts(world: ServerWorld, state: WorldState) {
        val leftover = (state.totalToSpawn - state.spawnedSoFar).coerceAtLeast(0)
        val total = state.totalToSpawn
        // Use packet to all players in this world
        Packets.rotmCounts.send(Packets.RotmCounts(leftover, total), world.players)
    }

    fun getCounts(world: ServerWorld): Pair<Int, Int> {
        val state = worldStates[world] ?: return 0 to 0
        return (state.totalToSpawn - state.spawnedSoFar).coerceAtLeast(0) to state.totalToSpawn
    }

    fun clearAllMobsAndState(world: ServerWorld) {
        val state = worldStates.getOrPut(world) { WorldState() }
        // Kill ROTM-tagged/tracked entities
        val toRemove = state.spawnedMobIds.toList()
        for (id in toRemove) {
            val e = world.getEntity(id)
            if (e != null) {
                e.discard()
            }
        }
        state.spawnedMobIds.clear()
        // Reset state
        state.currentWaveIndex = 0
        state.totalToSpawn = 0
        state.spawnedSoFar = 0
        state.nextSpawnAt = Long.MAX_VALUE
        state.waveStartAt = 0L
        state.waveEndAt = 0L
        state.lastWasNight = false
        sendCounts(world, state)
    }

    fun spawnSpecial(world: ServerWorld, count: Int) {
        // Clear first, per requirement
        clearAllMobsAndState(world)
        val state = worldStates.getOrPut(world) { WorldState() }
        // Spawn immediately without affecting wave counts
        repeat(count.coerceAtLeast(0)) {
            spawnOneAroundRandomPlayer(world, state)
        }
        // After special spawns, counts remain 0
        sendCounts(world, state)
    }

    fun startNextNight(world: ServerWorld) {
        clearAllMobsAndState(world)
        val tod = world.timeOfDay % 24000L
        val base = world.timeOfDay - tod
        val newTime = if (tod < 13000L) base + 13000L else base + 24000L + 13000L
        world.setTimeOfDay(newTime)
    }

    fun startPrevNight(world: ServerWorld) {
        clearAllMobsAndState(world)
        val tod = world.timeOfDay % 24000L
        val base = world.timeOfDay - tod
        val newTime = if (tod >= 13000L) base + 13000L else base - 24000L + 13000L
        world.setTimeOfDay(newTime)
    }

    fun startNight(world: ServerWorld, nightIndex: Int) {
        clearAllMobsAndState(world)
        val idx = nightIndex.coerceAtLeast(1)
        val newTime = (idx - 1).toLong() * 24000L + 13000L
        world.setTimeOfDay(newTime)
    }

    fun startNextDay(world: ServerWorld) {
        clearAllMobsAndState(world)
        val tod = world.timeOfDay % 24000L
        val base = world.timeOfDay - tod
        val newTime = if (tod < 1000L) base + 1000L else base + 24000L + 1000L
        world.setTimeOfDay(newTime)
    }

    fun init() {
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick { world ->
            if (world.isClient) return@EndWorldTick
            val sw = world as ServerWorld
            val state = worldStates.getOrPut(sw) { WorldState() }
            val timeOfDay = sw.timeOfDay % 24000L
            val isNight = timeOfDay >= 13000L && timeOfDay < 23000L
            val dayIndex = (sw.timeOfDay / 24000L).toInt() + 1

            // Toggle natural spawning based on day/night
            if (!state.lastWasNight && isNight) {
                // Day -> Night
                sw.gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, sw.server)

                val waveIndex = if (dayIndex <= RotmConfig.config.totalWaves) dayIndex else 0
                state.currentWaveIndex = waveIndex
                state.spawnedSoFar = 0
                state.totalToSpawn = if (waveIndex > 0) RotmConfig.config.waves[waveIndex - 1].enemyCount else 0
                state.waveStartAt = sw.time
                val ticksToSunrise = if (timeOfDay < 23000L) (23000L - timeOfDay) else 0L
                state.waveEndAt = sw.time + ticksToSunrise
                state.nextSpawnAt = if (state.totalToSpawn > 0) sw.time + computeNextInterval(sw, state) else Long.MAX_VALUE
                sendCounts(sw, state)
            }
            if (state.lastWasNight && !isNight) {
                // Night -> Day
                sw.gameRules.get(GameRules.DO_MOB_SPAWNING).set(true, sw.server)
                state.currentWaveIndex = 0
                state.totalToSpawn = 0
                state.spawnedSoFar = 0
                state.nextSpawnAt = Long.MAX_VALUE
                state.waveStartAt = 0L
                state.waveEndAt = 0L
                sendCounts(sw, state)
            }

            // While night, run wave spawns
            if (isNight && state.currentWaveIndex != 0 && state.spawnedSoFar < state.totalToSpawn) {
                if (sw.time >= state.nextSpawnAt) {
                    spawnOneAroundRandomPlayer(sw, state)
                    state.spawnedSoFar++
                    sendCounts(sw, state)
                    if (state.spawnedSoFar < state.totalToSpawn) {
                        state.nextSpawnAt = sw.time + computeNextInterval(sw, state)
                    } else {
                        state.nextSpawnAt = Long.MAX_VALUE
                    }
                }
            }

            // Subtle persistent custom particles around ROTM mobs
            if (state.spawnedMobIds.isNotEmpty() && sw.time % 10L == 0L) {
                for (id in state.spawnedMobIds) {
                    val e = sw.getEntity(id) as? LivingEntity ?: continue
                    val py2 = e.y + (e.height * 0.5)
                    sw.spawnParticles(ParticleTypes.FLAME, e.x, py2, e.z, 1, 0.15, 0.2, 0.15, 0.005)
                    sw.spawnParticles(ParticleTypes.END_ROD, e.x, py2, e.z, 1, 0.1, 0.15, 0.1, 0.0)
                }
            }

            state.lastWasNight = isNight
        })
    }

    private fun computeNextInterval(world: ServerWorld, state: WorldState): Int {
        val now = world.time
        val min = RotmConfig.config.minSpawnIntervalTicks
        val max = RotmConfig.config.maxSpawnIntervalTicks
        val randScale = Random.nextDouble(0.5, 1.5)
        return RotmSpawnAlgo.computeNextInterval(
            now = now,
            waveStart = state.waveStartAt,
            waveEnd = state.waveEndAt,
            spawnedSoFar = state.spawnedSoFar,
            totalToSpawn = state.totalToSpawn,
            minInterval = min,
            maxInterval = max,
            randScale = randScale
        )
    }

    private fun spawnOneAroundRandomPlayer(world: ServerWorld, state: WorldState) {
        val players = world.players
        if (players.isEmpty()) return
        val player = players.random()
        val angle = Random.nextDouble(0.0, Math.PI * 2)
        val minDist = RotmConfig.config.minSpawnDistance
        val maxDist = RotmConfig.config.maxSpawnDistance
        val dist = if (maxDist <= minDist) minDist.toDouble() else Random.nextDouble(minDist.toDouble(), (maxDist + 1).toDouble())
        val dx = Math.cos(angle) * dist
        val dz = Math.sin(angle) * dist
        val targetX = player.x + dx
        val targetZ = player.z + dz
        val xi = MathHelper.floor(targetX)
        val zi = MathHelper.floor(targetZ)
        val y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, xi, zi)
        val pos = BlockPos(xi, y, zi)
        val spawnPos = Vec3d.ofCenter(pos)

        val wave = RotmConfig.config.waves[state.currentWaveIndex - 1]
        val type = pickTypeByFractions(wave)
        val entity = type.create(world) ?: return
        entity.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, Random.nextFloat() * 360f, 0f)

        applyWaveProperties(entity, wave)

        // Custom golden fire-like particle burst on spawn
        val py = entity.y + (entity.height * 0.5)
        world.spawnParticles(ParticleTypes.FLAME, entity.x, py, entity.z, 8, 0.35, 0.5, 0.35, 0.01)
        world.spawnParticles(ParticleTypes.END_ROD, entity.x, py, entity.z, 4, 0.2, 0.3, 0.2, 0.0)

        world.spawnEntity(entity)
        // Track ROTM-spawned mobs for clearing
        worldStates[world]?.spawnedMobIds?.add(entity.uuid)
    }

    private fun pickTypeByFractions(wave: RotmConfig.Wave): EntityType<*> {
        val z = max(0f, wave.zombieFraction)
        val c = max(0f, wave.creeperFraction)
        val s = max(0f, wave.spiderFraction)
        val sk = max(0f, wave.skeletonFraction)
        var total = z + c + s + sk
        if (total <= 0f) total = 1f
        val r = Random.nextFloat() * total
        return when {
            r < z -> EntityType.ZOMBIE
            r < z + c -> EntityType.CREEPER
            r < z + c + s -> EntityType.SPIDER
            else -> EntityType.SKELETON
        }
    }

    private fun applyWaveProperties(entity: Entity, wave: RotmConfig.Wave) {
        // Determine player-based scaling: 1 + 0.5 * (player_num - 1)
        val world = entity.world
        val playerScale: Float = if (world is ServerWorld) {
            val players = world.players.size.coerceAtLeast(1)
            1f + 0.5f * (players - 1)
        } else 1f

        // Attributes & equipment for living entities
        if (entity is LivingEntity) {
            val hp = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
            if (hp != null) {
                val mult = wave.hpMultiplier * playerScale
                if (mult != 1f) {
                    hp.baseValue = hp.baseValue * mult
                    entity.health = entity.maxHealth
                }
            }
            // Melee damage multiplier still applies to melee mobs; for skeleton bows we handle via Power enchant below
            val dmg = entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
            if (dmg != null) {
                val mult = wave.damageMultiplier * playerScale
                if (mult != 1f) {
                    dmg.baseValue = dmg.baseValue * mult
                }
            }
            val spd = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            if (spd != null) {
                val mult = wave.speedMultiplier * playerScale
                if (mult != 1f) {
                    spd.baseValue = spd.baseValue * mult
                }
            }
        }

        // Smart AI
        if (wave.smartEnemies && entity is HostileEntity) {
            entity.makeSmart()
        }

        // Immunities and demolition via mixin interface
        if (entity is HostileEntity) {
            if (wave.fluidImmunity) entity.hardcraft_setIsPushedByFluids(false)
            if (wave.lavaImmunity) entity.hardcraft_setIsFireImmune(true)
            if (wave.pistonImmunity) entity.hardcraft_setIsPistonImmune(true)
            // Demolition multiplier
            val baseDemo = entity.hardcraft_getDemolition()
            val demoMult = wave.demolitionMultiplier * playerScale
            val newDemo = ceil(baseDemo * demoMult).toInt().coerceAtLeast(0)
            entity.hardcraft_setDemolition(newDemo)
        }

        // Skeletons get bows with Power based on combined damage multiplier (wave * playerScale)
        if (entity is SkeletonEntity) {
            val bow = ItemStack(Items.BOW)
            val combined = wave.damageMultiplier * playerScale
            val level = powerLevelFromDamageMultiplier(combined)
            if (level > 0) bow.addEnchantment(Enchantments.POWER, level)
            entity.equipStack(EquipmentSlot.MAINHAND, bow)
        }

    }
}

private fun powerLevelFromDamageMultiplier(multiplier: Float): Int {
    val baseDamage = 2.0f
    val extraDamage = baseDamage * multiplier - baseDamage
    return ((extraDamage - 0.5f) / 0.5f).roundToInt()
}