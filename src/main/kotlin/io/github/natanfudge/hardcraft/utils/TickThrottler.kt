package io.github.natanfudge.hardcraft.utils

/**
 * Allows calling a function only every X ticks
 * Only 1 [runThrottled] should be used per [TickThrottler]
 */
class TickThrottler {
    var ticksPassed = 0

    /**
     * Must be called every tick to work
     */
    inline fun runThrottled(ticksPerRun: Int, code: () -> Unit) {
        require(ticksPerRun >= 1)
        ticksPassed++
        if (ticksPassed >= ticksPerRun) {
            ticksPassed = 0
        }
        if (ticksPassed == 0) {
            code()
        }
    }
}
