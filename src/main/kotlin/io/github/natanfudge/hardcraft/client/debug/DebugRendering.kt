package io.github.natanfudge.hardcraft.client.debug

import com.mojang.blaze3d.systems.RenderSystem
import io.github.natanfudge.genericutils.client.ClientInit
import io.github.natanfudge.genericutils.network.s2cPacket
import io.github.natanfudge.hardcraft.HardCraft
import io.github.natanfudge.hardcraft.client.McColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.BlockPosSerializer
import kotlinx.serialization.minecraft.Vec3dSerializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import kotlin.time.Duration
import kotlin.time.TimeSource


@Serializable
private data class Tint(
    val color: McColor?,
    @Serializable(with = BlockPosSerializer::class)
    val pos: BlockPos,
    val lifetime: Duration?
)

@Serializable
private data class DebugText(
    val text: String,
    @Serializable(with = Vec3dSerializer::class)
    val pos: Vec3d,
    /**
     * Used to identify this specific text so it may be turned off
     */
    val id: DebugTextId,
    val lifetime: Duration?
)

object DebugRendering : HardCraft.Context() {
    private val worldMarkers = mutableMapOf<World, WorldDebugMarkers>()

    private val sendTintPacket = s2cPacket<Tint>("debug_tint")
    private val addTextPacket = s2cPacket<DebugText>("add_debug_text")
    private val removeTextPacket = s2cPacket<DebugTextId>("remove_debug_text")
    private var nextTextId = 0

    context(ctx: ClientInit)
    fun registerClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register {
            worldMarkers[it.world()]?.render(it)
        }

        sendTintPacket.register { content, context ->
            if (context.world != null) {
                if (content.color != null) {
                    tintBlock(context.world, content.pos, content.color, content.lifetime)
                } else {
                    untintBlock(context.world, content.pos)
                }
            }
        }
        addTextPacket.register { content, context ->
            if (context.world != null) {
                addText(context.world, content.pos, content.text, content.id, content.lifetime)
            }
        }
        removeTextPacket.register { content, context ->
            if (context.world != null) {
                removeText(context.world, TextHandle(content))
            }
        }
    }

    /**
     * Will tint the block at [pos] with [color].
     * Can be used on the client to tint right away, or on the server to send the tint to the client.
     */
    fun tintBlock(world: World, pos: BlockPos, color: McColor, time: Duration?) {
        if (world is ServerWorld) {
            sendTintPacket.sendToWorld(Tint(color, pos, time), world)
        } else {
            worldMarkers.computeIfAbsent(world) { WorldDebugMarkers() }.tint(pos, color, time)
        }

    }

    fun untintBlock(world: World, pos: BlockPos) {
        if (world is ServerWorld) {
            sendTintPacket.sendToWorld(Tint(null, pos, null), world)
        } else {
            worldMarkers[world]?.untint(pos)
        }
    }

    /**
     * @param time If not null, the text will expire and be removed after the specified amount of time.
     */
    fun addText(world: World, pos: Vec3d, text: String, id: DebugTextId = nextTextId++, time: Duration? = null): TextHandle {
        if (world is ServerWorld) {
            val handle = TextHandle(id)
            addTextPacket.sendToWorld(DebugText(text, pos, id, time), world)
            return handle
        } else {
            return worldMarkers.computeIfAbsent(world) { WorldDebugMarkers() }.addText(pos, text, id, time)
        }
    }

    fun removeText(world: World, handle: TextHandle) {
        if (world is ServerWorld) {
            removeTextPacket.sendToWorld(handle.id, world)
        } else {
            worldMarkers[world]?.removeText(handle)
        }
    }

}

typealias DebugTextId = Int

class TextHandle(val id: DebugTextId)


private data class TimestampedDebugText(
    val addTime:  TimeSource.Monotonic.ValueTimeMark,
    val debugText: DebugText
)

private data class TimestampedTint(
    val addTime: TimeSource.Monotonic.ValueTimeMark,
    val tint: Tint
)

class WorldDebugMarkers {
    private val tints = mutableMapOf<BlockPos, TimestampedTint>()
    private val texts = mutableMapOf<DebugTextId, TimestampedDebugText>()

    fun addText(pos: Vec3d, text: String, id: DebugTextId, lifetime: Duration?): TextHandle {
        this.texts[id] =TimestampedDebugText(TimeSource.Monotonic.markNow(), DebugText(text, pos, id, lifetime))
        return TextHandle(id)
    }

    fun removeText(handle: TextHandle) {
        texts.remove(handle.id)
    }

    fun untint(pos: BlockPos) {
        tints.remove(pos)
    }

    fun tint(pos: BlockPos, color: McColor, lifetime: Duration?) {
        tints[pos] = TimestampedTint(TimeSource.Monotonic.markNow(), Tint(color, pos, lifetime))
    }

    fun render(context: WorldRenderContext) {
        // Remove expired debug markers
        tints.values.removeIf { it.tint.lifetime != null  && it.addTime.elapsedNow() >= it.tint.lifetime }
        texts.values.removeIf { it.debugText.lifetime != null  && it.addTime.elapsedNow() >= it.debugText.lifetime }
        tintBlocks(context)
        for ((_, debug) in texts.values) {
            drawText(context, debug.text, debug.pos)
        }
    }

    private fun drawText(ctx: WorldRenderContext, text: String, pos: Vec3d) {
        val matrices: MatrixStack = ctx.matrixStack()
        val cam: Camera = ctx.camera()
        val camPos = cam.getPos()


        // Translate from world → camera space
        matrices.push()
        matrices.translate(
            pos.getX() + 0.5 - camPos.x,
            pos.getY() + 1.5 - camPos.y,
            pos.getZ() + 0.5 - camPos.z
        )


        // Make the label face the player and scale it down
        matrices.multiply(cam.getRotation()) // billboard
        matrices.scale(-0.025f, -0.025f, 0.025f) // 40 px ≈ 1 block

        val tr: TextRenderer = MinecraftClient.getInstance().textRenderer
        val wHalf: Float = tr.getWidth(text) / 2f
        matrices.translate(-wHalf, 0f, 0f) // centre horizontally

        val vcp =
            VertexConsumerProvider.immediate(
                Tessellator.getInstance().getBuffer()
            )

        tr.draw(
            text, 0f, 0f,
            0xFFFFFFFF.toInt(),  // colour
            false,  // no shadow
            matrices.peek().getPositionMatrix(),
            vcp,
            TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )

        vcp.draw() // flush
        matrices.pop()
    }

    private fun tintBlocks(context: WorldRenderContext) {
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
            val color = color.tint.color!!
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