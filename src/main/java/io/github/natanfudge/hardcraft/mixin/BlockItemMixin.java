package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.health.MaxHealthKt;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
