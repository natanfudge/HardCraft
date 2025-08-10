package io.github.natanfudge.hardcraft.test

import io.github.natanfudge.hardcraft.rotm.RotmSpawnAlgo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RotmSpawnAlgoTest {
    @Test
    fun naiveInterval_noCatchup_noClamp() {
        val now = 0L
        val waveStart = 0L
        val waveEnd = 1000L
        val spawnedSoFar = 0
        val totalToSpawn = 10
        val min = 1
        val max = 10_000
        val rand = 1.0
        val interval = RotmSpawnAlgo.computeNextInterval(
            now, waveStart, waveEnd, spawnedSoFar, totalToSpawn, min, max, rand
        )
        println("[DEBUG_LOG] naiveInterval_noCatchup_noClamp interval=$interval")
        // remainingTicks / remainingToSpawn = 1000 / 10 = 100
        assertEquals(100, interval)
    }

    @Test
    fun speedsUpWhenBehind() {
        val waveStart = 0L
        val waveEnd = 1000L
        val now = 500L // 50% progress
        val totalToSpawn = 10
        val spawnedSoFar = 2 // expected at 50% is 5, so behind by 3
        val min = 1
        val max = 10_000
        val rand = 1.0
        val interval = RotmSpawnAlgo.computeNextInterval(
            now, waveStart, waveEnd, spawnedSoFar, totalToSpawn, min, max, rand
        )
        // naive: remainingTicks/remainingToSpawn = 500 / 8 = 62 -> speed up factor 0.7 -> 43.4 -> 43
        assertEquals(43, interval)
    }

    @Test
    fun slowsDownWhenAhead() {
        val waveStart = 0L
        val waveEnd = 1000L
        val now = 500L // 50% progress
        val totalToSpawn = 10
        val spawnedSoFar = 7 // expected at 50% is 5, so ahead by 2
        val min = 1
        val max = 10_000
        val rand = 1.0
        val interval = RotmSpawnAlgo.computeNextInterval(
            now, waveStart, waveEnd, spawnedSoFar, totalToSpawn, min, max, rand
        )
        // naive: remainingTicks/remainingToSpawn = 500 / 3 = 166 -> slow down factor 1.3 -> 215.8 -> 215
        assertEquals(215, interval)
    }

    @Test
    fun clampsToMin() {
        val waveStart = 0L
        val waveEnd = 100L
        val now = 99L
        val totalToSpawn = 100
        val spawnedSoFar = 0
        val min = 20
        val max = 10_000
        val rand = 0.5
        val interval = RotmSpawnAlgo.computeNextInterval(
            now, waveStart, waveEnd, spawnedSoFar, totalToSpawn, min, max, rand
        )
        assertEquals(20, interval)
    }

    @Test
    fun clampsToMax() {
        val waveStart = 0L
        val waveEnd = 20000L
        val now = 1000L
        val totalToSpawn = 2
        val spawnedSoFar = 1
        val min = 1
        val max = 200
        val rand = 1.5
        val interval = RotmSpawnAlgo.computeNextInterval(
            now, waveStart, waveEnd, spawnedSoFar, totalToSpawn, min, max, rand
        )
        assertEquals(200, interval)
    }

    @Test
    fun totalSpawns_overNight_deterministic() {
        val waveStart = 0L
        val waveEnd = 1000L
        val totalToSpawn = 10
        val min = 1
        val max = 100_000
        var spawned = 0
        var now = waveStart
        var step = 0
        while (spawned < totalToSpawn) {
            val interval = RotmSpawnAlgo.computeNextInterval(
                now, waveStart, waveEnd, spawned, totalToSpawn, min, max, 1.0
            )
            val nextTime = now + interval
            println("[DEBUG_LOG] deterministic step=$step now=$now interval=$interval next=$nextTime spawned=$spawned")
            if (nextTime > waveEnd) break
            now = nextTime
            spawned++
            step++
        }
        assertEquals(totalToSpawn, spawned, "Should spawn all mobs within the night window")
    }

    @Test
    fun totalSpawns_overNight_withRandomnessCatchup() {
        val waveStart = 0L
        val waveEnd = 1000L
        val totalToSpawn = 10
        val min = 1
        val max = 100_000
        val randSeq = listOf(0.5, 1.5, 1.2, 0.8, 1.4, 0.6, 1.3, 0.7, 1.1, 0.9)
        var randIdx = 0

        var spawned = 0
        var now = waveStart
        var step = 0
        while (spawned < totalToSpawn) {
            val rand = randSeq[randIdx % randSeq.size]
            randIdx++
            val interval = RotmSpawnAlgo.computeNextInterval(
                now, waveStart, waveEnd, spawned, totalToSpawn, min, max, rand
            )
            val nextTime = now + interval
            println("[DEBUG_LOG] random step=$step now=$now interval=$interval next=$nextTime spawned=$spawned rand=$rand")
            if (nextTime > waveEnd) break
            now = nextTime
            spawned++
            step++
        }
        assertEquals(totalToSpawn, spawned, "Even with randomized intervals and catch-up logic, total spawns should match the target")
    }
}

