package io.github.natanfudge.hardcraft.injection;

public interface HardCraftHostileEntity {
    /**
     * Specifies how easily a mob destroys blocks.
     * Demolition is interpreted as damage per tick. So 2 demolition is doing 40 damage per second.
     */
    int hardcraft_demolition();
}
