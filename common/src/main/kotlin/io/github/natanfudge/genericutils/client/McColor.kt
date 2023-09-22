package io.github.natanfudge.genericutils.client

@JvmInline
value class McColor(val value: Int) {
    companion object {
        val Black = McColor(0xFFFFFF)
        val Gray = McColor(0x808080)
    }
}