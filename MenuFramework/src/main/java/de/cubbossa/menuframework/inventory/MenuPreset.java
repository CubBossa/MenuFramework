package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

/**
 * Places items and click handlers on the current inventory page if the slot does not contain a static button.
 */
public interface MenuPreset<C extends TargetContext<?>> {

	void placeDynamicEntries(PresetApplier buttonHandler) throws Throwable;

	@RequiredArgsConstructor
	abstract class PresetApplier {

		@Getter
		private final Menu menu;

		public abstract void addItem(int slot, ItemStack itemStack);
		public abstract void addItem(int slot, Supplier<ItemStack> itemStack);
		public abstract void addItemOnTop(int slot, ItemStack itemStack);
		public abstract void addItemOnTop(int slot, Supplier<ItemStack> itemStack);

		public abstract <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler);
		public abstract <C extends TargetContext<?>> void addClickHandlerOnTop(int slot, Action<C> action, ContextConsumer<C> clickHandler);
	}
}
