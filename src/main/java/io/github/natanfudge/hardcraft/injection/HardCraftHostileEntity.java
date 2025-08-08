package io.github.natanfudge.hardcraft.injection;

import net.minecraft.entity.Entity;

public interface HardCraftHostileEntity {
    /**
     * Specifies how easily a mob destroys blocks.
     * Demolition is interpreted as damage per tick. So 2 demolition is doing 40 damage per second.
     */
    int hardcraft_getDemolition();
    void hardcraft_setDemolition(int damagePerTick);

    /**
     * Used by navigation to mark the mob as having/not having a normal path (not needing breaking or placing blocks)
     * to the target. In those cases we don't want the mob to be doing anything special.
     */
    void hardcraft_setCantReachTarget(boolean value);

    /**
     * The result of {@link HardCraftHostileEntity#hardcraft_setCantReachTarget}, used to check if a mob should take
     * special action to reach its target.
     */
    boolean hardcraft_getCantReachTarget();

    /**
     * Will set the value returned by {@link Entity#isPushedByFluids()}, (whether the entity is pushed by fluids).
     * Useful for making mobs resilient to just pushing them with water.
     */
    void hardcraft_setIsPushedByFluids(boolean value);

    /**
     * Getter for {@link HardCraftHostileEntity#hardcraft_setIsPushedByFluids}
     */
    boolean hardcraft_getIsPushedByFluids();

    void hardcraft_setIsFireImmune(boolean value);
    boolean hardcraft_getIsFireImmune();

    void hardcraft_setIsPistonImmune(boolean value);
    boolean hardcraft_getIsPistonImmune();

}
