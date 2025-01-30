package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.mixinhandler.ItemDrop;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(LootPool.class)
class LootPoolMixin {
    /**
     * @reason We need to make blocks drop damaged items if they have been damaged before breaking.
     * There's a lot of possible places to inject this, this one should cover all cases.
     */
    @Redirect(
            method = "addGeneratedLoot(Ljava/util/function/Consumer;Lnet/minecraft/loot/context/LootContext;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/loot/LootPool;supplyOnce(Ljava/util/function/Consumer;Lnet/minecraft/loot/context/LootContext;)V")
    )
    public void redirectItemLoot(LootPool instance, Consumer<ItemStack> lootConsumer, LootContext context) {
        Consumer<ItemStack> newConsumer = (stack) -> {
            ItemDrop.INSTANCE.onItemDrop(stack, context);
            lootConsumer.accept(stack);
        };
        instance.supplyOnce(newConsumer, context);
    }
}

