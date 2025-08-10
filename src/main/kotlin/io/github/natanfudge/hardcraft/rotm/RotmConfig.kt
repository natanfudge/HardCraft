package io.github.natanfudge.hardcraft.rotm

import kotlin.math.roundToInt

object RotmConfig {
    data class Wave(
        val enemyCount: Int,
        val zombieFraction: Float,
        val creeperFraction: Float,
        val spiderFraction: Float,
        val skeletonFraction: Float,
        val hpMultiplier: Float,
        val damageMultiplier: Float,
        val speedMultiplier: Float,
        val fluidImmunity: Boolean,
        val lavaImmunity: Boolean,
        val pistonImmunity: Boolean,
        val smartEnemies: Boolean,
        val demolitionMultiplier: Float
    )

    data class Config(
        val totalWaves: Int = 10,
        val waves: List<Wave> = List(10) { i ->
            val waveNum = i + 1
            val enemyCount = 20 + i * 5
            val zombieF = 0.4f
            val creeperF = 0.2f
            val spiderF = 0.2f
            val skeletonF = 0.2f
            Wave(
                enemyCount = enemyCount,
                zombieFraction = zombieF,
                creeperFraction = creeperF,
                spiderFraction = spiderF,
                skeletonFraction = skeletonF,
                hpMultiplier = 1 + i / 10f,
                damageMultiplier = 1 + i / 10f,
                speedMultiplier = 1 + i / 10f,
                fluidImmunity = waveNum >= 3,
                lavaImmunity = waveNum >= 5,
                pistonImmunity = waveNum >= 7,
                smartEnemies = waveNum >= 2,
                demolitionMultiplier = 1 + i / 10f
            )
        },
        // These are fallback clamps; actual spawn interval is dynamic per remaining night time
        val minSpawnIntervalTicks: Int = 1,
        val maxSpawnIntervalTicks: Int = 1000,
        val minSpawnDistance: Int = 16,
        val maxSpawnDistance: Int = 32
    )

    //TODO: very slow to reconstruct this every time, this is just for dev
    val config  = Config()
}