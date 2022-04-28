package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.stream.IntStream;

/**
 * Defines all types of basic top inventory menus
 */
public class InventoryMenu extends TopInventoryMenu {

    @Getter
    private final InventoryType inventoryType;
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

    @Override
    public Inventory createInventory(Player player, int page) {
        return Bukkit.createInventory(null, inventoryType, ChatUtils.toLegacy(getTitle(page)));
    }
}
