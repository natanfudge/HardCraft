package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ActiveTargetGoal.class)
public class ActiveTargetGoalMixin {

    /**
     * @reason Make mobs search for enemies in a further Y range equal to their search distance, Minecraft hardcodes the Y range to 4.
     */
    @Inject(method = "getSearchBox(D)Lnet/minecraft/util/math/Box;", at = @At("HEAD"), cancellable = true)
    protected void extendSearchBoxY(double distance, CallbackInfoReturnable<Box> cir) {
        var self = ((ActiveTargetGoal) (Object) this);
        // Y = distance instance of y = 4
        cir.setReturnValue(self.mob.getBoundingBox().expand(distance, distance, distance));
    }
}
