package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.ReachTargetGoal;
import io.github.natanfudge.hardcraft.ai.HardCraftNavigation;
import io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class HostileEntityMixin implements HardCraftHostileEntity {

    @Unique
    boolean cantReachTarget = false;

    //TODO: should be true by default, this is just a test
    @Unique
    boolean hardcraft_isPushedByFluids = false;


    /**
     * @reason Make mobs more threatening by allowing them to break blocks that block their way
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    public void constructorHookAfterGoalSelectorInitialized(EntityType<?> entityType, World world, CallbackInfo ci) {
        HostileEntity self = (HostileEntity)(Object)this;
        if (world != null && !world.isClient) {
            self.goalSelector.add(0, new ReachTargetGoal(self));
            //TODO: make this optional via API
            self.goalSelector.add(3, new ActiveTargetGoal<>(self, VillagerEntity.class, true));
        }
        self.navigation = new HardCraftNavigation(self, world, self.navigation);
    }

    /**
     * Allow individual mobs to specify at what rate they destroy blocks
     */
    @Override
    public int hardcraft_demolition() {
        return 5;
    }

    @Override
    public void hardcraft_setCantReachTarget(boolean value) {
        this.cantReachTarget = value;
    }

    @Override
    public boolean hardcraft_getCantReachTarget() {
        return cantReachTarget;
    }


    @Override
    public void hardcraft_setIsPushedByFluids(boolean value) {
        this.hardcraft_isPushedByFluids = value;
    }

    @Override
    public boolean hardcraft_getIsPushedByFluids() {
        return this.hardcraft_isPushedByFluids;
    }
}
