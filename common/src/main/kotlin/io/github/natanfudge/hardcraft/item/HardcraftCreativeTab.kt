package io.github.natanfudge.hardcraft.item

import dev.architectury.registry.CreativeTabRegistry
import io.github.natanfudge.hardcraft.HardCraft
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier


val HardcraftItemGroup  = CreativeTabRegistry.create(Identifier(HardCraft.ModId, "hardcraft_tab")) { ItemStack(DebugCurrentHealthItem.Damage) }
