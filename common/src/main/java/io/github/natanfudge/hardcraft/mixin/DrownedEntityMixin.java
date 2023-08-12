package io.github.natanfudge.hardcraft.mixin;

import io.github.natanfudge.hardcraft.ai.HardCraftNavigation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin {
    @Unique
    DrownedEntity hardCraft$self = (DrownedEntity)((Object)this);

    /**
     *@reason Drowned decide to reassign their navigation dynamically to the land navigation so we need to fix the land navigation to conform with HostileEntityMixin
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    public void constructorHook(EntityType<? extends DrownedEntity> entityType, World world, CallbackInfo ci) {
        hardCraft$self.landNavigation = new HardCraftNavigation(hardCraft$self,world, hardCraft$self.landNavigation);
    }
}
