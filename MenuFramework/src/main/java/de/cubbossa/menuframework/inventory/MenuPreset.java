package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.panel.Panel;
import de.cubbossa.menuframework.inventory.panel.SimplePanel;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Supplier;

/**
 * Places items and click handlers on the current inventory page if the slot does not contain a static button.
 */
public interface MenuPreset<C extends TargetContext<?>> {

	void placeDynamicEntries(PresetApplier buttonHandler) throws Throwable;

	@Getter
	abstract class PresetApplier {

		private final Menu menu;
		private final SortedMap<Integer, ItemStack> dynamicItemStacks;
		private final SortedMap<Integer, ItemStack> dynamicItemStacksOnTop;
		private final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandler;
		private final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandlerOnTop;

		public PresetApplier(Menu menu) {
			this.menu = menu;
			this.dynamicItemStacks = new TreeMap<>();
			this.dynamicItemStacksOnTop = new TreeMap<>();
			this.dynamicClickHandler = new TreeMap<>();
			this.dynamicClickHandlerOnTop = new TreeMap<>();
		}

		public Collection<Panel> generate() {
			List<Integer> topSlots = new ArrayList<>();
			topSlots.addAll(dynamicItemStacksOnTop.keySet());
			topSlots.addAll(dynamicClickHandlerOnTop.keySet());
			SimplePanel top = new SimplePanel(topSlots.stream().distinct().mapToInt(Integer::intValue).toArray());
			topSlots.forEach(slot -> top.setButton(slot, Button.builder()
					.withItemStack(dynamicItemStacksOnTop.get(slot))
					.withClickHandler(dynamicClickHandlerOnTop.get(slot))));
			top.setPriority(Integer.MAX_VALUE);

			List<Integer> bottomSlots = new ArrayList<>();
			bottomSlots.addAll(dynamicItemStacks.keySet());
			bottomSlots.addAll(dynamicClickHandler.keySet());
			SimplePanel bottom = new SimplePanel(bottomSlots.stream().distinct().mapToInt(Integer::intValue).toArray());
			bottomSlots.forEach(slot -> bottom.setButton(slot, Button.builder()
					.withItemStack(dynamicItemStacks.get(slot))
					.withClickHandler(dynamicClickHandler.get(slot))));
			bottom.setPriority(Integer.MIN_VALUE);

			HashSet<Panel> sortedMap = new HashSet<>();
			sortedMap.add(bottom);
			sortedMap.add(top);

			dynamicClickHandler.clear();
			dynamicClickHandlerOnTop.clear();
			dynamicItemStacks.clear();
			dynamicItemStacksOnTop.clear();

			return sortedMap;
		}

		public abstract void addItem(int slot, ItemStack itemStack);

		public abstract void addItem(int slot, Supplier<ItemStack> itemStack);

		public abstract void addItemOnTop(int slot, ItemStack itemStack);

		public abstract void addItemOnTop(int slot, Supplier<ItemStack> itemStack);

		public abstract <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler);

		public abstract <C extends TargetContext<?>> void addClickHandlerOnTop(int slot, Action<C> action, ContextConsumer<C> clickHandler);
	}
}
