package io.github.natanfudge.hardcraft

import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.encodeToNbt
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
@Serializable
data class Amar(val x: Int, val y: String)

object HardCraft {
    const val Name = "HardCraft"
    const val ModId = "hardcraft"
    val logger: Logger = LogManager.getLogger(Name)

    fun init() {
        val toNbt = Nbt.encodeToNbt(Amar(1,"Asdf"))
        println("Halo Hardcraft! my nbt is ${toNbt}")
    }
}
