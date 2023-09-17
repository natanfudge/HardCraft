package io.github.natanfudge.hardcraft.injection;

import net.minecraft.entity.Entity;

/**
 * In HardCraft, we return true to canSee always so mobs will always chase the player.
 * However, in some cases, such as when finding targets for ranged enemies, we need to see if they can actually see their target.
 */
public interface HardCraftMobVisibilityCache {
    boolean hardcraft$originalCanSee(Entity entity);
}