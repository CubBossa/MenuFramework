package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public abstract class AbstractInventoryMenu extends ItemStackMenu {

    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> clickHandler;
    protected final List<DynamicMenuProcessor<Action<?>, ? extends TargetContext<?>>> dynamicProcessors;
    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandler;
    protected final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> defaultClickHandler;
    protected final Map<Action<?>, Boolean> defaultCancelled;

    public AbstractInventoryMenu(int slotsPerPage) {
        super(slotsPerPage);

        this.clickHandler = new TreeMap<>();
        this.dynamicProcessors = new ArrayList<>();
        this.dynamicClickHandler = new TreeMap<>();
        this.defaultClickHandler = new HashMap<>();
        this.defaultCancelled = new HashMap<>();
    }

    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable ItemStackMenu previous) {

        if (inventory == null) {
            inventory = createInventory(viewer, currentPage);
        }
        clearContent();

        if (viewer.isSleeping()) {
            viewer.wakeup(true);
        }
        int minPage = getMinPage();
        int maxPage = getMaxPage();
        if (currentPage < minPage) {
            currentPage = minPage;
        } else if (currentPage > maxPage) {
            currentPage = maxPage;
        }

        dynamicItemStacks.clear();
        dynamicClickHandler.clear();
        for (DynamicMenuProcessor processor : dynamicProcessors) {
            processor.placeDynamicEntries(this, (integer, itemStack) -> dynamicItemStacks.put((Integer) integer, (ItemStack) itemStack),
                    (key, value) -> dynamicClickHandler.put((Integer) key, (Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>) value));
        }

        for (int slot : getSlots()) {
            ItemStack item = itemStacks.getOrDefault(currentPage * slotsPerPage + slot, dynamicItemStacks.get(slot));
            if (item == null) {
                continue;
            }
            inventory.setItem(slot, item.clone());
        }

        openInventory(viewer, inventory);
        this.viewer.put(viewer.getUniqueId(), viewMode);
        InventoryHandler.getInstance().registerInventory(viewer, this, (AbstractInventoryMenu) previous);
    }

    public <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context) {

        Player player = context.getPlayer();
        int slot = context.getSlot();
        if (Arrays.stream(getSlots()).noneMatch(value -> value == slot)) {
            return false;
        }
        if (viewer.containsKey(player.getUniqueId()) && viewer.get(player.getUniqueId()).equals(ViewMode.VIEW)) {
            return true;
        }

        int actualSlot = currentPage * slotsPerPage + slot;
        if (soundPlayer.containsKey(actualSlot)) {
            soundPlayer.get(actualSlot).accept(context.getPlayer());
        }

        ContextConsumer<C> clickHandler = getClickHandlerOrFallback(slot, action);
        if (clickHandler == null) {
            clickHandler = (ContextConsumer<C>) dynamicClickHandler.getOrDefault(context.getSlot(), new HashMap<>()).get(action);
        }
        if (clickHandler != null) {
            //execute and catch exceptions so users can't dupe itemstacks.
            try {
                clickHandler.accept(context);
            } catch (Exception exc) {
                context.setCancelled(true);
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while handling GUI interaction of player " + player.getName(), exc);
            }
        }
        return context.isCancelled();
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        if (this instanceof TopInventoryMenu topMenu && this.viewer.size() == 0) {
            InventoryHandler.getInstance().unregisterTopMenuListener(topMenu);
        }
    }

    /**
     * loads a dynamic preset that only exists as long as the current page is opened. This might be useful to
     * implement pagination, as pagination may need to extend dynamically based on the page count.
     *
     * @param menuProcessor the instance of the processor. Use the BiConsumer parameters to add items and clickhandler
     *                      to a specific slot.
     */
    public <C extends ClickContext> DynamicMenuProcessor<Action<C>, C> loadPreset(DynamicMenuProcessor<Action<C>, C> menuProcessor) {
        DynamicMenuProcessor d = menuProcessor;
        dynamicProcessors.add(d);
        return menuProcessor;
    }

    /**
     * Unloads a certain menu processor / preset. The preset items will stay until their slot is updated.
     *
     * @param menuProcessor the preset to remove
     */
    public <C extends ClickContext> void unloadPreset(DynamicMenuProcessor<Action<C>, C> menuProcessor) {
        dynamicProcessors.remove(menuProcessor);
    }
    /**
     * Removes all presets. The preset icons will stay in all open menus of this instance until the menu gets refreshed.
     * Reopen them or call {@link #refresh(int...)} on the according or just all slots with {@link #getSlots()}
     */
    public void unloadAllPresets() {
        dynamicProcessors.clear();
    }

    protected <C extends TargetContext<?>> ContextConsumer<C> getClickHandlerOrFallback(int slot, Action<C> action) {
        return (ContextConsumer<C>) clickHandler.getOrDefault(currentPage * slotsPerPage + slot, new HashMap<>()).getOrDefault(action, defaultClickHandler.get(action));
    }

    /**
     * Clears all minecraft inventory slots. It does not clear the menu item map or any click handlers.
     * After reopening or refreshing the menu, all items will be back.
     */
    public void clearContent() {
        for (int slot : getSlots()) {
            inventory.setItem(slot, null);
        }
    }

    /**
     * Sets an inventory icon, sound and click handler from a button builder
     *
     * @param slot   the absolute slot to apply the button builder on. {@code ((current_page * slots_per_page) + page_slot)}
     * @param button the button builder. Use {@link ButtonBuilder#buttonBuilder()} to get a new button builder instance
     */
    public void setButton(int slot, ButtonBuilder button) {
        if (button.getStack() != null) {
            setItem(slot, button.getStack());
        }
        if (button.getSound() != null) {
            soundPlayer.put(slot, player -> player.playSound(player.getLocation(), button.getSound(), button.getVolume(), button.getPitch()));
        }
        if (!button.getClickHandler().isEmpty()) {
            setClickHandler(slot, button.getClickHandler());
        }
    }

    public <C extends ClickContext> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
        map.put(action, clickHandler);
        this.clickHandler.put(slot, map);
    }

    public void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler, int... slots) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
        map.putAll(clickHandler);
        this.clickHandler.put(slot, map);
    }

    public <C extends ClickContext> void setItemAndClickHandler(int slot, ItemStack item, Action<C> action, ContextConsumer<C> clickHandler) {
        setItem(slot, item);
        setClickHandler(slot, action, clickHandler);
    }

    public <C extends ClickContext> void setDefaultClickHandler(Action<C> action, ContextConsumer<C> clickHandler) {
        defaultClickHandler.put(action, clickHandler);
    }

    public void removeClickHandler(int... slots) {
        for (int slot : slots) {
            clickHandler.remove(slot);
        }
    }

    public <C extends ClickContext> void removeClickHandler(Action<C> action, int... slots) {
        for (int slot : slots) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public void removeItemAndClickHandler(int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            clickHandler.remove(slot);
        }
    }

    public <C extends ClickContext> void removeItemAndClickHandler(Action<C> action, int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public <C extends ClickContext> void removeDefaultClickHandler(Action<C> action) {
        defaultClickHandler.remove(action);
    }

    public boolean isThisInventory(Inventory inventory, Player player) {
        return this.inventory != null && this.inventory.equals(inventory);
    }

    public int getPageCount() {
        return getMaxPage() - getMinPage();
    }

    public int getMinPage() {
        int minPage = 0;
        int smallestSlot = Integer.min(itemStacks.isEmpty() ? 0 : itemStacks.firstKey(), clickHandler.isEmpty() ? 0 : clickHandler.firstKey());
        boolean negative = smallestSlot < 0;
        while (negative && smallestSlot < -slotsPerPage || !negative && smallestSlot > slotsPerPage) {
            if (negative) {
                minPage--;
                smallestSlot += slotsPerPage;
            } else {
                minPage++;
                smallestSlot -= slotsPerPage;
            }
        }
        return Integer.min(negative ? --minPage : minPage, currentPage);
    }

    public int getMaxPage() {
        int maxPage = 0;
        int highestSlot = Integer.max(itemStacks.isEmpty() ? 0 : itemStacks.lastKey(), clickHandler.isEmpty() ? 0 : clickHandler.lastKey());
        while (highestSlot > slotsPerPage) {
            maxPage++;
            highestSlot -= slotsPerPage;
        }
        return Integer.max(maxPage, currentPage);
    }
}
