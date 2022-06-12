package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.inventory.TopInventoryMenu;
import de.cubbossa.menuframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
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

    /**
     * Creates an empty Inventory Menu
     *
     * @param type  the inventory type of the top inventory
     * @param title the title component of the top inventory
     */
    public InventoryMenu(InventoryType type, ComponentLike title) {
        super(title, type.getDefaultSize(), IntStream.range(0, type.getDefaultSize()).toArray());
        this.inventoryType = type;
    }

    /**
     * Creates an empty Inventory Menu.
     *
     * @param type  The inventory type of the top inventory.
     * @param title The title component of the top inventory.
     * @param slots All valid slots of this menu.
     */
    public InventoryMenu(InventoryType type, ComponentLike title, int[] slots) {
        super(title, type.getDefaultSize(), slots);
        this.inventoryType = type;
    }

    @Override
    public Inventory createInventory(Player player, int page) {
        return Bukkit.createInventory(null, inventoryType, ChatUtils.toLegacy(getTitle(page)));
    }
}
