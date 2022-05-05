package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.BottomMenu;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A Chest Menu, that provides methods to add list entries without worrying about the last slot.
 */
public class ListMenu extends RectInventoryMenu {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ListElement {
        private Supplier<ItemStack> itemSupplier;
        private Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandlers;
    }

    @Getter
    private final int[] listSlots;
    private final long listSlotMask;
    private final List<ListElement> listElements;

    /**
     * Creates a new chest list menu with the given count of rows
     *
     * @param title     The Title of the chest inventory
     * @param rows      The amount of rows for the inventory
     * @param listSlots All slots of one page that should be used
     */
    public ListMenu(ComponentLike title, int rows, int... listSlots) {
        super(title, rows);
        this.listSlots = listSlots.length == 0 ? IntStream.range(0, (rows - 1) * 9).toArray() : listSlots;
        this.listSlotMask = BottomMenu.getMaskFromSlots(this.listSlots);
        this.listElements = new ArrayList<>();
    }

    private boolean isListSlot(int slot) {
        return (listSlotMask >> (slot % slotsPerPage) & 1) == 1;
    }

    private int toListSlot(int slot) {
        return slot - (slot / slotsPerPage) * (slotsPerPage - getListSlots().length);
    }

    @Override
    protected ItemStack getStaticItemStack(int slot) {
        if(!isListSlot(slot)) {
            return null;
        }
        int s = toListSlot(slot);
        if(s >= listElements.size()) {
            return null;
        }
        ListElement element = listElements.get(s);
        if (element == null || element.itemSupplier == null) {
            return null;
        }
        return element.itemSupplier.get();
    }

    @Override
    protected ContextConsumer<? extends TargetContext<?>> getStaticClickHandler(int slot, Action<?> action) {
        if(!isListSlot(slot)) {
            return null;
        }
        int s = toListSlot(slot);
        if(s >= listElements.size()) {
            return null;
        }
        ListElement element = listElements.get(s);
        if (element == null || element.clickHandlers == null) {
            return null;
        }
        return element.clickHandlers.get(action);
    }

    @Override
    public void setItem(int slot, Supplier<ItemStack> itemSupplier) {
        int s = toListSlot(slot);
        ListElement element = listElements.get(s);
        if (element == null) {
            element = new ListElement(itemSupplier, new HashMap<>());
            listElements.add(s, element);
        } else {
            element.itemSupplier = itemSupplier;
        }
    }

    @Override
    public void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        super.setClickHandler(slot, clickHandler);
        int s = toListSlot(slot);
        ListElement element = listElements.get(s);
        if (element == null) {
            element = new ListElement(null, clickHandler);
            listElements.add(s, element);
        } else {
            element.clickHandlers = clickHandler;
        }
    }

    @Override
    public int getMaxPage() {
        return (int) Math.floor((double) listElements.size() / listSlots.length);
    }

    /**
     * Adds an entry to the list inventory. It automatically increases the last index slot.
     * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
     *
     * @param buttonBuilder a button to insert.
     * @return the reference to the stored object Pair. Can be used to remove list elements
     */
    public <T> ListElement addListEntry(Button buttonBuilder) {
        ListElement element = new ListElement(buttonBuilder.getStackSupplier(), buttonBuilder.getClickHandler());
        listElements.add(element);
        return element;
    }

    public <T> List<ListElement> addListEntries(Collection<T> elements, Function<T, ItemStack> itemSupplier, Action<?> action, ContextConsumer<TargetContext<T>> clickHandler) {
        return addListEntries(elements, itemSupplier, Map.of(action, clickHandler));
    }


    public <T> List<ListElement> addListEntries(Collection<T> elements, Function<T, ItemStack> itemSupplier, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        List<ListElement> ret = new ArrayList<>();
        for (T element : elements) {
            ListElement e = new ListElement(() -> itemSupplier.apply(element), clickHandler);
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
    public void removeListEntry(ListElement entry) {
        listElements.remove(entry);
    }

    /**
     * Clears all list entries
     */
    public void clearListEntries() {
        listElements.clear();
    }
}
