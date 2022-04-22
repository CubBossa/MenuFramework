package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.AbstractInventoryMenu;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.inventory.ItemStackMenu;
import de.cubbossa.guiframework.inventory.LayeredMenu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class HotbarMenu extends AbstractInventoryMenu implements LayeredMenu {

    //TODO mit bottom inventory zusammenfassen

    private final Player player;
    @Getter
    private final int[] slots;
    @Getter
    private final long slotMask;

    public HotbarMenu(Player player, int... slots) {
        super(9);
        this.player = player;
        this.slots = Arrays.stream(slots).filter(s -> s >= 0 && s < 9).distinct().sorted().toArray();
        this.slotMask = InventoryHandler.getMaskFromSlots(this.slots);
    }

    @Override
    protected Inventory createInventory(Player player, int page) {
        return player.getInventory();
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {

    }

    @Override
    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable ItemStackMenu previous) {
        InventoryHandler.getInstance().registerBottomInventory(viewer, this);
        super.openInventorySynchronized(viewer, viewMode, previous);
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        InventoryHandler.getInstance().closeBottomMenu(player, this);
    }

    public boolean isThisInventory(Inventory inventory, Player player) {
        return this.inventory != null && this.inventory.equals(inventory);
    }

    @Override
    public void clearContent() {
        //Don't clear content so the HotbarMenuHandler can store the current player items
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        //NBTItem i = new NBTItem(item);
        //i.setBoolean("pickup-protection", true);
        //super.setItem(slot, i.getItem());
        super.setItem(slot, item); //TODO pickup protection
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
