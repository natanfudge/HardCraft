package io.github.natanfudge.genericutils

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object KItemGroup{
    context(ModContext)
    fun create(name: String, icon: Item,  items: List<Item>): ItemGroup = FabricItemGroup.builder()
        .icon { ItemStack(icon) }
        .displayName(Text.translatable("itemGroup.$modId.$name"))
        .entries { context, entries ->
            for(item in items) {
                entries.add { item }
            }
        }
        .build()
}


