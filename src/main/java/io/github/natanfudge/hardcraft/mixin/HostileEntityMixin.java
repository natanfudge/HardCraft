package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.HardCraftNavigation;
import io.github.natanfudge.hardcraft.ai.ReachTargetGoal;
import io.github.natanfudge.hardcraft.injection.HardCraftHostileEntity;
import io.github.natanfudge.hardcraft.mixinhandler.MobHooks;
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

@Mixin(HostileEntity.class)
public class HostileEntityMixin implements HardCraftHostileEntity {


    @Unique
    boolean cantReachTarget = false;

    @Unique
    boolean hardcraft_isPushedByFluids = true;

    @Unique
    boolean hardcraft_isFireImmune = false;
    @Unique
    boolean hardcraft_isPistonImmune = false;

    @Unique
    int hardcraft_demolition = 5;


    /**
     * @reason Make mobs more threatening by allowing them to break blocks that block their way
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    public void constructorHookAfterGoalSelectorInitialized(EntityType<?> entityType, World world, CallbackInfo ci) {
        HostileEntity self = (HostileEntity) (Object) this;
        if (world != null && !world.isClient) {
            self.goalSelector.add(0, new ReachTargetGoal(self));
            if (MobHooks.INSTANCE.getHateVillagers()) {
                // It's often useful for testing to make mobs attack villagers, so we have a flag that makes them attack villagers.
                self.goalSelector.add(3, new ActiveTargetGoal<>(self, VillagerEntity.class, true));
            }
        }
        self.navigation = new HardCraftNavigation(self, world, self.navigation);
    }

    /**
     * Allow individual mobs to specify at what rate they destroy blocks
     */
    @Override
    public int hardcraft_getDemolition() {
        return hardcraft_demolition;
    }

    @Override
    public void hardcraft_setDemolition(int damagePerTick) {
        this.hardcraft_demolition = damagePerTick;
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

    @Override
    public void hardcraft_setIsFireImmune(boolean value) {
        this.hardcraft_isFireImmune = value;
    }

    @Override
    public boolean hardcraft_getIsFireImmune() {
        return this.hardcraft_isFireImmune;
    }
    @Override
    public void hardcraft_setIsPistonImmune(boolean hardcraft_isPistonImmune) {
        this.hardcraft_isPistonImmune = hardcraft_isPistonImmune;
    }

    @Override
    public boolean hardcraft_getIsPistonImmune() {
        return this.hardcraft_isPistonImmune;
    }
}
