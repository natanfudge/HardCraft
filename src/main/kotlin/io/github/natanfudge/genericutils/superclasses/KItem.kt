package io.github.natanfudge.genericutils.superclasses

import io.github.natanfudge.genericutils.Registerable
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Rarity

abstract class KItem(
    final override val idPath: String,
    maxCount: Int = 64,
    maxDamage: Int = 0,
    recipeRemainder: Item? = null,
    rarity: Rarity? = Rarity.COMMON,
    foodComponent: FoodComponent? = null,
    fireproof: Boolean = false
) : Item(
    Settings()
        .rarity(rarity)
        .apply { if (fireproof) fireproof() }
        .maxCount(maxCount)
        .maxDamage(maxDamage)
        .recipeRemainder(recipeRemainder)
        .food(foodComponent)

), Registerable<Item> {

    final override val registry: Registry<Item> = Registries.ITEM
    final override val component: Item
        get() = this

    @Deprecated("ItemGroups are no longer a property of items. ItemGroups should simply add the items they want.")
    constructor(
        idPath: String,
        maxCount: Int = 64,
        maxDamage: Int = 0,
        recipeRemainder: Item? = null,
        group: ItemGroup? = null,
        rarity: Rarity? = Rarity.COMMON,
        foodComponent: FoodComponent? = null,
        fireproof: Boolean = false
    ) : this(idPath, maxCount, maxDamage, recipeRemainder, rarity, foodComponent, fireproof)
}