package de.cubbossa.menuframework.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface ListMenuSupplier<T> {

    Collection<T> getElements();

    ItemStack getDisplayItem(T object);
}
