package io.github.natanfudge.hardcraft.client.debug

import com.mojang.blaze3d.systems.RenderSystem
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.network.s2cPacket
import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.client.McColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.BlockPosSerializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

@Serializable
private data class Tint(
    val color: McColor?,
    @Serializable(with = BlockPosSerializer::class)
    val pos: BlockPos,
)


object DebugRendering : HardCraft.Context() {
    private val worldTints = mutableMapOf<World, WorldTints>()

    private val sendTintPacket = s2cPacket<Tint>("debug_tint")

    context(ClientInit)
    fun registerClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register {
            worldTints[it.world()]?.render(it)
        }

        sendTintPacket.register { content, context ->
            if (context.world != null) {
                if (content.color != null) {
                    tint(context.world, content.pos, content.color)
                } else {
                    untint(context.world, content.pos)
                }
            }
        }
    }

    /**
     * Will tint the block at [pos] with [color].
     * Can be used on the client to tint right away, or on the server to send the tint to the client.
     */
    fun tint(world: World, pos: BlockPos, color: McColor) {
        if (world is ServerWorld) {
            sendTintPacket.sendToWorld(Tint(color, pos), world)
        } else {
            worldTints.computeIfAbsent(world) { WorldTints(world) }.tint(pos, color)
        }

    }

    fun untint(world: World, pos: BlockPos) {
        if (world is ServerWorld) {
            sendTintPacket.sendToWorld(Tint(null, pos), world)
        } else {
            worldTints[world]?.untint(pos)
        }
    }
}

class WorldTints(world: World) {
    private val tints = mutableMapOf<BlockPos, McColor>()

    fun untint(pos: BlockPos) {
        tints.remove(pos)
    }

    fun tint(pos: BlockPos, color: McColor) {
        tints[pos] = color
    }

    fun render(context: WorldRenderContext) {
        val matrices = context.matrixStack()
        val camera = context.camera()
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        matrices.push()
        matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.polygonOffset(-5f, -5f)
        RenderSystem.enablePolygonOffset()
        RenderSystem.disableCull() // Important for seeing all faces

        // Configure depth testing
        RenderSystem.depthMask(false)
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL11.GL_LEQUAL)

        for ((pos, color) in tints) {
            val colorValue = color.argb
//            if (!camera.frustum.isVisible(Box(pos))) continue

            val alpha = (colorValue shr 24 and 0xFF) / 255f
            val red = (colorValue shr 16 and 0xFF) / 255f
            val green = (colorValue shr 8 and 0xFF) / 255f
            val blue = (colorValue and 0xFF) / 255f

            // Create slightly expanded box in all directions
            val box = Box(
                pos.x - OFFSET,
                pos.y - OFFSET,
                pos.z - OFFSET,
                pos.x + 1 + OFFSET,
                pos.y + 1 + OFFSET,
                pos.z + 1 + OFFSET
            )

            matrices.push()
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

            // Manually draw all 6 faces with proper normals
            listOf(
                Direction.DOWN, Direction.UP,
                Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST
            ).forEach { dir ->
                val faceVertices = getFaceVertices(box, dir)
                faceVertices.forEach { vec ->
                    buffer.vertex(matrices.peek().positionMatrix, vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())
                        .color(red, green, blue, alpha)
                        .next()
                }
            }

            tessellator.draw()
            matrices.pop()
        }

        // Cleanup GL state
        RenderSystem.depthMask(true)
        RenderSystem.disablePolygonOffset()
        RenderSystem.polygonOffset(0f, 0f)
        RenderSystem.enableCull()
        RenderSystem.disableBlend()
        matrices.pop()
    }
}

//val positions = listOf(
//    BlockPos(-30, 83, -14) to 0xDF1F0000
//)

private const val OFFSET = 0.000 // Small offset to prevent z-fighting


private fun getFaceVertices(box: Box, face: Direction): List<Vec3d> {
    return when (face) {
        Direction.DOWN -> listOf(
            Vec3d(box.minX, box.minY, box.minZ),
            Vec3d(box.maxX, box.minY, box.minZ),
            Vec3d(box.maxX, box.minY, box.maxZ),
            Vec3d(box.minX, box.minY, box.maxZ)
        )

        Direction.UP -> listOf(
            Vec3d(box.minX, box.maxY, box.minZ),
            Vec3d(box.minX, box.maxY, box.maxZ),
            Vec3d(box.maxX, box.maxY, box.maxZ),
            Vec3d(box.maxX, box.maxY, box.minZ)
        )

        Direction.NORTH -> listOf(
            Vec3d(box.minX, box.minY, box.minZ),
            Vec3d(box.minX, box.maxY, box.minZ),
            Vec3d(box.maxX, box.maxY, box.minZ),
            Vec3d(box.maxX, box.minY, box.minZ)
        )

        Direction.SOUTH -> listOf(
            Vec3d(box.minX, box.minY, box.maxZ),
            Vec3d(box.maxX, box.minY, box.maxZ),
            Vec3d(box.maxX, box.maxY, box.maxZ),
            Vec3d(box.minX, box.maxY, box.maxZ)
        )

        Direction.WEST -> listOf(
            Vec3d(box.minX, box.minY, box.minZ),
            Vec3d(box.minX, box.minY, box.maxZ),
            Vec3d(box.minX, box.maxY, box.maxZ),
            Vec3d(box.minX, box.maxY, box.minZ)
        )

        Direction.EAST -> listOf(
            Vec3d(box.maxX, box.minY, box.minZ),
            Vec3d(box.maxX, box.maxY, box.minZ),
            Vec3d(box.maxX, box.maxY, box.maxZ),
            Vec3d(box.maxX, box.minY, box.maxZ)
        )
    }
}