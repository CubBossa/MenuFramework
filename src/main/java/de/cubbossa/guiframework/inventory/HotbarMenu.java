package de.cubbossa.guiframework.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
        super.openInventorySynchronized(viewer, viewMode, previous);
        HotbarMenuHandler.getInstance().registerInventory(viewer, this, (HotbarMenu) previous);
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
}
