package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.injection.HardCraftMobVisibilityCache;
import io.github.natanfudge.hardcraft.mixinhandler.RangedAttackGoals;
import io.github.natanfudge.hardcraft.utils.TickThrottler;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(GoalSelector.class)
public class GoalSelectorMixin {
    @Shadow
    @Final
    private Set<PrioritizedGoal> goals;
    @Unique
    TickThrottler hardcraft$tickThrottler = new TickThrottler();

    @Redirect(method = "tickGoals(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/PrioritizedGoal;tick()V"))
    public void redirectTickGoalsTick(PrioritizedGoal instance) {
        var goal = instance.getGoal();
        MobEntity rangedMob = RangedAttackGoals.getRangedAttackGoalActor(goal);
        // Only relevant for ranged mobs
        if (rangedMob != null) {
            var target = rangedMob.getTarget();
            // If ranged mobs can't see their target, don't shoot.
            if (target != null) {
                var visibilityCache = (HardCraftMobVisibilityCache) rangedMob.getVisibilityCache();
                if (!visibilityCache.hardcraft$originalCanSee(target)) {
                    hardcraft$tickThrottler.runThrottled(20, () -> {
                        // Instead, try to walk up to the target.
                        rangedMob.navigation.startMovingTo(target, RangedAttackGoals.getRangedAttackGoalSpeed(goal));
                        return null;
                    });
                    return;
                }
            }
        }
        instance.tick();
    }
}
