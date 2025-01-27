package io.github.natanfudge.genericutils

import com.google.common.base.Suppliers
import dev.architectury.registry.registries.Registries

/**
 * Functions that should only be called in CommonInit should be marked as context(CommonInit).
 * Then the mod's onInitialize() override should do with(CommonInit).
 */
class CommonInit(private val context: ModContext) : ModContext by context

inline fun commonInit(modId: String, init: context(CommonInit) () -> Unit) {
    CommonInit(ModContext(modId)).apply(init)
}