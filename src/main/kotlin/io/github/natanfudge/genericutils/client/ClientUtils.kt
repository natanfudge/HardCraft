package io.github.natanfudge.genericutils.client

import net.minecraft.client.MinecraftClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

fun getClient(): MinecraftClient = MinecraftClient.getInstance()

