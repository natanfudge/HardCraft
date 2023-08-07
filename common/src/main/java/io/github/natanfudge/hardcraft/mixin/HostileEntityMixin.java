package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.BreakBlockGoal;
import io.github.natanfudge.hardcraft.ai.HardCraftNavigation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {
    @Unique
    HostileEntity hardCraft$self = (HostileEntity) ((Object) this);

    /**
     * @reason Make mobs more threatening by allowing them to break blocks that block their way
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    public void constructorHookAfterGoalSelectorInitialized(EntityType<?> entityType, World world, CallbackInfo ci) {
        if (world != null && !world.isClient) {
            hardCraft$self.goalSelector.add(0, new BreakBlockGoal(hardCraft$self));
        }
        hardCraft$self.navigation = new HardCraftNavigation(hardCraft$self, world, hardCraft$self.navigation);
    }
}
