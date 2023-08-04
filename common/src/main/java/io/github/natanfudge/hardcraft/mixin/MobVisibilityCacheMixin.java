package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobVisibilityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobVisibilityCache.class)
public class MobVisibilityCacheMixin {
    @Inject(method = "Lnet/minecraft/entity/mob/MobVisibilityCache;canSee(Lnet/minecraft/entity/Entity;)Z" ,at = @At("HEAD"), cancellable = true)
    public void canSeeHook(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }
}
