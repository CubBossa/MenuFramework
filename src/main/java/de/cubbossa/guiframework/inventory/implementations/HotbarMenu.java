package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.AbstractInventoryMenu;
import de.cubbossa.guiframework.inventory.HotbarMenuHandler;
import de.cubbossa.guiframework.inventory.ItemStackMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class HotbarMenu extends AbstractInventoryMenu {

    private final Player player;

    public HotbarMenu(Player player) {
        super(9);
        this.player = player;
    }

    @Override
    public int[] getSlots() {
        return IntStream.range(0, 9).toArray();
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
        HotbarMenuHandler.getInstance().registerInventory(viewer, this, (HotbarMenu) previous);
        super.openInventorySynchronized(viewer, viewMode, previous);
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        HotbarMenuHandler.getInstance().unregisterHotbarMenuListener(this);
        HotbarMenuHandler.getInstance().closeCurrentHotbar(viewer);
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
}
