package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Allows placing dynamic items and clickhandler that are not permanent.
 * This might be useful to implement pagination presets that depend on the current page.
 *
 * @param <T> Type of the processed {@link AbstractInventoryMenu <T>}
 */
public interface DynamicMenuProcessor<T, C extends ClickContext> {

    void placeDynamicEntries(ItemStackMenu menu,
                             BiConsumer<Integer, ItemStack> placeDynamicItem,
                             BiConsumer<Integer, Map<T, ContextConsumer<C>>> placeDynamicClickHandler);
}
