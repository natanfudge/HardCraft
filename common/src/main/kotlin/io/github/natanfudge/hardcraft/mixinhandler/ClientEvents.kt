package io.github.natanfudge.hardcraft.mixinhandler

import io.github.natanfudge.hardcraft.health.CurrentHealthRendering
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.Camera
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.world.World

object ClientEvents {
    /**
     * Runs when the client is drawing the world's block's breaking progress overlay.
     * Here you should add rendering that is specific to the world, specifically block breaking overlays.
     */
    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun onRenderBlockBreakProgress(camera: Camera, world: World, matrices: MatrixStack, bufferBuilders: BufferBuilderStorage, client: MinecraftClient) {
        CurrentHealthRendering.render(camera, world, matrices, bufferBuilders, client)
    }
}

