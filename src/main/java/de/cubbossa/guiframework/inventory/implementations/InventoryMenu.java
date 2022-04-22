package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.inventory.AbstractMenu;
import de.cubbossa.guiframework.inventory.Menu;
import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * Defines all types of basic top inventory menus
 */
public class InventoryMenu extends TopInventoryMenu {

    private InventoryType inventoryType = InventoryType.CHEST;
    @Getter
    private int rows = 0;
    @Getter
    private final int[] slots;

    /**
     * Creates an empty Inventory Menu
     *
     * @param type  the inventory type of the top inventory
     * @param title the title component of the top inventory
     */
    public InventoryMenu(InventoryType type, Component title) {
        super(title, type.getDefaultSize());
        this.inventoryType = type;
        this.slots = IntStream.range(0, inventoryType.getDefaultSize()).toArray();
    }

    /**
     * Creates an empty Chest Inventory Menu
     *
     * @param rows  the row count of the chest inventory
     * @param title the title component of the chest inventory
     */
    public InventoryMenu(int rows, Component title) {
        super(title, rows * 9);
        this.rows = rows;
        this.slots = IntStream.range(0, rows * 9).toArray();
    }

	@Override
	protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable Menu previous) {
        if (inventory != null) {
            clearContent();
        }
        super.openInventorySynchronized(viewer, viewMode, previous);
    }

    @Override
    public void close(Player viewer) {
        super.close(viewer);
        if (this.getViewer().size() == 0) {
            InventoryHandler.getInstance().getInventoryListener().unregister(this);
        }
    }

    @Override
    public Inventory createInventory(Player player, int page) {
        return inventoryType == InventoryType.CHEST ?
                Bukkit.createInventory(null, rows * 9, getTitle(page)) :
                Bukkit.createInventory(null, inventoryType, getTitle(page));
    }
}
