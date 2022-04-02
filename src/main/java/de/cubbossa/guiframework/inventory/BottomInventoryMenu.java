package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BottomInventoryMenu<T> extends AbstractInventoryMenu<T, ClickContext> {

    private final int[] rows;

    private final Map<UUID, ItemStack[]> inventoryStorage;

    /**
     * @param rows 0 is the hotbar, 1 the highest row, 2 the center row and 3 the lowest inventory row
     */
    public BottomInventoryMenu(int... rows) {
        super(rows.length * 9);
        this.rows = rows;
        this.inventoryStorage = new HashMap<>();
    }


    public BottomInventoryMenu() {
        this(1, 2, 3);
    }

    @Override
    public Inventory createInventory(Player player, int page) {
        return player.getInventory();
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
    }

    @Override
    public boolean isThisInventory(Inventory inventory, Player player) {
        return inventory.equals(player.getInventory());
    }

    @Override
    public int[] getSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row : rows) {
            slots.addAll(IntStream.range(row * 9, row * 9 + 9).boxed().collect(Collectors.toSet()));
        }
        return slots.stream().mapToInt(value -> value).toArray();
    }

    @Override
    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable AbstractInventoryMenu<?, ?> previous) {
        //TODO store inventory;
        super.openInventorySynchronized(viewer, viewMode, previous);
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        //TODO restore inventory;
    }

    @Override
    public void refresh(int... slots) {
        for (int slot : slots) {
            int realIndex = currentPage * slotsPerPage + slot;
            viewer.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
                player.getInventory().setItem(slot, itemStacks.getOrDefault(realIndex, dynamicItemStacks.get(slot)));
            });
        }
    }
}
