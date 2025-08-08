package io.github.natanfudge.genericutils

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text

class KItemGroup(val group: ItemGroup, val name: String): Registerable<ItemGroup> {
    override val idPath: String
        get() = name
    override val registry: Registry<ItemGroup>
        get() = Registries.ITEM_GROUP
    override val component: ItemGroup
        get() = group

    companion object {
        context(ctx: ModContext)
        fun create(name: String, icon: Item,  items: List<Item>): KItemGroup {
            val group = FabricItemGroup.builder()
                .icon { ItemStack(icon) }
                .displayName(Text.translatable("itemGroup.${ctx.modId}.$name"))
                .entries { context, entries ->
                    for (item in items) {
                        entries.add { item }
                    }
                }
                .build()
            return KItemGroup(group, name)
        }
    }

}


