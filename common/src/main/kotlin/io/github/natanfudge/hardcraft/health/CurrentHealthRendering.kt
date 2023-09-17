package io.github.natanfudge.hardcraft.health

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

object CurrentHealthRendering {
    /**
     * Render block damage in the same way minecraft does it, but for the CurrentHealth system.
     */
    fun render(camera: Camera, world: World, matrices: MatrixStack, bufferBuilders: BufferBuilderStorage, client: MinecraftClient) {
        // This is pretty much copy-pasted from Minecraft with slighty changes
        val cameraPos = camera.pos
        val cameraX = cameraPos.getX()
        val cameraY = cameraPos.getY()
        val cameraZ = cameraPos.getZ()

        // Custom logic
        for (currentHealthEntry in CurrentHealthStorage.getClientStorage().allValues.long2IntEntrySet()) {
            val blockPos = BlockPos.fromLong(currentHealthEntry.longKey)

            val xCameraDistance = blockPos.x - cameraX
            val yCameraDistance = blockPos.y - cameraY
            val zCameraDistance = blockPos.z - cameraZ
            val distanceOfBlockFromCamera = xCameraDistance.squared() + yCameraDistance.squared() + zCameraDistance.squared()
            if (distanceOfBlockFromCamera > 1024.0) continue

            // Custom logic
            val health = currentHealthEntry.intValue
            val maxHealth: Int = world.getMaxBlockHealthOrMinus1(blockPos)
            if (maxHealth == -1) continue


            val stage = (10 - health.toFloat() / maxHealth * 10).toInt() /*6*/
            if (stage == 0 || stage >= 10) continue
            matrices.push()
            matrices.translate(blockPos.x.toDouble() - cameraX, blockPos.y.toDouble() - cameraY, blockPos.z.toDouble() - cameraZ)
            val entry3: MatrixStack.Entry = matrices.peek()
            val vertexConsumer2: VertexConsumer = OverlayVertexConsumer(
                bufferBuilders.effectVertexConsumers.getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS[stage]),
                entry3.positionMatrix,
                entry3.normalMatrix
            )
            client.blockRenderManager.renderDamage(world.getBlockState(blockPos), blockPos, world, matrices, vertexConsumer2)
            matrices.pop()
        }
    }
}