package io.github.natanfudge.hardcraft.client.health

import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.health.CurrentHealthStorage
import io.github.natanfudge.hardcraft.health.getMaxBlockHealthOrMinus1
import io.github.natanfudge.hardcraft.utils.squared
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.Camera
import net.minecraft.client.render.OverlayVertexConsumer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.collections.get

object CurrentHealthRendering {
    /**
     * Render block damage in the same way minecraft does it, but for the CurrentHealth system.
     */
    fun render(camera: Camera, world: World, matrices: MatrixStack, bufferBuilders: BufferBuilderStorage, client: MinecraftClient) {
        val storage = CurrentHealthStorage.Companion.getClientStorage() ?: return
        // This is pretty much copy-pasted from Minecraft with slighty changes
        val cameraPos = camera.pos
        val cameraX = cameraPos.getX()
        val cameraY = cameraPos.getY()
        val cameraZ = cameraPos.getZ()

        for (currentHealthEntry in storage.allValues.long2IntEntrySet()) {
            val blockPos = BlockPos.fromLong(currentHealthEntry.longKey)

            val xCameraDistance = blockPos.x - cameraX
            val yCameraDistance = blockPos.y - cameraY
            val zCameraDistance = blockPos.z - cameraZ
            val distanceOfBlockFromCamera = xCameraDistance.squared() + yCameraDistance.squared() + zCameraDistance.squared()
            if (distanceOfBlockFromCamera > 1024.0) continue

            // Custom logic
            val maxHealth: Int = world.getMaxBlockHealthOrMinus1(blockPos)
            if (maxHealth == -1) continue
            val health = currentHealthEntry.intValue.coerceAtMost(maxHealth)

            val stage = (10 - health.toFloat() / maxHealth * 10).toInt() /*6*/
            if (stage == -1) {
                HardCraft.Logger.error("Unexpected stage = -1, bad health value")
                continue
            }
            if (stage == 0 || stage >= 10) continue
            matrices.push()
            matrices.translate(blockPos.x.toDouble() - cameraX, blockPos.y.toDouble() - cameraY, blockPos.z.toDouble() - cameraZ)
            val entry3: MatrixStack.Entry = matrices.peek()
            val vertexConsumer2: VertexConsumer = OverlayVertexConsumer(
                bufferBuilders.effectVertexConsumers.getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS[stage]),
                entry3.positionMatrix,
                entry3.normalMatrix,
                1.0f
            )
            client.blockRenderManager.renderDamage(world.getBlockState(blockPos), blockPos, world, matrices, vertexConsumer2)
            matrices.pop()
        }
    }
}