package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.ai.goal.BowAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


//TODO: current status of ranged problem.
// Solution 1: Mixin into all different ranged attack goals and set the range value so that when there is no actual path they will move to target.
// Solution 2: Prevent Ranged attack goals from running and instead add a goal that shoots anything that blocks in between.
// Solution 3: Prevent ranged attack goals - this may cause the mobs to simply walk up to the player which is what we want.
//  - If they won't go themselves, add a simple goal that walks up to the target.
// All 3 solutions require have a special Mob


// Ultimate solution:
//  - Check if mob can actually see. If true, use vanilla behavior.
//  - If mob can't see, check if a path exists. If exists, use vanilla behavior to walk up to the enemy until vision exists.
//  - If mob can't see and no path exists - shoot anything between mob and target to destroy it.
// -    Since this complicates things a lot and makes it harder to use vanilla behavior for pathing, at the start only make them destroy things by hand.
// - So basically just walk up as usual and HardCraftNavigation will take care of it.
// - Need to see if we need to explicitly tell the mob to go to target if there is no ranged attack task.

@Mixin(BowAttackGoal.class)
public class BowAttackGoalMixin {

    /**
     * Only shoot when a line of sight exists.
     * Because of our modification (MobVisibilyCacheMixin) to make all mobs see everything, we need to separate between shooting and find enemies.
     */
//    @Redirect(method = "tick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/BowAttackGoal;squaredRange:F"))
//    public float redirectTickRangeCheck(BowAttackGoal<?> instance) {
//        return 0;
//    }
}
