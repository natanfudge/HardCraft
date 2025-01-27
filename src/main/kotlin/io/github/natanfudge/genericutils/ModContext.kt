package io.github.natanfudge.genericutils

import net.minecraft.util.Identifier

interface ModContext {
    /**
     * Allows easily making a class implement ModContext
     * This should generally be used once per mod to define the mod's ModContext superclass
     */
    abstract class Superclass(override val modId: String) : ModContext
    val modId: String
}

 fun ModContext(modId: String) = object: ModContext {
    override val modId: String = modId
}

context (ModContext)
fun modId(path: String) = Identifier(modId, path)