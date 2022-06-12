package de.cubbossa.menuframework.inventory;

import com.google.common.base.Strings;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.InventoryMenu;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.menuframework.inventory.panel.MenuIcon;
import de.cubbossa.menuframework.inventory.panel.Panel;
import de.cubbossa.menuframework.util.Animation;
import de.cubbossa.menuframework.util.ItemStackUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class MenuPresets {

    public static ItemStack FILLER_LIGHT = ItemStackUtils.createItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack FILLER_DARK = ItemStackUtils.createItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack BACK = ItemStackUtils.createItemStack(Material.SPRUCE_DOOR, Component.text("Back", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack BACK_DISABLED = ItemStackUtils.createItemStack(Material.IRON_DOOR, Component.text("Back", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack UP = ItemStackUtils.createItemStack(Material.PAPER, Component.text("Up", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack DOWN = ItemStackUtils.createItemStack(Material.PAPER, Component.text("Down", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack RIGHT = ItemStackUtils.createItemStack(Material.PAPER, Component.text("Next", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack LEFT = ItemStackUtils.createItemStack(Material.PAPER, Component.text("Previous", NamedTextColor.WHITE, TextDecoration.UNDERLINED).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack UP_DISABLED = ItemStackUtils.createItemStack(Material.MAP, Component.text("Up", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack DOWN_DISABLED = ItemStackUtils.createItemStack(Material.MAP, Component.text("Down", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack RIGHT_DISABLED = ItemStackUtils.createItemStack(Material.MAP, Component.text("Next", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);
    public static ItemStack LEFT_DISABLED = ItemStackUtils.createItemStack(Material.MAP, Component.text("Previous", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), null);

    public static ListMenuSupplier<Player> PLAYER_LIST_SUPPLIER = new ListMenuSupplier<Player>() {
        @Override
        public Collection<Player> getElements() {
            return (Collection<Player>) Bukkit.getOnlinePlayers();
        }

        @Override
        public ItemStack getDisplayItem(Player object) {
            return ItemStackUtils.createCustomHead(object, GUIHandler.getInstance().getAudiences().player(object)
                    .getOrDefault(Identity.DISPLAY_NAME, Component.text(object.getName())), null);
        }
    };

    /**
     * x = 0 is first col, x = 8 is last col,
     * y = 0 is first row, y = 5 is last row
     * -> top left to bottom right
     */
    public static BiFunction<Integer, Integer, Integer> XY_TO_INDEX = (col, row) -> 9 * row + col;

    /**
     * Fills one page of an inventory menu with an itemstack
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu  the menu to fill
     * @param stack the itemstack to set on each slot
     * @param page  the page to fill
     */
    public static void fill(Menu menu, ItemStack stack, int page) {
        Arrays.stream(menu.getSlots()).map(operand -> operand + page * menu.getSlotsPerPage()).forEach(value -> menu.setItem(value, stack));
    }

    /**
     * Fills one page of an inventory menu with {@link #FILLER_DARK}
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu the menu to fill
     * @param page the page to fill
     */
    public static void fillDark(Menu menu, int page) {
        fill(menu, FILLER_DARK, page);
    }

    /**
     * Fills one page of an inventory menu with {@link #FILLER_LIGHT}
     * Use {@link #fill(ItemStack)} to fill every page
     *
     * @param menu the menu to fill
     * @param page the page to fill
     */
    public static void fillLight(Menu menu, int page) {
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
    public static void fillRow(Menu menu, ItemStack stack, int line, int page) {
        int offset = page * menu.getSlotsPerPage() + line * 9;
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
    public static void fillColumn(Menu menu, ItemStack stack, int column, int page) {
        int offset = page * menu.getSlotsPerPage();
        IntStream.range(offset, offset + menu.getSlotsPerPage()).filter(value -> value % 9 == column)
                .forEach(value -> menu.setItem(value, stack));
    }

    /**
     * Places a back icon to close the current menu and open the parent menu if one was set.
     * The icon will be {@link #BACK} or {@link #BACK_DISABLED} if disabled.
     *
     * @param actions all valid actions to run the back handler.
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static Panel back(Action<?>... actions) {
        return new MenuIcon(() -> BACK, player -> {
        }, Button.builder()
                .withClickHandler(c -> {
                    if (!(c.getMenu() instanceof TopMenu)) {
                        GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Back Context cannot be applied to bottom Inventories.");
                        return;
                    }
                    TopMenu topMenu = (TopMenu) c.getMenu();
                    if (topMenu.getPrevious(c.getPlayer()) != null) {
                        topMenu.openPreviousMenu(c.getPlayer());
                    }
                }, actions).getClickHandler());
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
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> paginationRow(int row, int leftSlot, int rightSlot, boolean hideDisabled, Action<?>... actions) {
        return applier -> {

            boolean leftLimit = applier.getMenu().getCurrentPage() <= applier.getMenu().getMinPage();
            boolean rightLimit = applier.getMenu().getCurrentPage() >= applier.getMenu().getMaxPage();
            if (leftLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(row * 9 + leftSlot, LEFT_DISABLED);
                }
            } else {
                applier.addItemOnTop(row * 9 + leftSlot, LEFT);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(row * 9 + leftSlot, action, c -> applier.getMenu().setPreviousPage());
                }
            }
            if (rightLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(row * 9 + rightSlot, RIGHT_DISABLED);
                }
            } else {
                applier.addItemOnTop(row * 9 + rightSlot, RIGHT);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(row * 9 + rightSlot, action, c -> applier.getMenu().setNextPage());
                }
            }
        };
    }

    public static MenuPreset<?> scrollingVertically(int leftSlot, int rightSlot, boolean hideDisabled, Action<?>... actions) {
        return applier -> {

            boolean leftLimit = applier.getMenu().getCurrentPage() <= applier.getMenu().getMinPage();
            boolean rightLimit = applier.getMenu().getCurrentPage() >= applier.getMenu().getMaxPage();
            if (leftLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(leftSlot, LEFT_DISABLED);
                }
            } else {
                applier.addItemOnTop(leftSlot, LEFT);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(leftSlot, action, c -> applier.getMenu().removeOffset(1));
                }
            }
            if (rightLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(rightSlot, RIGHT_DISABLED);
                }
            } else {
                applier.addItemOnTop(rightSlot, RIGHT);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(rightSlot, action, c -> applier.getMenu().addOffset(9));
                }
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
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> paginationRow(Menu otherMenu, int row, int leftSlot, int rightSlot, boolean hideDisabled, Action<?>... actions) {
        return applier -> {
            Menu menu = applier.getMenu();

            int lSlot = row * 9 + leftSlot;
            int rSlot = row * 9 + rightSlot;

            // place next and previous items
            boolean leftLimit = otherMenu.getCurrentPage() <= otherMenu.getMinPage();
            if (!leftLimit || !hideDisabled) {
                applier.addItemOnTop(lSlot, leftLimit ? LEFT_DISABLED : LEFT);
            }
            boolean rightLimit = otherMenu.getCurrentPage() >= otherMenu.getMaxPage();
            if (!rightLimit || !hideDisabled) {
                applier.addItemOnTop(rSlot, rightLimit ? RIGHT_DISABLED : RIGHT);
            }

            // handle clicking
            for (Action<?> action : actions) {
                applier.addClickHandlerOnTop(lSlot, action, targetContext -> {
                    if (otherMenu.getCurrentPage() > otherMenu.getMinPage()) {
                        otherMenu.setPreviousPage();
                        menu.refresh(menu.getSlots());
                    }
                });
                applier.addClickHandlerOnTop(lSlot, action, targetContext -> {
                    if (otherMenu.getCurrentPage() < otherMenu.getMaxPage()) {
                        otherMenu.setNextPage();
                        menu.refresh(menu.getSlots());
                    }
                });
            }
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
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> paginationColumn(int column, int upSlot, int downSlot, boolean hideDisabled, Action<?>... actions) {
        return applier -> {
            Menu menu = applier.getMenu();
            boolean upperLimit = menu.getCurrentPage() == menu.getMinPage();
            boolean lowerLimit = menu.getCurrentPage() == menu.getMaxPage();
            if (upperLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(upSlot * 9 + column, UP_DISABLED);
                }
            } else {
                applier.addItemOnTop(upSlot * 9 + column, UP);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(upSlot * 9 + column, action, c -> menu.setPreviousPage());
                }
            }
            if (lowerLimit) {
                if (!hideDisabled) {
                    applier.addItemOnTop(downSlot * 9 + column, DOWN_DISABLED);
                }
            } else {
                applier.addItemOnTop(downSlot * 9 + column, DOWN);
                for (Action<?> action : actions) {
                    applier.addClickHandlerOnTop(downSlot * 9 + column, action, c -> menu.setNextPage());
                }
            }
        };
    }

    /**
     * Fills a whole inventory with the given item.
     *
     * @param stack the item to place on each slot.
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fill(ItemStack stack) {
        return applier -> Arrays.stream(applier.getMenu().getSlots()).forEach(value -> applier.addItem(value, stack));
    }

    /**
     * Fills a whole inventory line with the given item.
     *
     * @param stack the item to place on each line slot.
     * @param line  the line to fill
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillRow(ItemStack stack, int line) {
        return applier -> IntStream.range(line * 9, line * 9 + 9).forEach(value -> applier.addItem(value, stack));
    }

    /**
     * Fills a whole inventory line with the given item that override static inventory items.
     *
     * @param stack the item to place on each line slot.
     * @param line  the line to fill
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillRowOnTop(ItemStack stack, int line) {
        return applier -> IntStream.range(line * 9, line * 9 + 9).forEach(value -> applier.addItemOnTop(value, stack));
    }

    /**
     * Fills a whole inventory column with the given item.
     *
     * @param stack  the item to place on each column slot.
     * @param column the column to fill
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillColumn(ItemStack stack, int column) {
        return applier -> {
            IntStream.range(0, applier.getMenu().getSlotsPerPage()).filter(value -> value % 9 == column).forEach(value -> applier.addItem(value, stack));
        };
    }

    /**
     * Fills a whole inventory column with the given item and overrides static items.
     *
     * @param stack  the item to place on each column slot.
     * @param column the column to fill
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillColumnOnTop(ItemStack stack, int column) {
        return applier -> IntStream.range(0, applier.getMenu().getSlotsPerPage()).filter(value -> value % 9 == column).forEach(value -> applier.addItemOnTop(value, stack));
    }

    /**
     * Fills a whole inventory with a frame (outer ring of slots filled)
     *
     * @param stack the stack to place
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillFrame(ItemStack stack) {
        return applier -> {
            IntStream.range(0, applier.getMenu().getSlotsPerPage())
                    .filter(value -> value % 9 == 0 || value % 9 == 8 || value < 9 || value >= applier.getMenu().getSlotsPerPage() - 9)
                    .forEach(value -> applier.addItem(value, stack));
        };
    }

    /**
     * Fills a whole inventory with a frame (outer ring of slots filled) and overrides static items.
     *
     * @param stack the stack to place
     * @return an instance of the {@link MenuPreset} to register it on a menu.
     */
    public static MenuPreset<?> fillFrameOnTop(ItemStack stack) {
        return applier -> IntStream.range(0, applier.getMenu().getSlotsPerPage())
                .filter(value -> value % 9 == 0 || value % 9 == 8 || value < 9 || value >= applier.getMenu().getSlotsPerPage() - 9)
                .forEach(value -> applier.addItemOnTop(value, stack));
    }

    /**
     * Creates a list menu with all Online Players. The implementation of refresh on join and disconnect needs to be implemented manually.
     * To refresh the current page, call {@link ListMenu#refresh(int...)} for {@link ListMenu#getEntryPanel().getSlots()}
     *
     * @param title        The title of the list menu
     * @param rows         The amount of rows of the list menu
     * @param action       The action that triggers the clickhandler. Mind that middle click is used for duplicate and right click for deleting.
     * @param clickHandler The click handler to run when an object icon is clicked.
     * @return The instance of the list menu
     */
    public static ListMenu newPlayerListMenu(ComponentLike title, int rows, Action<? extends TargetContext<?>> action, ContextConsumer<TargetContext<Player>> clickHandler) {
        return newListMenu(title, rows, PLAYER_LIST_SUPPLIER, action, clickHandler);
    }

    /**
     * Creates a list menu from a supplier and allows viewing, deleting and duplicating elements if the supplier derives from
     * {@link ListMenuManagerSupplier}.
     * To refresh the current page after adding a new element, call {@link ListMenu#refresh(int...)} for {@link ListMenu#getEntryPanel().getSlots()}
     *
     * @param title        The title of the list menu
     * @param rows         The amount of rows of the list menu
     * @param supplier     The supplier that defines how to display the provided type of objects
     * @param action       The action that triggers the clickhandler. Mind that middle click is used for duplicate and right click for deleting.
     * @param clickHandler The click handler to run when an object icon is clicked.
     * @param <T>          The type of objects to display in the list menu as itemstacks
     * @return The instance of the list menu
     */
    public static <T> ListMenu newListMenu(ComponentLike title, int rows, ListMenuSupplier<T> supplier, Action<? extends TargetContext<?>> action, ContextConsumer<TargetContext<T>> clickHandler) {
        ListMenu listMenu = new ListMenu(title, rows);
        listMenu.addPreset(fill(FILLER_LIGHT));
        listMenu.addPreset(fillRow(FILLER_DARK, rows - 1));
        listMenu.addPreset(paginationRow(rows - 1, 0, 1, false, Action.LEFT));

        if (supplier instanceof ListMenuManagerSupplier) {
            ListMenuManagerSupplier<T> manager = (ListMenuManagerSupplier<T>) supplier;

            for (T object : supplier.getElements()) {
                listMenu.addListEntry(Button.builder()
                        .withItemStack(manager.getDisplayItem(object))
                        .withClickHandler(action, c -> {
                            clickHandler.accept(new TargetContext<>(c.getPlayer(), c.getMenu(), c.getSlot(), (Action<? extends TargetContext<T>>) c.getAction(), c.isCancelled(), object));
                        })
                        .withClickHandler(Action.MIDDLE, clickContext -> {
                            manager.duplicateElementFromMenu(object);
                            listMenu.refresh(listMenu.getEntryPanel().getSlots());
                        })
                        .withClickHandler(Action.RIGHT, clickContext -> {
                            manager.deleteFromMenu(object);
                            listMenu.refresh(listMenu.getEntryPanel().getSlots());
                        }));
            }
        } else {

            for (T object : supplier.getElements()) {
                listMenu.addListEntry(Button.builder()
                        .withItemStack(supplier.getDisplayItem(object))
                        .withClickHandler(action, c -> {
                            clickHandler.accept(new TargetContext<>(c.getPlayer(), c.getMenu(), c.getSlot(), (Action<? extends TargetContext<T>>) c.getAction(), c.isCancelled(), object));
                            clickHandler.accept(new TargetContext<>(c.getPlayer(), c.getMenu(), c.getSlot(), (Action<? extends TargetContext<T>>) c.getAction(), c.isCancelled(), object));
                        }));
            }
        }
        return listMenu;
    }

    /**
     * Creates an inventory with an animation that switches the crafting recipes for this itemstack.
     * use {@link InventoryMenu#setClickHandler(int, Action, ContextConsumer)} to set ClickHandler for the
     * crafting slots. 0 = Result slot, 1 - 9 = Crafting Slots.
     * <p>
     * This renders only shaped and shapeless recipes but no furnace recipes.
     *
     * @param stack          The stack to display all recipes for
     * @param animationSpeed The tick count to wait before displaying the next recipe for this item
     * @return The menu instance
     */
    public static InventoryMenu newCraftMenu(ComponentLike title, ItemStack stack, int animationSpeed) {

        InventoryMenu workbench = new InventoryMenu(InventoryType.WORKBENCH, title);
        workbench.setItem(0, stack);

        List<Recipe> recipes = Bukkit.getRecipesFor(stack).stream()
                .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                .collect(Collectors.toList());
        ItemStack[][] animationMap = new ItemStack[9][recipes.size()];

        // No recipes -> return empty crafting table view
        if (recipes.isEmpty()) {
            return workbench;
        }

        int recipeIndex = 0;
        for (Recipe recipe : recipes) {
            if (recipe instanceof ShapedRecipe) {
                ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                String combined = concatShape(shapedRecipe.getShape());
                for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                    if (combined.charAt(slotIndex) == ' ') {
                        continue;
                    }
                    animationMap[slotIndex][recipeIndex] = shapedRecipe.getIngredientMap().get(combined.charAt(slotIndex));
                }

            } else if (recipe instanceof ShapelessRecipe) {
                int slotIndex = 0;
                for (ItemStack s : ((ShapelessRecipe) recipe).getIngredientList()) {
                    animationMap[slotIndex++][recipeIndex] = s;
                }
            }
            recipeIndex++;
        }
        if (recipes.size() > 1) {
            Animation anim = workbench.playEndlessAnimation(animationSpeed, IntStream.range(1, 10).toArray());
            for (int i = 1; i < 10; i++) {
                final int slot = i;
                workbench.setItem(i, () -> animationMap[slot - 1][anim.getInterval().get() % recipes.size()]);
            }
        } else {
            for (int slot = 1; slot < 10; slot++) {
                workbench.setItem(slot, animationMap[slot - 1][0]);
            }
        }
        return workbench;
    }

    /**
     * Creates an inventory with an animation that switches the cooking recipes for this itemstack.
     * use {@link InventoryMenu#setClickHandler(int, Action, ContextConsumer)} to set ClickHandler for the
     * crafting slots. 0 = input slot, 1 = fuel, 2 = result slot.
     *
     * @param title          The title of the furnace menu
     * @param stack          The item to display all cooking recipes for
     * @param animationSpeed The amount of ticks to wait before displaying the next recipe if the stack has multiple cooking recipes
     * @return The instance of the menu
     */
    public static InventoryMenu newCookingMenu(ComponentLike title, ItemStack stack, int animationSpeed) {

        int inputSlot = 0;
        InventoryMenu furnace = new InventoryMenu(InventoryType.FURNACE, title);
        furnace.setItem(1, new ItemStack(Material.COAL));
        furnace.setItem(2, stack);

        List<Recipe> recipes = Bukkit.getRecipesFor(stack).stream()
                .filter(recipe -> recipe instanceof FurnaceRecipe)
                .collect(Collectors.toList());
        ItemStack[] animationMap = new ItemStack[recipes.size()];

        // No recipes -> return empty crafting table view
        if (recipes.isEmpty()) {
            return furnace;
        }

        int recipeIndex = 0;
        for (Recipe recipe : recipes) {
            if (recipe instanceof FurnaceRecipe) {
                animationMap[recipeIndex] = ((FurnaceRecipe) recipe).getInput();
            }
            recipeIndex++;
        }
        if (recipes.size() > 1) {
            Animation animation = furnace.playEndlessAnimation(animationSpeed, inputSlot);
            furnace.setItem(inputSlot, () -> animationMap[animation.getInterval().get() % recipes.size()]);
        } else {
            furnace.setItem(inputSlot, animationMap[0]);
        }
        return furnace;
    }

    /**
     * @return A new Hotbar menu, consisting of a {@link BottomInventoryMenu} with the slots 0 - 8
     */
    public static BottomInventoryMenu newHotbarMenu() {
        return new BottomInventoryMenu(InventoryRow.HOTBAR);
    }

    public static <C extends TargetContext<?>> Map<Action<C>, ContextConsumer<C>> combineActions(ContextConsumer<C> contextConsumer, Action<C>... actions) {
        Map<Action<C>, ContextConsumer<C>> map = new HashMap<>();
        for (Action<C> action : actions) {
            map.put(action, contextConsumer);
        }
        return map;
    }

    private static String concatShape(String[] shape) {
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            String s = i < shape.length ? shape[i] : "   ";
            combined.append(s).append(Strings.repeat(" ", s.length() < 3 ? 3 - s.length() : 0));
        }
        return combined.toString();
    }
}
