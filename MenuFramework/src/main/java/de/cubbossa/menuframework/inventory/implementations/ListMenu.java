package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.panel.Panel;
import de.cubbossa.menuframework.inventory.panel.RectPanel;
import de.cubbossa.menuframework.inventory.panel.SimplePanel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.IntStream;

/**
 * A Chest Menu, that provides methods to add list entries without worrying about the last slot.
 */
@Getter
public class ListMenu extends RectInventoryMenu {

    @Setter
    @AllArgsConstructor
    public static class ListElement {
        private Supplier<ItemStack> itemSupplier;
        private Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandlers;
    }

    private final List<ListElement> listElements;

    private final Panel entryPanel;
    private final Panel navigationPanel;

    /**
     * Creates a new chest list menu with the given count of rows
     *
     * @param title     The Title of the chest inventory
     * @param rows      The amount of rows for the inventory
     * @param listSlots All slots of one page that should be used
     */
    public ListMenu(ComponentLike title, int rows, int... listSlots) {
        super(title, rows);
        int[] slots = listSlots;
        if (listSlots.length == 0) {
            slots = IntStream.range(0, (rows - 1) * 9).toArray();
        }
        this.entryPanel = new SimplePanel(slots);
        this.navigationPanel = new RectPanel(9, 1);
        this.navigationPanel.setButton(0, Button.builder()
                .withItemStack(Material.PAPER)
                .withClickHandler(Action.LEFT, c -> entryPanel.setOffset(entryPanel.getOffset() - entryPanel.getPageSize())));
        this.navigationPanel.setButton(1, Button.builder()
                .withItemStack(Material.PAPER)
                .withClickHandler(Action.LEFT, c -> entryPanel.setOffset(entryPanel.getOffset() + entryPanel.getPageSize())));
        this.listElements = new ArrayList<>();

        clearSubPanels();
    }

    @Override public void clearSubPanels() {
        super.clearSubPanels();
        addSubPanel(0, entryPanel);
        addSubPanel((getRows() - 1) * 9, navigationPanel);
    }

    @Override
    public void render(Player viewer, boolean clear) throws ItemPlaceException {
        entryPanel.clearSubPanels();
        for (int i = 0; i < listElements.size(); i++) {
            ListElement e = listElements.get(i);
            entryPanel.setButton(i, Button.builder().withItemStack(e.itemSupplier).withClickHandler(e.clickHandlers));
        }
        super.render(viewer, clear);
    }

    @Override
    public void setItem(int slot, Supplier<ItemStack> itemSupplier) {
        GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Don't use #setClickHandler or #setItem on ListMenus. Instead, append with #addListEntry");
    }

    @Override
    public void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Don't use #setClickHandler or #setItem on ListMenus. Instead, append with #addListEntry");
    }

    @Override public int getMinPage() {
        return 0;
    }

    @Override
    public int getMaxPage() {
        return entryPanel.getMaxPage();
    }

    /**
     * Adds an entry to the list inventory. It automatically increases the last index slot.
     * Afterwards, list entries can be overwritten by placing a normal button at the same slot.
     *
     * @param buttonBuilder a button to insert.
     * @return the reference to the stored object Pair. Can be used to remove list elements
     */
    public ListElement addListEntry(Button buttonBuilder) {
        ListElement element = new ListElement(buttonBuilder.getStackSupplier(), buttonBuilder.getClickHandler());
        listElements.add(element);
        return element;
    }

    public <T> List<ListElement> addListEntries(Collection<T> elements, Function<T, ItemStack> itemSupplier, Action<?> action, ContextConsumer<TargetContext<T>> clickHandler) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = new HashMap<>();
        map.put(action, clickHandler);
        return addListEntries(elements, itemSupplier, map);
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
