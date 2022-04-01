package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
public class MenuPresets {

    public static ItemStack FILLER_LIGHT = createItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack FILLER_DARK = createItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
    public static ItemStack BACK = createItemStack(Material.SPRUCE_DOOR, Component.text("Back", NamedTextColor.GOLD), null);
    public static ItemStack BACK_DISABLED = createItemStack(Material.IRON_DOOR, Component.text("Back", NamedTextColor.GRAY), null);
    public static ItemStack UP = createItemStack(Material.PAPER, Component.text("Up", NamedTextColor.WHITE, TextDecoration.UNDERLINED), null);
    public static ItemStack DOWN = createItemStack(Material.PAPER, Component.text("Down", NamedTextColor.WHITE, TextDecoration.UNDERLINED), null);
    public static ItemStack RIGHT = createItemStack(Material.PAPER, Component.text("Next", NamedTextColor.WHITE, TextDecoration.UNDERLINED), null);
    public static ItemStack LEFT = createItemStack(Material.PAPER, Component.text("Previous", NamedTextColor.WHITE, TextDecoration.UNDERLINED), null);
    public static ItemStack UP_DISABLED = createItemStack(Material.MAP, Component.text("Up", NamedTextColor.GRAY), null);
    public static ItemStack DOWN_DISABLED = createItemStack(Material.MAP, Component.text("Down", NamedTextColor.GRAY), null);
    public static ItemStack RIGHT_DISABLED = createItemStack(Material.MAP, Component.text("Next", NamedTextColor.GRAY), null);
    public static ItemStack LEFT_DISABLED = createItemStack(Material.MAP, Component.text("Previous", NamedTextColor.GRAY), null);

    public static void fill(AbstractInventoryMenu<?, ?> menu, ItemStack stack, int page) {
        menu.setItem(stack, Arrays.stream(menu.getSlots()).map(operand -> operand + page * menu.getSlotsPerPage()).toArray());
    }

    public static void fillDark(AbstractInventoryMenu<?, ?> menu, int page) {
        fill(menu, FILLER_DARK, page);
    }

    public static void fillLight(AbstractInventoryMenu<?, ?> menu, int page) {
        fill(menu, FILLER_LIGHT, page);
    }

    public static void fillRow(AbstractInventoryMenu<?, ?> menu, ItemStack stack, int line, int page) {
        int offset = page * menu.slotsPerPage + line * 9;
        menu.setItem(stack, IntStream.range(offset, offset + 9).toArray());
    }

    public static void fillColumn(AbstractInventoryMenu<?, ?> menu, ItemStack stack, int column, int page) {
        int offset = page * menu.slotsPerPage;
        menu.setItem(stack, IntStream.range(offset, offset + menu.slotsPerPage).filter(value -> value % 9 == column).toArray());
    }

    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> back(int slot, boolean disabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            placeDynamicItem.accept(slot, disabled ? BACK_DISABLED : BACK);
            placeDynamicClickHandler.accept(slot, populate(c -> menu.close(c.getPlayer()), actions));
        };
    }

    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> paginationRow(int row, int leftSlot, int rightSlot, boolean hideDisabled, T... actions) {
        return (menu, placeDynamicItem, placeDynamicClickHandler) -> {
            boolean leftLimit = menu.getCurrentPage() == menu.getMinPage();
            boolean rightLimit = menu.getCurrentPage() == menu.getMaxPage();
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

    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fillRow(ItemStack stack, int line) {
        return (menu1, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(line * 9, line * 9 + 9).forEach(value -> placeDynamicItem.accept(value, stack));
        };
    }

    public static <T, C extends ClickContext> DynamicMenuProcessor<T, C> fillColumn(ItemStack stack, int column) {
        return (menu1, placeDynamicItem, placeDynamicClickHandler) -> {
            IntStream.range(0, 6*9).filter(value -> value % 9 == column).forEach(value -> placeDynamicItem.accept(value, stack));
        };
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
