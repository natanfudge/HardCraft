package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.health.MaxHealthKt;
import io.github.natanfudge.hardcraft.mixinhandler.BlockEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    /**
     * @reason Any ItemStack of a block that is damaged should show as damaged. We set the "max damage" to the max health of the block.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void setMaxDamageOnInit(CallbackInfo ci) {
        var self = (Item) (Object) this;
        int value = self.getMaxDamage();

        if (value == 0) {
            var maxDamage = MaxHealthKt.getBlockMaxHealth(self);
            if (maxDamage != null) {
                self.maxDamage = maxDamage;
            }
        }
    }

    /**
     * @reason We need blocks to retain their damage value when placed.
     * We make sure to inject early enough to not lose the content of the placed stack.
     */
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/event/GameEvent$Emitter;)V"))
    private void setBlockDamageOnPlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        BlockEvents.onBlockPlacedByItemStack(context);
    }

}
