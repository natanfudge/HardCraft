package io.github.natanfudge.hardcraft.rotm

object RevengeOfTheMobs {
    val config = RotmConfig.config
    fun init() {
        RotmSpawner.init()
    }
    fun initClient() {
        RotmHud.initClient()
    }
}

//TODO: Something nicer than that luck effect