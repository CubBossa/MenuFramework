package de.cubbossa.guiframework.inventory.implementations;

import com.google.common.collect.Lists;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class RectInventoryMenu extends TopInventoryMenu {

    public interface OffsetApplier {
        int applyOffset(int slot, int offset, int rows);
    }

    public static final OffsetApplier OFFSET_TOP_RIGHT = (slot, offset, rows) -> slot + offset;
    public static final OffsetApplier OFFSET_TOP_LEFT = (slot, offset, rows) -> {
        return slot;
    };
    public static final OffsetApplier OFFSET_TOP_DOWN = (slot, offset, rows) -> {
        int invSize = (rows * 9);
        int i = slot + 9 * offset;
        while (i >= invSize) {
            i -= invSize;
            i--;
        }
        return i;
    };
    public static final OffsetApplier OFFSET_BOTTOM_UP = (slot, offset, rows) -> slot + offset; //TODO

    private static final List<InventoryType> VALID_TYPES = Lists.newArrayList(
            InventoryType.CHEST, InventoryType.BARREL, InventoryType.ENDER_CHEST, InventoryType.SHULKER_BOX
    );

    @Getter
    private final int rows;
    @Getter
    private final int[] slots;
    @Setter
    private OffsetApplier offsetApplier = OFFSET_TOP_RIGHT;
    private InventoryType type = InventoryType.CHEST;

    /**
     * Creates an empty Chest Inventory Menu
     *
     * @param rows  the row count of the chest inventory
     * @param title the title component of the chest inventory
     */
    public RectInventoryMenu(Component title, int rows) {
        super(title, rows * 9);
        this.rows = rows;
        this.slots = IntStream.range(0, rows * 9).toArray();
    }

    /**
     * Creates an empty squared Inventory Menu
     *
     * @param type  the inventory type, which has to be of type CHEST, BARREL, ENDER_CHEST or SHULKER_BOX (every rowed, 9 slot wide top inventory type)
     * @param title the title component of the chest inventory
     */
    public RectInventoryMenu(Component title, InventoryType type) {
        super(title, 3 * 9);
        if (!VALID_TYPES.contains(type)) {
            GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Only square inventory types are valid for RectInventoryMenu class.", new RuntimeException());
        } else {
            this.type = type;
        }
        this.rows = 3;
        this.slots = IntStream.range(0, rows * 9).toArray();
    }

    @Override
    public Inventory createInventory(Player player, int page) {
        return type == InventoryType.CHEST ?
                Bukkit.createInventory(null, slotsPerPage, ChatUtils.toLegacy(getTitle(page))) :
                Bukkit.createInventory(null, type, ChatUtils.toLegacy(getTitle(page)));
    }

    @Override
    protected int applyOffset(int slot) {
        return offsetApplier.applyOffset(slot, rows, offset);
    }
}
