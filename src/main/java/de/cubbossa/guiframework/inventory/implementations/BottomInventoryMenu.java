package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.AbstractInventoryMenu;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.inventory.ItemStackMenu;
import de.cubbossa.guiframework.inventory.LayeredMenu;
import de.cubbossa.guiframework.util.InventoryRow;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class BottomInventoryMenu extends AbstractInventoryMenu implements LayeredMenu {

    @Getter
    private final int[] slots;
    @Getter
    private final long slotMask;

    public BottomInventoryMenu(InventoryRow... rows) {
        // turn each row into its range from 0 -> 9 and then flatmap to one list and convert to array
        this(Arrays.stream(rows)
                .map(inventoryRow -> IntStream.range(inventoryRow.ordinal() * 9, inventoryRow.ordinal() * 9 + 9)
                        .boxed().toList())
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .toArray());
    }

    public BottomInventoryMenu(int... slots) {
        super(slots.length);
        this.slots = slots;
        this.slotMask = InventoryHandler.getMaskFromSlots(slots);
    }

    public BottomInventoryMenu() {
        this(InventoryRow.FIRST_ROW, InventoryRow.SECOND_ROW, InventoryRow.THIRD_ROW);
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
        return inventory != null && inventory.equals(player.getInventory());
    }

    @Override
    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable ItemStackMenu previous) {
        InventoryHandler.getInstance().registerBottomInventory(viewer, this);
        super.openInventorySynchronized(viewer, viewMode, previous);
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        InventoryHandler.getInstance().closeBottomMenu(viewer, this);
    }

    @Override
    public void refresh(int... slots) {
        refresh(true, slots);
    }

    public void refresh(boolean checkSlots, int... slots) {
        if (checkSlots) {
            viewer.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
                for (int slot : slots) {
                    if (InventoryHandler.getInstance().getMenuAtSlot(player, slot) != this) {
                        return;
                    }
                    player.getInventory().setItem(slot, getItemStack(currentPage * slotsPerPage + slot));
                }
            });
        } else {
            viewer.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
                for (int slot : slots) {
                    player.getInventory().setItem(slot, getItemStack(currentPage * slotsPerPage + slot));
                }
            });
        }
    }

    @Override
    public void restoreSlots(long mask) {
        refresh(false, InventoryHandler.getSlotsFromMask(mask));
    }
}
