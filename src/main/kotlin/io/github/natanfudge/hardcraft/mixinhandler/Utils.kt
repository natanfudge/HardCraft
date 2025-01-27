package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity
import net.minecraft.entity.mob.HostileEntity

/**
 * Specifies how easily a mob destroys blocks.
 * Demolition is interpreted as damage per tick. So 2 demolition is doing 40 damage per second.
 */
val HostileEntity.demolition get() = (this as HardCraftHostileEntity).hardcraft_demolition()