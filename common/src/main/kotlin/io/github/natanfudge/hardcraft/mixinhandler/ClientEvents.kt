package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthRendering
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage.Companion.getClientStorage
import io.github.natanfudge.hardcraft.health.getMaxBlockHealthOrMinus1
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.Camera
import net.minecraft.client.render.OverlayVertexConsumer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ClientEvents {
    /**
     * Runs when the client is drawing the world
     * Here you should add rendering that is specific to the world.
     */
    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun onWorldRendererTick(camera: Camera, world: World, matrices: MatrixStack, bufferBuilders: BufferBuilderStorage, client: MinecraftClient) {
        CurrentHealthRendering.render(camera, world, matrices, bufferBuilders, client)
    }
}

inline fun Double.squared() = this * this