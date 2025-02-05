//package io.github.natanfudge.hardcraft.client;
//
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
//import net.minecraft.client.render.BufferBuilder;
//import net.minecraft.client.render.Camera;
//import net.minecraft.client.render.Tessellator;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Box;
//
//import java.util.Map;
//
//public class test {
//    private void renderTintOverlays(WorldRenderContext context) {
//        MatrixStack matrices = context.matrixStack();
//        Camera camera = context.camera();
//        VertexConsumerProvider consumers = context.consumers();
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.getBuffer();
//
//        matrices.push();
//        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
//
//
//
//        for (Map.Entry<BlockPos, Integer> entry : positions) {
//            BlockPos pos = entry.getKey();
//            int color = entry.getValue();
//
//            if (!camera.getFrustum().isVisible(new Box(pos))) continue;
//
//            float alpha = (color >> 24 & 0xFF) / 255f;
//            float red = (color >> 16 & 0xFF) / 255f;
//            float green = (color >> 8 & 0xFF) / 255f;
//            float blue = (color & 0xFF) / 255f;
//
//            // Render a translucent overlay
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//
//            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//            WorldRenderer.drawBox(matrices, buffer,
//                    new Box(pos), red, green, blue, alpha
//            );
//            tessellator.draw();
//
//            RenderSystem.disableBlend();
//        }
//
//        matrices.pop();
//    }
//}
