package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.block.piston.PistonBehavior;
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

    /**
     * @reason Implements {@link io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity#hardcraft_setIsFireImmune(boolean)}
     * So we can make mobs resilient to fire
     */
    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void allowGivingFireImmunity(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof HostileEntity hostileEntity && hostileEntity.hardcraft_getIsFireImmune()) {
            cir.setReturnValue(true);
        }

    }
    /**et
     * @reason Implements {@link io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity#hardcraft_setIsPistonImmune(boolean)}
     * So we can make mobs resilient to fire
     */
    @Inject(method = "getPistonBehavior", at = @At("HEAD"), cancellable = true)
    private void allowGivingPistonImmunity(CallbackInfoReturnable<PistonBehavior> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof HostileEntity hostileEntity && hostileEntity.hardcraft_getIsPistonImmune()) {
            cir.setReturnValue(PistonBehavior.IGNORE);
        }
    }
}

