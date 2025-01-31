package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.mixinhandler.BlockEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public class WorldMixin {
    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    private void onBlockReplaced(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        BlockEvents.onBlockReplaced((ServerWorld)(Object)this, pos, oldBlock, newBlock);
    }
}
