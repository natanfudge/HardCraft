package io.github.natanfudge.genericutils.platform

import dev.architectury.platform.Platform
import dev.architectury.utils.Env
import java.nio.file.Path

object PlatformHelp {
    fun isClient() = Platform.getEnvironment() == Env.CLIENT
    fun configDir(): Path = Platform.getConfigFolder()
}