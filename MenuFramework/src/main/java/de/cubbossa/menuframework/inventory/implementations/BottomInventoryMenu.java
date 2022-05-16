package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.inventory.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BottomInventoryMenu extends AbstractMenu implements BottomMenu {

    @Getter
    private final int[] slots;
    @Getter
    private final long slotMask;

    public BottomInventoryMenu(InventoryRow... rows) {
        // turn each row into its range from 0 -> 9 and then flatmap to one list and convert to array
        this(Arrays.stream(rows)
                .map(inventoryRow -> IntStream.range(inventoryRow.ordinal() * 9, inventoryRow.ordinal() * 9 + 9)
                        .boxed().collect(Collectors.toList()))
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .toArray());
    }

    public BottomInventoryMenu(int... slots) {
        super(slots.length);
        this.slots = Arrays.stream(slots).filter(s -> s >= 0 && s < 9 * 4).distinct().sorted().toArray();
        this.slotMask = BottomMenu.getMaskFromSlots(slots);
        addPreset(MenuPresets.fill(MenuPresets.FILLER_LIGHT));
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
    public void openSync(Player viewer, ViewMode viewMode) {
        InvMenuHandler.getInstance().registerBottomInventory(viewer, this);
        super.openSync(viewer, viewMode);
    }

    @Override
    public void handleClose(Player viewer) {
        InvMenuHandler.getInstance().closeBottomMenu(viewer, this);
        super.handleClose(viewer);
    }

    @Override
    public void refresh(int... slots) {
        refresh(true, slots);
    }

    public void refresh(boolean checkSlots, int... slots) {
        if (checkSlots) {
            viewer.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
                for (int slot : slots) {
                    if (InvMenuHandler.getInstance().getMenuAtSlot(player, slot) != this) {
                        continue;
                    }
                    player.getInventory().setItem(slot, getItemStack(slot + offset));
                }
            });
        } else {
            viewer.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
                for (int slot : slots) {
                    player.getInventory().setItem(slot, getItemStack(slot + offset));
                }
            });
        }
    }

    @Override
    public void restoreSlots(long mask) {
        refresh(false, BottomMenu.getSlotsFromMask(mask));
    }
}
