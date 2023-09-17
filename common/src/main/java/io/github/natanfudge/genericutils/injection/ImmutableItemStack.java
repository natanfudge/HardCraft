package io.github.natanfudge.genericutils.injection;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for using ItemStacks better - immutability.
 * We use java because it's more predictable for injection - and we inject this interface as an interface of ItemStack.
 */
public interface ImmutableItemStack {
    public default boolean isEmpty() {
        throw new IllegalStateException();
    }

    public default boolean isStackable() {
        throw new IllegalStateException();
    }

    public default Item getItem() {
        throw new IllegalStateException();
    }

    public default int getCount() {
        throw new IllegalStateException();
    }

    @Nullable
    public default NbtCompound getNbt() {
        throw new IllegalStateException();
    }
}
