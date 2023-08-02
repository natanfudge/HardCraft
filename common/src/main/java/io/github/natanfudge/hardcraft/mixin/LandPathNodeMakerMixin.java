package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//TODO: on onActive
@Mixin(LandPathNodeMaker.class)
public class LandPathNodeMakerMixin {
    @Inject(method = "getLandNodeType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos$Mutable;)Lnet/minecraft/entity/ai/pathing/PathNodeType;", at = @At("RETURN"), cancellable = true)
    private static void getLandTypeHook(BlockView world, BlockPos.Mutable pos, CallbackInfoReturnable<PathNodeType> cir) {
        var type = cir.getReturnValue();
//        var type = LandPathNodeMaker.getCommonNodeType(world, pos);
        if(type.getDefaultPenalty() < 0) {
            type = PathNodeType.DANGER_FIRE;
        }
        cir.setReturnValue(type);
        //TODO: make this more specific and less intrusive
        //TODO: see how this works with stairs
//        cir.setReturnValue(PathNodeType.WALKABLE);
    }
}
