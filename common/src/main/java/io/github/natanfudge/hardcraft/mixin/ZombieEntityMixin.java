package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.BreakBlockGoal;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin {
    @Inject(method = "initGoals()V", at = @At("HEAD"), cancellable = true)
    private void initGoalsHook(CallbackInfo ci) {
        ci.cancel();
        @SuppressWarnings("DataFlowIssue")
        var thisValue = ((ZombieEntity)(Object)this);
        thisValue.goalSelector.add(0, new BreakBlockGoal(thisValue));
    }
}
