package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage;
import io.github.natanfudge.hardcraft.health.MaxHealthKt;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", at = @At(value = "HEAD"))
    public void tickHook(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        //TODO: refactor out into a OnRenderTick event
//        Vec3d vec3d = camera.getPos();
//        double cameraX = vec3d.getX();
//        double cameraY = vec3d.getY();
//        double cameraZ = vec3d.getZ();
//        for (Long2IntMap.Entry currentHealthEntry : CurrentHealthStorage.getClientStorage().getAllValues().long2IntEntrySet()) {
//            BlockPos blockPos = BlockPos.fromLong(currentHealthEntry.getLongKey());
//            double h = (double) blockPos.getX() - cameraX;
//            double n = (double) blockPos.getY() - cameraY;
//            double o = (double) blockPos.getZ() - cameraZ;
//            if (!(h * h + n * n + o * o > 1024.0)) {
//                int health = currentHealthEntry.getIntValue();
//                int maxHealth = MaxHealthKt.getMaxBlockHealth()
//                matrices.push();
//                matrices.translate((double) blockPos.getX() - cameraX, (double) blockPos.getY() - cameraY, (double) blockPos.getZ() - cameraZ);
//                MatrixStack.Entry entry3 = matrices.peek();
//                VertexConsumer vertexConsumer2 = new OverlayVertexConsumer(
//                        this.bufferBuilders.getEffectVertexConsumers().getBuffer((RenderLayer) ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(p)),
//                        entry3.getPositionMatrix(),
//                        entry3.getNormalMatrix()
//                );
//                this.client.getBlockRenderManager().renderDamage(this.world.getBlockState(blockPos), blockPos, this.world, matrices, vertexConsumer2);
//                matrices.pop();
//
//            }
//        }
//
//        int originalValue = instance.getStage();
//        var damagedHealth = CurrentHealthStorage.getClientStorage().getIfDamaged(instance.getPos());
//        return originalValue;
        //TODO: retry when we don't need to specify world
//        CurrentHealthStorage.get(null,null)
    }
}
