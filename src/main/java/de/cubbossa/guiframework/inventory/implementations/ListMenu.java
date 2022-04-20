package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.ButtonBuilder;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import de.cubbossa.guiframework.util.Pair;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * A Chest Menu, that provides methods to add list entries without worrying about the last slot.
 */
public class ListMenu extends InventoryMenu {

    @Getter
    private final int[] listSlots;
    private final List<Pair<ItemStack, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>>> listElements;

    /**
     * Creates a new chest list menu with the given count of rows
     *
     * @param rows      The amount of rows for the inventory
     * @param title     The Title of the chest inventory
     * @param listSlots All slots of one page that should be used
     */
    public ListMenu(int rows, Component title, int... listSlots) {
        super(rows, title);
        this.listSlots = listSlots.length == 0 ? IntStream.range(0, (rows - 1) * 9).toArray() : listSlots;
        this.listElements = new ArrayList<>();

        this.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, rows - 1));
        this.addPreset(MenuPresets.paginationRow(rows - 1, 0, 1, false, Action.Inventory.RIGHT, Action.Inventory.LEFT));
    }

    private Pair<ItemStack, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> getElement(int slot) {
        int index = -1;
        for (int i = 0; i < listSlots.length; i++) {
            if (listSlots[i] == slot) {
                index = i;
            }
        }
        return index == -1 ? null : listElements.get(index + currentPage * listSlots.length);

    }

    @Override
    public ItemStack getItemStack(int slot) {
        var element = getElement(slot);
        return element == null ? super.getItemStack(slot) : element.getLeft();
    }

    @Override
    protected <C extends TargetContext<?>> ContextConsumer<C> getClickHandlerOrFallback(int slot, Action<C> action) {
        var element = getElement(slot);
        return element == null ? super.getClickHandlerOrFallback(slot, action) : (ContextConsumer<C>) element.getRight().get(action);
    }

    @Override
    public int getMaxPage() {
        return Integer.min(super.getMaxPage(), listElements.size() % listSlots.length);
    }

    /**
     * Adds an entry to the list inventory. It automatically increases the last index slot.
     * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
     *
     * @param buttonBuilder a button to insert.
     * @return the reference to the stored object Pair. Can be used to remove list elements
     */
    public Pair<ItemStack, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> addListEntry(ButtonBuilder buttonBuilder) {
        var pair = new Pair<>(buttonBuilder.getStack(), buttonBuilder.getClickHandler());
        listElements.add(pair);
        return pair;
    }

    /**
     * Removes the last element from the list
     */
    public void popListEntry() {
        listElements.remove(listElements.get(listElements.size() - 1));
    }

    /**
     * Removes the element from this list
     * @param entry The instance to remove - store it when calling {@link #addListEntry(ButtonBuilder)}
     */
    public void removeListEntry(Pair<ItemStack, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> entry) {
        listElements.remove(entry);
    }

    /**
     * Clears all list entries
     */
    public void clearListEntries() {
        listElements.clear();
    }
}
