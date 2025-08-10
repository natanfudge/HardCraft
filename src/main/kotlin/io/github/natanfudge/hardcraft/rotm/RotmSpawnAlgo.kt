package io.github.natanfudge.hardcraft.rotm

/**
 * Pure spawn interval algorithm used by RotmSpawner, extracted for unit testing.
 */
object RotmSpawnAlgo {
    /**
     * Compute the next spawn interval (in ticks).
     * now: current world time in ticks.
     * waveStart: time when the wave started.
     * waveEnd: time when the wave ends (sunrise).
     * spawnedSoFar: how many mobs have spawned so far in this wave.
     * totalToSpawn: total mobs to spawn this wave.
     * minInterval/maxInterval: clamps.
     * randScale: multiplicative randomness, typically in [0.5, 1.5].
     * catchupThreshold: tolerance for determining behind/ahead.
     * speedupFactor/slowdownFactor: multipliers applied when behind/ahead.
     */
    fun computeNextInterval(
        now: Long,
        waveStart: Long,
        waveEnd: Long,
        spawnedSoFar: Int,
        totalToSpawn: Int,
        minInterval: Int,
        maxInterval: Int,
        randScale: Double,
        catchupThreshold: Double = 0.5,
        speedupFactor: Double = 0.7,
        slowdownFactor: Double = 1.3,
    ): Int {
        val remainingTicks = (waveEnd - now).coerceAtLeast(1L)
        val remainingToSpawn = (totalToSpawn - spawnedSoFar).coerceAtLeast(1)
        val naive = (remainingTicks / remainingToSpawn).toInt().coerceAtLeast(1)
        var interval = (naive * randScale).toInt().coerceAtLeast(1)

        val totalDuration = (waveEnd - waveStart).coerceAtLeast(1L).toDouble()
        val progress = ((now - waveStart).coerceAtLeast(0L)).toDouble() / totalDuration
        val expected = totalToSpawn * progress
        val behind = expected - spawnedSoFar
        interval = when {
            behind > catchupThreshold -> (interval * speedupFactor).toInt().coerceAtLeast(1)
            behind < -catchupThreshold -> (interval * slowdownFactor).toInt().coerceAtLeast(1)
            else -> interval
        }
        return interval.coerceIn(minInterval, maxInterval)
    }
}