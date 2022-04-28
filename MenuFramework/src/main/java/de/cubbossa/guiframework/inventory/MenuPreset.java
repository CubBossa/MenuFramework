package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

/**
 * Places items and click handlers on the current inventory page if the slot does not contain a static button.
 */
public interface MenuPreset<C extends TargetContext<?>> {

	void placeDynamicEntries(PresetApplier buttonHandler);

	@RequiredArgsConstructor
	abstract class PresetApplier {

		@Getter
		private final Menu menu;

		public abstract void addItem(int slot, ItemStack itemStack);
		public abstract void addItemOnTop(int slot, ItemStack itemStack);

		public abstract <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler);
		public abstract <C extends TargetContext<?>> void addClickHandlerOnTop(int slot, Action<C> action, ContextConsumer<C> clickHandler);
	}
}
