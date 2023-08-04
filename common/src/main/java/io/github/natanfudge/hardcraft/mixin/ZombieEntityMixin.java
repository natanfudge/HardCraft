package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.BreakBlockGoal;
import io.github.natanfudge.hardcraft.ai.ControlZombieGoal;
import io.github.natanfudge.hardcraft.ai.HardCraftNavigation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("HEAD"), cancellable = true)
    private void initGoalsHook(CallbackInfo ci) {
        ci.cancel();
        getThis().goalSelector.add(0, new ControlZombieGoal(getThis()));
        getThis().goalSelector.add(0, new BreakBlockGoal(getThis()));
//       thisValue.getNavigation().time
        // TODO: should be applied on every entity if it can jump. The jump mechanics are just bad
        getThis().stepHeight = 1.0f;
    }

    private ZombieEntity getThis() {
        return (ZombieEntity) (Object)this;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new HardCraftNavigation(getThis(), world);
    }
}
