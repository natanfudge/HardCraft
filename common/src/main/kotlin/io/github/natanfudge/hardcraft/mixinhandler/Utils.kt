package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity
import net.minecraft.entity.mob.HostileEntity

val HostileEntity.demolition get() = (this as HardCraftHostileEntity).hardcraft_demolition()