package io.github.natanfudge.hardcraft.item

import io.github.natanfudge.genericutils.superclasses.KItem

class DebugCurrentHealthItem private constructor(
    /**
     * True to make it damage blocks, false to make it heal blocks
     */
    val damage: Boolean
): KItem(if(damage) "debug_damage_block" else "debug_repair_block", group = HardcraftItemGroup) {
    companion object {
        val Damage = DebugCurrentHealthItem(true)
        val Repair = DebugCurrentHealthItem(false)
    }

}