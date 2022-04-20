package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.CloseContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import de.cubbossa.guiframework.inventory.implementations.ListMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class MenuPresets {

    public static ItemStack FILLER_LIGHT = createItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack FILLER_DARK = createItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack BACK = createItemStack(Material.SPRUCE_DOOR, Component.text("Back", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack BACK_DISABLED = createItemStack(Material.IRON_DOOR, Component.text("Back", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack UP = createItemStack(Material.PAPER, Component.text("Up", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack DOWN = createItemStack(Material.PAPER, Component.text("Down", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack RIGHT = createItemStack(Material.PAPER, Component.text("Next", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack LEFT = createItemStack(Material.PAPER, Component.text("Previous", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack UP_DISABLED = createItemStack(Material.MAP, Component.text("Up", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack DOWN_DISABLED = createItemStack(Material.MAP, Component.text("Down", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack RIGHT_DISABLED = createItemStack(Material.MAP, Component.text("Next", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack LEFT_DISABLED = createItemStack(Material.MAP, Component.text("Previous", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack ACCEPT = createItemStack(Material.LIME_CONCRETE, Component.text("Accept", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack DECLINE = createItemStack(Material.RED_CONCRETE, Component.text("Decline", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack NEW = createItemStack(Material.EMERALD, Component.text("New", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false), null);

    public static ListMenuSupplier<Player> PLAYER_LIST_SUPPLIER = new ListMenuSupplier<>() {
        @Override
        public Collection<Player> getElements() {
            return (Collection<Player>) Bukkit.getOnlinePlayers();
        }

        @Override
        public ItemStack getDisplayItem(Player object) {
            return createItemStack(Material.PLAYER_HEAD, object.displayName(), null);
        }
    };

    /**
     * Fills one page of an inventory menu with an itemstack
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu  the menu to fill
     * @param stack the itemstack to set on each slot
     * @param page  the page to fill
     */
    public static void fill(AbstractInventoryMenu menu, ItemStack stack, int page) {
        Arrays.stream(menu.getSlots()).map(operand -> operand + page * menu.getSlotsPerPage()).forEach(value -> menu.setItem(value, stack));
    }

    /**
     * Fills one page of an inventory menu with {@link #FILLER_DARK}
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu the menu to fill
     * @param page the page to fill
     */
    public static void fillDark(AbstractInventoryMenu menu, int page) {
        fill(menu, FILLER_DARK, page);
    }

    /**
     * Fills one page of an inventory menu with {@link #FILLER_LIGHT}
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu the menu to fill
     * @param page the page to fill
     */
    public static void fillLight(AbstractInventoryMenu menu, int page) {
        fill(menu, FILLER_LIGHT, page);
    }

    /**
     * Fills a single row of a menu with the given itemstack.
     * Use {@link #fillRow(ItemStack, int)} to fill the row for every page
     *
     * @param menu  the menu to fill
     * @param stack the stack to use
     * @param line  the line to fill (0 to 5)
     * @param page  the page to fill the line on
     */
    public static void fillRow(AbstractInventoryMenu menu, ItemStack stack, int line, int page) {
        int offset = page * menu.slotsPerPage + line * 9;
        IntStream.range(offset, offset + 9).forEach(value -> menu.setItem(value, stack));
    }

    /**
     * Fills a single column of a menu with the given itemstack.
     * Use {@link #fillColumn(ItemStack, int)} to fill the column for every page
     *
     * @param menu   the menu to fill
     * @param stack  the stack to use
     * @param column the column to fill (0 to 8)
     * @param page   the page to fill the column on
     */
    public static void fillColumn(AbstractInventoryMenu menu, ItemStack stack, int column, int page) {
        int offset = page * menu.slotsPerPage;
        IntStream.range(offset, offset + menu.slotsPerPage).filter(value -> value % 9 == column)
                .forEach(value -> menu.setItem(value, stack));
    }

    /**
     * Places a back icon to close the current menu and open the parent menu if one was set.
     * The icon will be {@link #BACK} or {@link #BACK_DISABLED} if disabled.
     *
     * @param slot     the slot to place the back icon at.
     * @param disabled if the back icon should be displayed as disabled.
     * @param actions  all valid actions to run the back handler.
     * @param <T>      the Action type
     * @param <C>      the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> back(int slot, boolean disabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            placeDynamicItem.accept(slot, disabled ? BACK_DISABLED : BACK);
            placeDynamicClickHandler.accept(slot, populate(c -> menu.close(c.getPlayer()), actions));
        };
    }

    /**
     * Places a next page and a previous page icon at each page
     *
     * @param row          the row to place both icons at (0 to 5)
     * @param leftSlot     the slot for the previous page icon (0 to 8)
     * @param rightSlot    the slot for the next page icon (0 to 8)
     * @param hideDisabled if the previous and next page buttons should be invisible if no previous or next page exists.
     *                     Otherwise, {@link #LEFT_DISABLED} and {@link #RIGHT_DISABLED} will be rendered.
     * @param actions      the actions to run the clickhandlers with.
     * @param <T>          the Action type
     * @param <C>          the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> paginationRow(int row, int leftSlot, int rightSlot, boolean hideDisabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {

            boolean leftLimit = menu.getCurrentPage() <= menu.getMinPage();
            boolean rightLimit = menu.getCurrentPage() >= menu.getMaxPage();
            if (leftLimit) {
                if (!hideDisabled) {
                    placeDynamicItem.accept(row * 9 + leftSlot, LEFT_DISABLED);
                }
            } else {
                placeDynamicItem.accept(row * 9 + leftSlot, LEFT);
                placeDynamicClickHandler.accept(row * 9 + leftSlot, populate(c -> menu.openPreviousPage(c.getPlayer()), actions));
            }
            if (rightLimit) {
                if (!hideDisabled) {
                    placeDynamicItem.accept(row * 9 + rightSlot, RIGHT_DISABLED);
                }
            } else {
                placeDynamicItem.accept(row * 9 + rightSlot, RIGHT);
                placeDynamicClickHandler.accept(row * 9 + rightSlot, populate(c -> menu.openNextPage(c.getPlayer()), actions));
            }
        };
    }

    /**
     * Places a next page and a previous page icon at each page that allows to turn pages for a DIFFERENT menu that is
     * currently open. This of course makes most sense when combining a top inventory menu and a bottom inventory menu.
     * The bottom inventory menu could be used to navigate through the top inventory menu of someone else, implementing
     * an administrators view.
     *
     * @param otherMenu    the menu to turn pages for
     * @param row          the row to place both icons at (0 to 5)
     * @param leftSlot     the slot for the previous page icon (0 to 8)
     * @param rightSlot    the slot for the next page icon (0 to 8)
     * @param hideDisabled if the previous and next page buttons should be invisible if no previous or next page exists.
     *                     Otherwise, {@link #LEFT_DISABLED} and {@link #RIGHT_DISABLED} will be rendered.
     * @param actions      the actions to run the clickhandlers with.
     * @param <T>          the Action type
     * @param <C>          the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> paginationRow(AbstractInventoryMenu otherMenu, int row, int leftSlot, int rightSlot, boolean hideDisabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {

            boolean leftLimit = otherMenu.getCurrentPage() <= otherMenu.getMinPage();
            boolean rightLimit = otherMenu.getCurrentPage() >= otherMenu.getMaxPage();
            if (!leftLimit || hideDisabled) {
                placeDynamicItem.accept(row * 9 + leftSlot, leftLimit ? LEFT_DISABLED : LEFT);
            }
            placeDynamicClickHandler.accept(row * 9 + leftSlot, populate(c -> {
                boolean currentLeftLimit = otherMenu.getCurrentPage() <= otherMenu.getMinPage();
                boolean currentRightLimit = otherMenu.getCurrentPage() >= otherMenu.getMaxPage();
                menu.setDynamicItem(row * 9 + leftSlot, currentLeftLimit ? LEFT_DISABLED : LEFT);
                menu.setDynamicItem(row * 9 + rightSlot, currentRightLimit ? RIGHT_DISABLED : RIGHT);
                menu.refresh(row * 9 + leftSlot, row * 9 + rightSlot);
                if (!currentLeftLimit) {
                    otherMenu.openPreviousPage(c.getPlayer());
                }
            }, actions));
            if (!rightLimit || !hideDisabled) {
                placeDynamicItem.accept(row * 9 + rightSlot, rightLimit ? RIGHT_DISABLED : RIGHT);
            }
            placeDynamicClickHandler.accept(row * 9 + rightSlot, populate(c -> {
                //TODO chaos
                boolean currentLeftLimit = otherMenu.getCurrentPage() <= otherMenu.getMinPage();
                boolean currentRightLimit = otherMenu.getCurrentPage() >= otherMenu.getMaxPage();
                menu.setDynamicItem(row * 9 + leftSlot, currentLeftLimit ? LEFT_DISABLED : LEFT);
                menu.setDynamicItem(row * 9 + rightSlot, currentRightLimit ? RIGHT_DISABLED : RIGHT);
                menu.refresh(row * 9 + leftSlot, row * 9 + rightSlot);
                if (!currentRightLimit) {
                    otherMenu.openNextPage(c.getPlayer());
                }
            }, actions));
        };
    }

    /**
     * Places a next page and a previous page icon at each page in a column.
     *
     * @param column       the column to place both icons at (0 to 8)
     * @param upSlot       the slot for the previous page icon (0 to 5)
     * @param downSlot     the slot for the next page icon (0 to 5)
     * @param hideDisabled if the previous and next page buttons should be invisible if no previous or next page exists.
     *                     Otherwise, {@link #UP_DISABLED} and {@link #DOWN_DISABLED} will be rendered.
     * @param actions      the actions to run the clickhandlers with.
     * @param <T>          the Action type
     * @param <C>          the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> paginationColumn(int column, int upSlot, int downSlot, boolean hideDisabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            boolean upperLimit = menu.getCurrentPage() == menu.getMinPage();
            boolean lowerLimit = menu.getCurrentPage() == menu.getMaxPage();
            if (upperLimit) {
                if (!hideDisabled) {
                    placeDynamicItem.accept(upSlot * 9 + column, UP_DISABLED);
                }
            } else {
                placeDynamicItem.accept(upSlot * 9 + column, UP);
                placeDynamicClickHandler.accept(upSlot * 9 + column, populate(c -> menu.openPreviousPage(c.getPlayer()), actions));
            }
            if (lowerLimit) {
                if (!hideDisabled) {
                    placeDynamicItem.accept(downSlot * 9 + column, DOWN_DISABLED);
                }
            } else {
                placeDynamicItem.accept(downSlot * 9 + column, DOWN);
                placeDynamicClickHandler.accept(downSlot * 9 + column, populate(c -> menu.openNextPage(c.getPlayer()), actions));
            }
        };
    }

    /**
     * Fills a whole inventory with the given item.
     *
     * @param stack the item to place on each slot.
     * @param <T>   the Action type
     * @param <C>   the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fill(ItemStack stack) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(0, menu.getSlotsPerPage()).forEach(value -> placeDynamicItem.accept(value, stack));
        };
    }

    /**
     * Fills a whole inventory line with the given item.
     *
     * @param stack the item to place on each line slot.
     * @param line  the line to fill
     * @param <T>   the Action type
     * @param <C>   the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fillRow(ItemStack stack, int line) {
        return (menu1, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(line * 9, line * 9 + 9).forEach(value -> placeDynamicItem.accept(value, stack));
        };
    }

    /**
     * Fills a whole inventory column with the given item.
     *
     * @param stack  the item to place on each column slot.
     * @param column the column to fill
     * @param <T>    the Action type
     * @param <C>    the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fillColumn(ItemStack stack, int column) {
        return (menu1, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(0, menu1.getSlotsPerPage()).filter(value -> value % 9 == column).forEach(value -> placeDynamicItem.accept(value, stack));
        };
    }

    /**
     * Fills a whole inventory with a frame (outer ring of slots filled)
     *
     * @param stack the stack to place
     * @param <T>   the Action type
     * @param <C>   the ClickContext type
     * @return an instance of the {@link DynamicMenuProcessor} to register it on a menu.
     */
    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fillFrame(ItemStack stack) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(0, menu.getSlotsPerPage())
                    .filter(value -> value % 9 == 0 || value % 9 == 5 || value < 9 || value >= menu.slotsPerPage - 9)
                    .forEach(value -> placeDynamicItem.accept(value, stack));
        };
    }

    public static InventoryMenu newCraftMenu(Component title, ItemStack stack) {
        return null; //TODO
    }

    private static <T, C extends ClickContext> DynamicMenuProcessor<T, C> newItem(int slot, ContextConsumer<C> newHandler) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            placeDynamicItem.accept(slot, NEW);
            placeDynamicClickHandler.accept(slot, populate(newHandler, (T) Action.Inventory.LEFT)); //TODO T zu actions
        };
    }

    /**
     * Creates a list menu with all Online Players. The implementation of refresh on join and disconnect needs to be implemented manually.
     *
     * @param title        The title of the list menu
     * @param rows         The amount of rows of the list menu
     * @param actions      The actions that trigger the clickhandler. Mind that middle click is used for duplicate and right click for deleting.
     * @param clickHandler The click handler to run when an object icon is clicked.
     * @return The instance of the list menu
     */
    public static ListMenu newPlayerListMenu(Component title, int rows, Collection<Action<? extends TargetContext<?>>> actions, ContextConsumer<TargetContext<Player>> clickHandler) {
        return newListMenu(title, rows, PLAYER_LIST_SUPPLIER, actions, clickHandler, null);
    }

    /**
     * Creates a list menu from a supplier and allows to delete, duplicate and create elements if the supplier derives from
     * {@link ListMenuManagerSupplier}.
     *
     * @param title            The title of the list menu
     * @param rows             The amount of rows of the list menu
     * @param supplier         The supplier that defines how to display the provided type of objects
     * @param actions          The actions that trigger the clickhandler. Mind that middle click is used for duplicate and right click for deleting.
     * @param clickHandler     The click handler to run when an object icon is clicked.
     * @param createNewHandler The createNewHandler allows to add own functions to provide the arguments for the call of {@link ListMenuManagerSupplier#newElementFromMenu(Object[])}
     * @param <T>              The type of objects to display in the list menu as itemstacks
     * @return The instance of the list menu
     */
    public static <T> ListMenu newListMenu(Component title, int rows, ListMenuSupplier<T> supplier, Collection<Action<? extends TargetContext<?>>> actions, ContextConsumer<TargetContext<T>> clickHandler, @Nullable Consumer<Consumer<Object[]>> createNewHandler) {
        ListMenu listMenu = new ListMenu(rows, title);
        listMenu.loadPreset(paginationRow(rows - 1, 0, 1, false, Action.Inventory.LEFT));

        if (supplier instanceof ListMenuManagerSupplier<T> manager) {

            for (T object : supplier.getElements()) {
                listMenu.addListEntry(ButtonBuilder.buttonBuilder()
                        .withItemStack(manager.getDisplayItem(object))
                        .withClickHandler(c -> clickHandler.accept(new TargetContext<>(c.getPlayer(), c.getSlot(), c.isCancelled(), object)), actions.toArray(Action[]::new))
                        .withClickHandler(Action.Inventory.MIDDLE, clickContext -> {
                            manager.duplicateElementFromMenu(object);
                        })
                        .withClickHandler(Action.Inventory.RIGHT, clickContext -> {
                            manager.deleteFromMenu(object);
                        }));
            }

            ContextConsumer<ClickContext> c;
            if (createNewHandler == null) {
                c = clickContext -> manager.newElementFromMenu(new Object[0]);
            } else {
                c = clickContext -> createNewHandler.accept(manager::newElementFromMenu);
            }
            listMenu.loadPreset(newItem(rows * 9 - 1, c));
        } else {

            for (T object : supplier.getElements()) {
                listMenu.addListEntry(ButtonBuilder.buttonBuilder().withItemStack(supplier.getDisplayItem(object)));
            }
        }
        return listMenu;
    }

    public static InventoryMenu newConfirmMenu(Component title, ContextConsumer<ClickContext> accept, ContextConsumer<ClickContext> decline, ContextConsumer<CloseContext> closeHandler) {
        return null; //TODO
    }


    private static <T, C extends ClickContext> Map<T, ContextConsumer<C>> populate(ContextConsumer<C> contextConsumer, T... actions) {
        Map<T, ContextConsumer<C>> map = new HashMap<>();
        for (T action : actions) {
            map.put(action, contextConsumer);
        }
        return map;
    }

    private static ItemStack createItemStack(Material type, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(type);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
}
