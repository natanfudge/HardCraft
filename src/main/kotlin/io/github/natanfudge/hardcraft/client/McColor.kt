package io.github.natanfudge.hardcraft.client

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class McColor(val argb: Long) {
    companion object {
        // Predefined colors (ARGB format)
        val Red = McColor(0xFFFF0000L)
        val Green = McColor(0xFF00FF00L)
        val Blue = McColor(0xFF0000FFL)
        val White = McColor(0xFFFFFFFFL)
        val Black = McColor(0xFF000000L)
        val Cyan = McColor(0xFF00FFFFL)
        val Magenta = McColor(0xFFFF00FFL)
        val Yellow = McColor(0xFFFFFF00L)
        val Transparent = McColor(0x00000000L)

        // Factories
        fun fromRGBA(r: Int, g: Int, b: Int, a: Int = 255): McColor {
            val clampedA = a.coerceIn(0..255)
            val clampedR = r.coerceIn(0..255)
            val clampedG = g.coerceIn(0..255)
            val clampedB = b.coerceIn(0..255)
            return McColor(
                (clampedA.toLong() shl 24) or
                        (clampedR.toLong() shl 16) or
                        (clampedG.toLong() shl 8) or
                        clampedB.toLong()
            )
        }
    }

    // Component accessors (0-255)
    val alpha: Int get() = ((argb shr 24) and 0xFF).toInt()
    val red: Int get() = ((argb shr 16) and 0xFF).toInt()
    val green: Int get() = ((argb shr 8) and 0xFF).toInt()
    val blue: Int get() = (argb and 0xFF).toInt()

    // Component accessors (0f-1f)
    val alphaFloat: Float get() = alpha / 255f
    val redFloat: Float get() = red / 255f
    val greenFloat: Float get() = green / 255f
    val blueFloat: Float get() = blue / 255f

    // Builder-style setters
    fun withRed(r: Int): McColor = fromRGBA(r, green, blue, alpha)
    fun withGreen(g: Int): McColor = fromRGBA(red, g, blue, alpha)
    fun withBlue(b: Int): McColor = fromRGBA(red, green, b, alpha)
    fun withAlpha(a: Int): McColor = fromRGBA(red, green, blue, a)

    // Color operations
    fun mix(other: McColor, ratio: Float = 0.5f): McColor {
        val r = (red + (other.red - red) * ratio).toInt()
        val g = (green + (other.green - green) * ratio).toInt()
        val b = (blue + (other.blue - blue) * ratio).toInt()
        val a = (alpha + (other.alpha - alpha) * ratio).toInt()
        return fromRGBA(r, g, b, a)
    }

    // Conversion
    fun toArgb(): Int = argb.toInt()

    override fun toString(): String {
        return String.format(
            "Color(#%08X) [R:%d G:%d B:%d A:%d]",
            argb,
            red,
            green,
            blue,
            alpha
        )
    }
}