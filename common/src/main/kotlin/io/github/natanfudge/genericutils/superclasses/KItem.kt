package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.Registerable
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Rarity
import kotlin.math.max

abstract class KItem(
    override val idPath: String,
    maxCount: Int = 64,
    maxDamage: Int = 0,
    recipeRemainder: Item? = null,
    group: ItemGroup? = null,
    rarity: Rarity? = Rarity.COMMON,
    foodComponent: FoodComponent? = null,
    fireproof: Boolean = false
) : Item(
    Settings()
        .rarity(rarity)
        .apply { if(fireproof) fireproof()}
        .maxCount(maxCount)
        .maxDamage(maxDamage)
        .recipeRemainder(recipeRemainder)
        .group(group)
        .food(foodComponent)

), Registerable {
}