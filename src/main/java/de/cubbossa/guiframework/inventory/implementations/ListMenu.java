package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.ButtonBuilder;
import de.cubbossa.guiframework.inventory.MenuPresets;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Chest Menu, that provides methods to add list entries without worrying about the last slot.
 */
public class ListMenu extends InventoryMenu {

    @Getter
    private final int[] listSlots;
    private int listIndex;

    //TODO items und clickhandler manuell speichern, um refresh methode zu implementieren

    /**
     * Creates a new chest list menu with the given count of rows
     *
     * @param rows      The amount of rows for the inventory
     * @param title     The Title of the chest inventory
     * @param listSlots All slots of one page that should be used
     */
    public ListMenu(int rows, Component title, int... listSlots) {
        super(rows, title);
        this.listSlots = listSlots;
        this.listIndex = listSlots.length == 0 ? 0 : listSlots[0];

        this.loadPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, rows - 1));
        this.loadPreset(MenuPresets.paginationRow(rows - 1, 0, 1, false, Action.Inventory.RIGHT, Action.Inventory.LEFT));
    }

    /**
     * Adds an entry to the list inventory. It automatically increases the last index slot.
     * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
     *
     * @param buttonBuilder a button to insert.
     */
    public void addListEntry(ButtonBuilder buttonBuilder) {
        setButton(getNextListSlot(), buttonBuilder);
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
    public <E> void addListEntries(Collection<E> collection, Function<E, ItemStack> itemProvider, Consumer<E> clickHandler, Action<?>... actions) {
        collection.forEach(e -> addListEntry(ButtonBuilder.buttonBuilder()
                .withItemStack(itemProvider.apply(e))
                .withClickHandler(clickContext -> clickHandler.accept(e), actions)));
    }

    /**
     * @return the next free list slot
     */
    public int getNextListSlot() {
        return listSlots.length == 0 ? listIndex : listIndex / listSlots.length * slotsPerPage + listSlots[listIndex % listSlots.length];
    }
}
