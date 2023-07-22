package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.health.CurrentHealthStorage;
import io.github.natanfudge.hardcraft.ai.BreakBlockGoal;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onUse(" +
            "Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
            "Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;" +
            ")Lnet/minecraft/util/ActionResult;", at = @At("HEAD"))
    public void onUseHook(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {
        var currentHealth = CurrentHealthStorage.get(world, pos);
        CurrentHealthStorage.set(world, pos, currentHealth - 50);
        BreakBlockGoal.setTargetPos(pos);
        System.out.println("Setting health value in world " + world);
    }
}
