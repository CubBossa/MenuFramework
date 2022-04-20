package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Allows placing dynamic items and clickhandler that are not permanent.
 * This might be useful to implement pagination presets that depend on the current page.
 */
public interface DynamicMenuProcessor<C extends TargetContext<?>> {

    void placeDynamicEntries(ItemStackMenu menu,
                             BiConsumer<Integer, ItemStack> placeDynamicItem,
                             BiConsumer<Integer, Map<Action<C>, ContextConsumer<C>>> placeDynamicClickHandler);
}
