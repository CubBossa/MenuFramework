package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
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
	 * @param viewers  The viewers to open this menu for.
	 * @param viewMode The viewmode. Use VIEW to prevent the viewer from interacting with entries.
	 */
	void open(Collection<Player> viewers, ViewMode viewMode);

	/**
	 * Opens this menu to a viewer.
	 *
	 * @param viewer   The viewer to open this menu for.
	 * @param previous The menu that is expected to be open before this menu.
	 *                 If this menu is an instance of {@link LayeredMenu}, previous will be ignored.
	 *                 If not and previous is set to null, this menu will be opened as new base menu.
	 *                 Base menus simply close when closed. Child menus open their parent menus if closed.
	 */
	void open(Player viewer, Menu previous);

	/**
	 * Opens this menu to multiple viewers.
	 *
	 * @param viewers  The viewers to open this menu for.
	 * @param previous The menu that is expected to be open before this menu.
	 *                 If this menu is an instance of {@link LayeredMenu}, previous will be ignored.
	 *                 If not and previous is set to null, this menu will be opened as new base menu.
	 *                 Base menus simply close when closed. Child menus open their parent menus if closed.
	 */
	void open(Collection<Player> viewers, Menu previous);

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
	Menu openSubMenu(Player player, Supplier<AbstractMenu> menuSupplier);

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
	Menu openSubMenu(Player player, Supplier<AbstractMenu> menuSupplier, MenuPreset<?> backPreset);

	/**
	 * Renders the next page. This page might be empty, if no items are set.
	 *
	 * @param player The player to render the new page for.
	 */
	void openNextPage(Player player);

	/**
	 * Renders the previous page. This page might be empty, if no items are set.
	 *
	 * @param player The player to render the new page for.
	 */
	void openPreviousPage(Player player);

	/**
	 * Renders a given page. This does not open the inventory, it only replaces all items in the current inventory with the
	 * itemstacks for the given page.
	 *
	 * @param player The player to render the new page for.
	 * @param page   The page to render.
	 */
	void openPage(Player player, int page);

	/**
	 * Renders all items to the open inventory.
	 *
	 * @param viewer The viewer to render the items for. Used to render menus that are displayed for each player individually.
	 */
	void render(Player viewer);

	/**
	 * Close this menu for a player.
	 *
	 * @param viewer the player to close this menu for
	 */
	void close(Player viewer);

	/**
	 * Close this menu for multiple players.
	 *
	 * @param viewers the players to close this menu for.
	 */
	void closeAll(Collection<Player> viewers);

	/**
	 * Close all player views for this menu.
	 */
	void closeAll();

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
	 * Remove all static icons from the given slots. This does not affect the actual inventory. Use {@link #refresh(int...)} afterwards to
	 * remove the items from view.
	 *
	 * @param slots The slots to clear.
	 */
	void removeItem(int... slots);

	/**
	 * Populates the inventory with itemstacks that are rendered on each page, if no real item was found.
	 *
	 * @param slot The slots to render the item at (not paginated, only 0 to rows*cols)
	 * @param item The item to render
	 */
	void setDynamicItem(int slot, ItemStack item);

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
	 * @param button The button builder. Use {@link ButtonBuilder#buttonBuilder()} to get a new button builder instance
	 */
	void setButton(int slot, ButtonBuilder button);

	<C extends ClickContext> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler);

	void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler, int... slots);

	<C extends ClickContext> void setItemAndClickHandler(int slot, ItemStack item, Action<C> action, ContextConsumer<C> clickHandler);

	<C extends ClickContext> void setDefaultClickHandler(Action<C> action, ContextConsumer<C> clickHandler);

	void setDynamicClickHandler(int slot, Action<?> action, ContextConsumer<ClickContext> clickHandler);

	void removeClickHandler(int... slots);

	<C extends ClickContext> void removeClickHandler(Action<C> action, int... slots);

	void removeItemAndClickHandler(int... slots);

	<C extends ClickContext> void removeItemAndClickHandler(Action<C> action, int... slots);

	<C extends ClickContext> void removeDefaultClickHandler(Action<C> action);

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
