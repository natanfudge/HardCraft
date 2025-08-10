package io.github.natanfudge.hardcraft.rotm

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient

object RotmHud {
    private var leftover: Int = 0
    private var total: Int = 0

    fun setCounts(left: Int, tot: Int) {
        leftover = left
        total = tot
    }

    fun initClient() {
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, _ ->
            val client = MinecraftClient.getInstance()
            val world = client.world ?: return@HudRenderCallback
            val font = client.textRenderer ?: return@HudRenderCallback
            val timeOfDay = world.timeOfDay % 24000L
            val isNight = timeOfDay >= 13000L && timeOfDay < 23000L
            val dayIndex = (world.timeOfDay / 24000L).toInt() + 1
            val totalWaves = RotmConfig.config.totalWaves
            val waveNum = if (dayIndex <= totalWaves) dayIndex else totalWaves

            val x = 8
            var y = 8

            val waveText = "Wave $waveNum/$totalWaves"
            drawContext.drawText(font, waveText, x, y, 0xFFFFFF, true)
            y += 10

            // Remaining mobs info
            if (total > 0) {
                drawContext.drawText(font, "Remaining: $leftover/$total", x, y, 0x55FF55, true)
                y += 10
            }

            if (!isNight) {
                val ticksUntilNight = if (timeOfDay < 13000L) (13000L - timeOfDay) else (13000L + (24000L - timeOfDay))
                val timeStr = formatTicksAsTime(ticksUntilNight)
                drawContext.drawText(font, "Night in: $timeStr", x, y, 0xAAAAFF, true)
            } else {
                val ticksUntilSunrise = if (timeOfDay < 23000L) (23000L - timeOfDay) else 0L
                val timeStr = formatTicksAsTime(ticksUntilSunrise)
                drawContext.drawText(font, "Sunrise in: $timeStr", x, y, 0xFFAA55, true)
            }
        })
    }

    private fun formatTicksAsTime(ticks: Long): String {
        val totalSeconds = (ticks / 20L).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}