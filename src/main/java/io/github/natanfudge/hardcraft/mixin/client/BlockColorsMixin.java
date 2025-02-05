package io.github.natanfudge.hardcraft.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

//@Mixin(BlockModelRenderer.class)
@Mixin(BlockModelRenderer.class)
public class BlockColorsMixin {
//    @Inject(
//            method = "getColor",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void applyTint(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
//        if (pos == null || world == null) return;
//        cir.setReturnValue(0xFFFF0000);
//
////        int tintColor = TintManager.getColor(pos);
////        if (tintColor != -1) {
////            cir.setReturnValue(tintColor); // Override any existing color
////        }
//    }

//TODO: this is only for debugging and should be turned off in production, because it does boxing for every render call.

//    @ModifyArgs(
//            method = "renderQuad",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFF[IIZ)V"
//            )
//    )
//    private void forceRedColor(Args args) {
//        // Parameters: (MatrixStack.Entry, BakedQuad, float[] brightnesses,
//        //             float red, float green, float blue, int[] lights,
//        //             int overlay, boolean useQuadColorData)
//
//        // Set RGB values to red (keep original alpha brightness)
//        args.set(3, 1.0f);  // Red channel
//        args.set(4, 0.0f);  // Green channel
//        args.set(5, 0.0f);  // Blue channel
//    }
}
