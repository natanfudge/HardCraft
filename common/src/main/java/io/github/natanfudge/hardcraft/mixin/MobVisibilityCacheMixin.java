package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.injection.HardCraftMobVisibilityCache;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobVisibilityCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobVisibilityCache.class)
public class MobVisibilityCacheMixin implements HardCraftMobVisibilityCache {
    @Final
    @Shadow
    private MobEntity owner;
    @Final
    @Shadow
    private IntSet visibleEntities;
    @Final
    @Shadow
    private IntSet invisibleEntities;

    /**
     * @reason Make hostile mobs more threatening by allowing them to see anything within their range
     */
    @Inject(method = "canSee(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void canSeeHook(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (owner instanceof HostileEntity) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Override
    public boolean hardcraft$originalCanSee(Entity entity) {
        int i = entity.getId();
        if (this.visibleEntities.contains(i)) {
            return true;
        } else if (this.invisibleEntities.contains(i)) {
            return false;
        } else {
            this.owner.world.getProfiler().push("hasLineOfSight");
            boolean bl = this.owner.canSee(entity);
            this.owner.world.getProfiler().pop();
            if (bl) {
                this.visibleEntities.add(i);
            } else {
                this.invisibleEntities.add(i);
            }

            return bl;
        }
    }
}
