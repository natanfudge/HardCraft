package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    /**
     * @reason Implements {@link io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity#hardcraft_setIsPushedByFluids(boolean)}
     * So we can make mobs resilient to being pushed by water
     */
    @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
    private void allowDisablingPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof HostileEntity hostileEntity) {
            cir.setReturnValue(hostileEntity.hardcraft_getIsPushedByFluids());
        }
    }
}
