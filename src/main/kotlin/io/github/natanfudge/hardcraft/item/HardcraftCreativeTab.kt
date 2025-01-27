package io.github.natanfudge.hardcraft.item

import io.github.natanfudge.genericutils.KItemGroup
import io.github.natanfudge.hardcraft.HardCraft

object HardCraftItemGroup: HardCraft.Context() {
    val Instance = KItemGroup.create(
        name = "hardcraft_tab",
        icon = DebugCurrentHealthItem.Damage,
        items = HardCraftItems.All
    )
}

