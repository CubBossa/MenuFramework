package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ListMenu extends InventoryMenu {

	private final int[] listSlots;
	private int listIndex = 0;

	public ListMenu(int rows, Component title) {
		this(rows, title, IntStream.range(0, (rows - 1) * 9).toArray());
	}

	public ListMenu(int rows, Component title, int... listslots) {
		super(rows, title);
		this.listSlots = listslots;
		this.listIndex = listslots[0];

		this.loadPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, rows - 1));
		this.loadPreset(MenuPresets.paginationRow(rows - 1, 0, 1, false, ClickType.RIGHT, ClickType.LEFT));
	}

	/**
	 * Adds an entry to the list inventory. It automatically increases the last index slot.
	 * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
	 *
	 * @param buttonBuilder a button to insert.
	 */
	public void addListEntry(ButtonBuilder<ClickType, ClickContext> buttonBuilder) {
		setButton(buttonBuilder, listIndex / listSlots.length * slotsPerPage + listSlots[listIndex % listSlots.length]);
		listIndex++;
	}

	/**
	 * Adds a collection as list entries to the list. This might be useful to display all online players e.g.
	 *
	 * @param collection   the collection of objects to add
	 * @param itemProvider a function to display an object of the collection as itemstack
	 * @param clickHandler a click handler to run when a list entry is clicked
	 * @param actions      the actions that will trigger the click handler
	 * @param <E>          the entry type of the collection
	 */
	public <E> void addListEntries(Collection<E> collection, Function<E, ItemStack> itemProvider, Consumer<E> clickHandler, ClickType... actions) {
		collection.forEach(e -> addListEntry(buttonBuilder()
				.withItemStack(itemProvider.apply(e))
				.withClickHandler(clickContext -> clickHandler.accept(e), actions)));
	}

	/**
	 * @return the next free list slot
	 */
	public int getNextListSlot() {
		return listIndex;
	}
}
