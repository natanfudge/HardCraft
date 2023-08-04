package io.github.natanfudge.hardcraft.mixin;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityNavigation.class)
public class EntityNavigationMixin {
   //TODO: Get rid of these mixins because it's bad to not reset pathing
    @ModifyConstant(method = "checkTimeouts(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(intValue = 100))
    public int changeTimeoutConstant(int constant){
        // Make timeout be 1000 ticks (50s)
        return 1000;
    }
    @ModifyConstant(method = "checkTimeouts(Lnet/minecraft/util/math/Vec3d;)V", constant = @Constant(doubleValue = 3.0))
    public double changeTimeoutConstant2(double constant){
        // Make sure it doesn't time out for other reasons
        return 100.0;
    }
}
