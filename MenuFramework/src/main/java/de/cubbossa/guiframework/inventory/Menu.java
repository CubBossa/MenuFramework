package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.CloseContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.OpenContext;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public interface Menu {

    /**
     * If set to view, a viewer cannot interact with the menu anymore
     */
    enum ViewMode {
        MODIFY,
        VIEW
    }

    /**
     * @return A map of all players that currently see this menu and their view mode.
     */
    Map<UUID, ViewMode> getViewer();

    /**
     * @return All slots that form one page of this menu.
     */
    int[] getSlots();

    /**
     * @return The amount of slots that form one page of this menu.
     */
    int getSlotsPerPage();

    /**
     * Opens this menu to a viewer.
     *
     * @param viewer The viewer to open this menu for.
     */
    void open(Player viewer);

    /**
     * Opens this menu to a viewer.
     *
     * @param viewer   The viewer to open this menu for.
     * @param viewMode The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     */
    void open(Player viewer, ViewMode viewMode);

    /**
     * Opens this menu to multiple viewers.
     *
     * @param viewers The viewers to open this menu for.
     */
    void open(Collection<Player> viewers);

    /**
     * Opens this menu to multiple viewers.
     *
     * @param viewers  The viewers to open this menu for.
     * @param viewMode The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     */
    void open(Collection<Player> viewers, ViewMode viewMode);

    /**
     * Opening inventories is not generally thread safe, therefore the actual opening happens in a synchronized method.
     * Only call this method if you are on the main thread.
     *
     * @param viewer The viewer to open this menu for.
     */
    void openSync(Player viewer);

    /**
     * Opening inventories is not generally thread safe, therefore the actual opening happens in a synchronized method.
     * Only call this method if you are on the main thread.
     *
     * @param viewer   The viewer to open this menu for.
     * @param viewMode The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     */
    void openSync(Player viewer, ViewMode viewMode);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player The viewer to open the sub menu for.
     * @param menu   The menu that is supposed to be opened as sub menu to this menu.
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Menu menu);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Supplier<Menu> menuSupplier);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player     The viewer to open the sub menu for.
     * @param menu       The menu that is supposed to be opened as sub menu to this menu.
     * @param backPreset A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, boolean, Action[])}
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Menu menu, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @param backPreset   A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, boolean, Action[])}
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Supplier<Menu> menuSupplier, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player     The viewer to open the sub menu for.
     * @param menu       The menu that is supposed to be opened as sub menu to this menu.
     * @param viewMode   The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     * @param backPreset A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, boolean, Action[])}
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Menu menu, ViewMode viewMode, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @param viewMode     The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     * @param backPreset   A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, boolean, Action[])}
     * @return The sub menu instance.
     */
    Menu openSubMenu(Player player, Supplier<Menu> menuSupplier, ViewMode viewMode, MenuPreset<?> backPreset);

    void openPreviousMenu(Player player);

    /**
     * @param previous The menu that will reappear once this menu closes
     */
    void setPrevious(Player player, Menu previous);

    /**
     * @param player The player to find the previous menu for.
     * @return The previous menu for the given player of null if none was found.
     */
    @Nullable Menu getPrevious(Player player);

    /**
     * Renders the next page. This page might be empty, if no items are set.
     *
     * @param player The player to render the new page for.
     */
    void setNextPage(Player player);

    /**
     * Renders the previous page. This page might be empty, if no items are set.
     *
     * @param player The player to render the new page for.
     */
    void setPreviousPage(Player player);

    /**
     * Renders a certain page. This does not open the inventory, it only replaces all items in the current inventory with the
     * itemstacks for the given page.
     *
     * @param player The player to render the new page for.
     * @param page   The page to render.
     */
    void setPage(Player player, int page);

    /**
     * Renders with offset. This does not open the inventory, it only replaces all items in the current inventory with the
     * itemstacks for the given page. Each item will be placed in the slot plus offset
     *
     * @param player The player to render the new page for.
     * @param offset The offset to add to the current offset.
     */
    void addOffset(Player player, int offset);

    /**
     * Renders with offset. This does not open the inventory, it only replaces all items in the current inventory with the
     * itemstacks for the given page. Each item will be placed in the slot plus offset
     *
     * @param player The player to render the new page for.
     * @param offset The offset to add to the current offset.
     */
    void removeOffset(Player player, int offset);

    /**
     * Renders with offset. This does not open the inventory, it only replaces all items in the current inventory with the
     * itemstacks for the given page. Each item will be placed in the slot plus offset
     *
     * @param player The player to render the new page for.
     * @param offset The offset to add to the current offset.
     */
    void setOffset(Player player, int offset);

    /**
     * Renders all items to the open inventory.
     *
     * @param viewer The viewer to render the items for. Used to render menus that are displayed for each player individually.
     * @param clear  If the inventory should be cleared before rendering items.
     */
    void render(Player viewer, boolean clear);

    /**
     * Close this menu for a player.
     *
     * @param viewer the player to close this menu for
     */
    void close(Player viewer);

    void closeKeepInventory(Player viewer);

    /**
     * Handles all logic that has to be run when closed. Don't call except you know what you're doing.
     * This is meant to be called by the {@link #close(Player)} method or in the InventoryCloseEvent.
     *
     * @param viewer       The viewer to run the close logic for.
     */
    void handleClose(Player viewer);

    /**
     * Handles the interaction for a certain action with provided context.
     *
     * @param action  The action to handle
     * @param context The context for this action
     * @param <C>     The Context Target Type
     * @return true, if the interaction should be cancelled
     */
    <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context);

    /**
     * Refreshes the itemstack at certain slots of this menu.
     * This method needs to be called after all methods that insert items. {@link #setItem(int, ItemStack)}
     *
     * @param slots the slots to refresh
     */
    void refresh(int... slots);

    /**
     * Refreshes all dynamic ItemStacks, generated by MenuPresets.
     */
    void refreshDynamicItemSuppliers();

    /**
     * loads a dynamic preset that only exists as long as the current page is opened. This might be useful to
     * implement pagination, as pagination may need to extend dynamically based on the page count.
     *
     * @param menuPreset The instance of the processor. Use the BiConsumer parameters to add items and clickhandler
     *                   to a specific slot.
     */
    MenuPreset<? extends TargetContext<?>> addPreset(MenuPreset<? extends TargetContext<?>> menuPreset);

    /**
     * Unloads a certain menu processor / preset. The preset items will stay until their slot is updated.
     *
     * @param menuPreset The preset to remove
     */
    void removePreset(MenuPreset<? extends TargetContext<?>> menuPreset);

    /**
     * Removes all presets. The preset icons will stay in all open menus of this instance until the menu gets refreshed.
     * Reopen them or call {@link #refresh(int...)} on the according or just all slots with {@link #getSlots()}
     */
    void removeAllPresets();

    /**
     * Clears all minecraft inventory slots. It does not clear the menu item map or any click handlers.
     * After reopening or refreshing the menu, all items will be back.
     */
    void clearContent();

    /**
     * Gets the ItemStack for a certain Slot. This considers Menu Presets and static Items. It can be overridden to change the
     * way, menus are rendered.
     *
     * @param slot The slot to get the itemstack from
     * @return The itemstack of the menu at the given slot. This does not return the actual item in the inventory but the stored item instance.
     */
    ItemStack getItemStack(int slot);

    /**
     * Sets an inventory icon
     *
     * @param item The item instance to insert into the inventory
     * @param slot The slot to add the item at. Use slots larger than the slots on one page to place them on a different page.
     *             {@code slot = (current_page * slots_per_page) + inventory_slot}
     */
    void setItem(int slot, ItemStack item);

    /**
     * Sets an inventory icon
     *
     * @param itemSupplier The item supplier to insert into the inventory. The supplier is called every time the slot gets refreshed.
     * @param slot         The slot to add the item at. Use slots larger than the slots on one page to place them on a different page.
     *                     {@code slot = (current_page * slots_per_page) + inventory_slot}
     */
    void setItem(int slot, Supplier<ItemStack> itemSupplier);

    /**
     * Remove all static icons from the given slots. This does not affect the actual inventory. Use {@link #refresh(int...)} afterwards to
     * remove the items from view.
     *
     * @param slots The slots to clear.
     */
    void removeItem(int... slots);

    /**
     * Gets the click handler that will be executed when interacting. This regards menu presets, if no static handler was set.
     * Click handlers are mapped on slots and actions.
     *
     * @param slot   The absolute slot to get the click handler from.
     * @param action The action to get the click handler from.
     * @return The click handler instance.
     */
    ContextConsumer<? extends TargetContext<?>> getClickHandler(int slot, Action<?> action);

    /**
     * Sets an inventory icon, sound and click handler from a button builder
     *
     * @param slot   The absolute slot to insert the button builder at. {@code ((current_page * slots_per_page) + page_slot)}
     * @param button The button builder. Use {@link Button#builder()} to get a new button builder instance
     */
    void setButton(int slot, Button button);

    /**
     * Sets a click handler, that is called if a player interacts with the given slot and the actions are equal.
     *
     * @param slot         The absolute slot to insert the clickHandler at. {@code ((current_page * slots_per_page) + page_slot)}
     * @param action       The action to run this click handler for.
     * @param clickHandler An instance of the actual click handler interface, you might want to use lambda expressions
     * @param <C>          The click context type of the action and click handler.
     */
    <C extends TargetContext<?>> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler);

    /**
     * Sets a click handler, that is called if a player interacts with the given slot and the actions are equal.
     *
     * @param slot         The absolute slot to insert the clickHandler at. {@code ((current_page * slots_per_page) + page_slot)}
     * @param clickHandler A map of actions and corresponding instances of the actual click handler interfaces.
     */
    void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler);

    /**
     * Sets a click handler, that is called if a player interacts with the given slot and the actions are equal.
     *
     * @param slot         The absolute slot to insert the clickHandler at. {@code ((current_page * slots_per_page) + page_slot)}
     * @param item         The item stack to insert at the slot.
     * @param action       The action to run this click handler for.
     * @param clickHandler An instance of the actual click handler interface, you might want to use lambda expressions.
     * @param <C>          The click context type of the action and click handler.
     */
    <C extends TargetContext<?>> void setItemAndClickHandler(int slot, ItemStack item, Action<C> action, ContextConsumer<C> clickHandler);

    /**
     * Sets a click handler, that is called if a player interacts with the given slot and the actions are equal.
     *
     * @param slot         The absolute slot to insert the clickHandler at. {@code ((current_page * slots_per_page) + page_slot)}
     * @param itemSupplier The item supplier to insert into the inventory. The supplier is called every time the slot gets refreshed.
     * @param action       The action to run this click handler for.
     * @param clickHandler An instance of the actual click handler interface, you might want to use lambda expressions.
     * @param <C>          The click context type of the action and click handler.
     */
    <C extends TargetContext<?>> void setItemAndClickHandler(int slot, Supplier<ItemStack> itemSupplier, Action<C> action, ContextConsumer<C> clickHandler);

    /**
     * Sets a default click handler to run on a certain action
     *
     * @param action       The action to call the default click handler for.
     * @param clickHandler An instance of the actual click handler interface, you might want to use lambda expressions
     * @param <C>The       click context type of the action and click handler.
     */
    <C extends TargetContext<?>> void setDefaultClickHandler(Action<C> action, ContextConsumer<C> clickHandler);

    /**
     * Removes all click handlers from the given slots.
     */
    void removeClickHandler(int... slots);

    /**
     * Removes all click handlers for a certain action from the given slots.
     *
     * @param action The action to remove all click handlers for.
     * @param slots  The slots to clear.
     */
    void removeClickHandler(Action<?> action, int... slots);

    /**
     * Removes all items and click handlers from the given slots.
     */
    void removeItemAndClickHandler(int... slots);

    /**
     * Removes all items and click handlers for a certain action from the given slots.
     *
     * @param action The action to remove all click handlers for.
     * @param slots  The slots to clear.
     */
    void removeItemAndClickHandler(Action<?> action, int... slots);

    /**
     * Removes a default click handler
     *
     * @param action The action to remove the default click handler for.
     */
    void removeDefaultClickHandler(Action<?> action);

    /**
     * Sets an open handler that is called when the inventory has been opened and all items are set.
     *
     * @param openHandler The open handler instance
     */
    void setOpenHandler(ContextConsumer<OpenContext> openHandler);

    /**
     * Sets a close handler that is called when the inventory has been closed.
     *
     * @param closeHandler The close handler instance
     */
    void setCloseHandler(ContextConsumer<CloseContext> closeHandler);

    /**
     * Checks if a certain inventory is equal to the current instance of this menu inventory.
     *
     * @param inventory The inventory to check.
     * @param player    The player to check the inventory for, in case inventories are handled by players.
     * @return true if the inventories are equal.
     */
    boolean isThisInventory(Inventory inventory, Player player);

    /**
     * @return The index of the first filled page of this menu.
     */
    int getMinPage();

    /**
     * @return The index of the last filled page of this menu.
     */
    int getMaxPage();

    /**
     * @return The current page of this menu.
     */
    int getCurrentPage();

    /**
     * @return The amount of filled pages for this menu. (abs(minPage) + abs(maxPage))
     */
    int getPageCount();
}
