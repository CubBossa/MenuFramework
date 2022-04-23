package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Places items and click handlers on the current inventory page if the slot does not contain a static button.
 */
public interface MenuPreset<C extends TargetContext<?>> {

	void placeDynamicEntries(Menu menu,
							 BiConsumer<Integer, ItemStack> addItem,
							 BiConsumer<Integer, Map<Action<C>, ContextConsumer<C>>> addClickHandler);
}
