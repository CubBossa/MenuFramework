package de.cubbossa.menuframework.inventory;

import net.kyori.adventure.text.ComponentLike;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface TopMenu extends Menu {

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player The viewer to open the sub menu for.
     * @param menu   The menu that is supposed to be opened as sub menu to this menu.
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, TopMenu menu);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player     The viewer to open the sub menu for.
     * @param menu       The menu that is supposed to be opened as sub menu to this menu.
     * @param backPreset A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, Action[])}
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, TopMenu menu, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @param backPreset   A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, Action[])}
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player     The viewer to open the sub menu for.
     * @param menu       The menu that is supposed to be opened as sub menu to this menu.
     * @param viewMode   The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     * @param backPreset A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, Action[])}
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, TopMenu menu, Menu.ViewMode viewMode, MenuPreset<?> backPreset);

    /**
     * Opens another menu as a sub menu to this menu.
     *
     * @param player       The viewer to open the sub menu for.
     * @param menuSupplier The supplier for the menu that is supposed to be opened as sub menu to this menu.
     * @param viewMode     The viewmode. Use VIEW to prevent the viewer from interacting with entries.
     * @param backPreset   A menu preset that could insert a back icon on every page. E.g. {@link MenuPresets#back(int, int, Action[])}
     * @return The sub menu instance.
     */
    TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier, Menu.ViewMode viewMode, MenuPreset<?> backPreset);

    /**
     * Opens the previous menu for this inventory or closes this inventory if no previous inventory exists.
     *
     * @param player The player to open the previous menu for.
     */
    void openPreviousMenu(Player player);

    /**
     * @param previous The menu that will reappear once this menu closes
     */
    void setPrevious(Player player, TopMenu previous);

    /**
     * @param player The player to find the previous menu for.
     * @return The previous menu for the given player of null if none was found.
     */
    @Nullable TopMenu getPrevious(Player player);

    boolean isPrevious(TopMenu menu, Player player);

    boolean isSubMenu(TopMenu menu, Player player);

    /**
     * @param page The page to return the title for.
     * @return The title for the given page. Fallback title if no title was found.
     */
    ComponentLike getTitle(int page);

    /**
     * Updates the title of this inventory.
     *
     * @param title The new title to set
     */
    void updateTitle(ComponentLike title);

    /**
     * Sets the title for all given pages and updates the current pages if it is contained in the pages array.
     *
     * @param title The title to set for the given pages.
     * @param pages The pages to affect.
     */
    void updateTitle(ComponentLike title, int... pages);

}
