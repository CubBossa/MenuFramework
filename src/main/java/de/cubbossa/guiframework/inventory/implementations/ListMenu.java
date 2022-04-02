package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;

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
	 * @return the next free list slot
	 */
	public int getNextListSlot() {
		return listIndex;
	}
}
