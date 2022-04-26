package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.Button;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A Chest Menu, that provides methods to add list entries without worrying about the last slot.
 */
public class ListMenu extends InventoryMenu {

    public record ListElement<T>(Supplier<ItemStack> itemSupplier,
                                 Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandlers) {
    }

    @Getter
    private final int[] listSlots;
    private final List<ListElement<?>> listElements;

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
    }

    private ListElement<?> getElement(int slot) {
        int index = -1;
        int pageSlot = slot % slotsPerPage;
        for (int i = 0; i < listSlots.length; i++) {
            if (listSlots[i] == pageSlot) {
                index = i;
            }
        }
        if (index == -1) {
            return null;
        }
        int i = index + currentPage * listSlots.length;
        return i >= listElements.size() ? null : listElements.get(i);
    }

    @Override
    public ItemStack getItemStack(int slot) {
        var element = getElement(slot);
        return element == null ? super.getItemStack(slot) : element.itemSupplier().get();
    }

    @Override
    public ContextConsumer<? extends TargetContext<?>> getClickHandler(int slot, Action<?> action) {
        var element = getElement(slot);
        return element == null ? super.getClickHandler(slot, action) : element.clickHandlers().get(action);
    }

    @Override
    public int getMaxPage() {
        return Integer.max(super.getMaxPage(), (int) Math.ceil((double) listElements.size() / listSlots.length));
    }

    /**
     * Adds an entry to the list inventory. It automatically increases the last index slot.
     * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
     *
     * @param buttonBuilder a button to insert.
     * @return the reference to the stored object Pair. Can be used to remove list elements
     */
    public <T> ListElement<T> addListEntry(Button buttonBuilder) {
        ListElement<T> element = new ListElement<>(buttonBuilder.getStackSupplier(), buttonBuilder.getClickHandler());
        listElements.add(element);
        return element;
    }

    public <T> List<ListElement<T>> addListEntries(Collection<T> elements, Function<T, ItemStack> itemSupplier, Action<?> action, ContextConsumer<TargetContext<T>> clickHandler) {
        return addListEntries(elements, itemSupplier, Map.of(action, clickHandler));
    }


    public <T> List<ListElement<T>> addListEntries(Collection<T> elements, Function<T, ItemStack> itemSupplier, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        List<ListElement<T>> ret = new ArrayList<>();
        for (T element : elements) {
            ListElement<T> e = new ListElement<T>(() -> itemSupplier.apply(element), clickHandler);
            this.listElements.add(e);
            ret.add(e);
        }
        return ret;
    }

    /**
     * Removes the last element from the list
     */
    public void popListEntry() {
        listElements.remove(listElements.get(listElements.size() - 1));
    }

    /**
     * Removes the element from this list
     *
     * @param entry The instance to remove - store it when calling {@link #addListEntry(Button)}
     */
    public void removeListEntry(ListElement<?> entry) {
        listElements.remove(entry);
    }

    /**
     * Clears all list entries
     */
    public void clearListEntries() {
        listElements.clear();
    }
}
