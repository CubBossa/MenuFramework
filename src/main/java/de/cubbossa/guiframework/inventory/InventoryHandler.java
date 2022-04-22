package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.guiframework.inventory.listener.HotbarListener;
import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryHandler {

    private static final long INVENTORY_MASK = (long) Math.pow(2, 37) - 1;

    //TODO allow dynamic registration of listeners

    @Getter
    private static InventoryHandler instance;

    @Getter
    private final InventoryListener inventoryListener = new InventoryListener();
    @Getter
    private final HotbarListener hotbarListener = new HotbarListener();

    private final Map<UUID, Stack<TopInventoryMenu>> navigationTopMap;
    private final Map<UUID, Stack<LayeredMenu>> navigationBottomMap;
    private final Map<UUID, ItemStack[]> coveredInventories;

    public InventoryHandler() {
        instance = this;

        this.navigationTopMap = new HashMap<>();
        this.navigationBottomMap = new HashMap<>();
        this.coveredInventories = new HashMap<>();
    }

    public void registerTopInventory(Player player, TopInventoryMenu menu, @Nullable TopInventoryMenu previous) {

        Stack<TopInventoryMenu> stack = navigationTopMap.getOrDefault(player.getUniqueId(), new Stack<>());
        if (previous != null && (stack.isEmpty() || stack.peek() != previous)) {
            stack.clear();
            stack.push(previous);
        }
        if (stack.isEmpty() || stack.peek() != menu) {
            stack.push(menu);
        }
        navigationTopMap.put(player.getUniqueId(), stack);

        if (menu.getViewer().size() == 1) {
            inventoryListener.register(menu);
        }
    }

    public TopInventoryMenu getCurrentTopMenu(Player player) {
        Stack<TopInventoryMenu> stack = navigationTopMap.get(player.getUniqueId());
        return stack.isEmpty() ? null : stack.pop();
    }

    public void closeAllMenus() {
        for (Stack<TopInventoryMenu> stack : navigationTopMap.values()) {
            if (stack.isEmpty()) {
                continue;
            }
            AbstractInventoryMenu open = stack.peek();
            stack.clear();
            open.closeAll();
        }
        for (Stack<LayeredMenu> stack : navigationBottomMap.values()) {
            while (stack.isEmpty()) {
                LayeredMenu layered = stack.peek();
                if (layered instanceof AbstractInventoryMenu menu) {
                    menu.closeAll();
                }
            }
        }
    }

    public void closeCurrentTopMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentTopMenu);
    }

    public void closeCurrentTopMenu(Player player) {
        Stack<TopInventoryMenu> menuStack = navigationTopMap.get(player.getUniqueId());
        if (!menuStack.isEmpty()) {
            menuStack.pop();
        }
        if (!menuStack.isEmpty()) {
            menuStack.peek().open(player);
        }
    }

    public void unregisterTopMenuListener(TopInventoryMenu topMenu) {
        inventoryListener.unregister(topMenu);
    }

    public void registerBottomInventory(Player player, LayeredMenu menu) {
        Stack<LayeredMenu> stack = navigationBottomMap.getOrDefault(player.getUniqueId(), new Stack<>());
        //Remove from stack to put it back on top
        stack.remove(menu);

        // Create masks for all items that are already saved
        long maskBelowMenus = 0;
        for (LayeredMenu layered : stack) {
            maskBelowMenus = maskBelowMenus | layered.getSlotMask();
        }
        // Find all slots that still need to be saved
        long storeItemsMask = (maskBelowMenus ^ INVENTORY_MASK) & menu.getSlotMask();
        // Save all required slots
        ItemStack[] inventory = coveredInventories.getOrDefault(player.getUniqueId(), new ItemStack[9 * 4]);
        for (int slot : getSlotsFromMask(storeItemsMask)) {
            inventory[slot] = player.getInventory().getItem(slot);
        }
        coveredInventories.put(player.getUniqueId(), inventory);

        // Insert menu into stack
        navigationBottomMap.put(player.getUniqueId(), stack);
        if (stack.isEmpty() || stack.peek() != menu) {
            stack.push(menu);
        }

        if (menu instanceof TopInventoryMenu aMenu && aMenu.getViewer().size() == 1) {
            inventoryListener.register(aMenu);
        } else if (menu instanceof BottomInventoryMenu aMenu && aMenu.getViewer().size() == 1) {
            hotbarListener.register(aMenu);
        }
    }

    public void closeCurrentBottomMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentBottomMenu);
    }

    public void closeCurrentBottomMenu(Player player) {
        if (!navigationBottomMap.containsKey(player.getUniqueId())) {
            return;
        }
        Stack<LayeredMenu> stack = navigationBottomMap.get(player.getUniqueId());
        if (stack.isEmpty()) {
            return;
        }
        closeBottomMenu(player, navigationBottomMap.get(player.getUniqueId()).peek());
    }

    public void closeBottomMenu(Player player, LayeredMenu bottomMenu) {
        // filter all slots of the menu that were not covered by other menus.
        // loop through each lower menu and restore the corresponding slot and reduce mask
        // after all menus restore actual inventory if mask is still not 0

        Stack<LayeredMenu> menuStack = navigationBottomMap.get(player.getUniqueId());

        int index = menuStack.indexOf(bottomMenu);
        // Create mask for all menus above
        long upperInventoryMask = 0;
        if (menuStack.size() > index + 1) {
            for (LayeredMenu layered : menuStack.subList(index + 1, menuStack.size())) {
                upperInventoryMask = upperInventoryMask | layered.getSlotMask();
            }
        }
        // Filter all slots that are not covered
        long uncoveredMenuSlots = (upperInventoryMask ^ INVENTORY_MASK) & bottomMenu.getSlotMask();
        // Iterate all Menus beneath and restore
        List<LayeredMenu> menusBeneath = menuStack.subList(0, index);
        Collections.reverse(menusBeneath);
        for (LayeredMenu layered : menusBeneath) {
            layered.restoreSlots(uncoveredMenuSlots);
            uncoveredMenuSlots = uncoveredMenuSlots & (layered.getSlotMask() ^ INVENTORY_MASK);
            if (uncoveredMenuSlots == 0) {
                break;
            }
        }
        //Restore player inventory with remaining slots
        ItemStack[] inventory = coveredInventories.getOrDefault(player.getUniqueId(), new ItemStack[9 * 4]);
        for (int slot : getSlotsFromMask(uncoveredMenuSlots)) {
            player.getInventory().setItem(slot, inventory[slot]);
        }

        menuStack.remove(bottomMenu);
    }

    public LayeredMenu getMenuAtSlot(Player player, int slot) {
        Stack<LayeredMenu> stack = navigationBottomMap.get(player.getUniqueId());
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (int index = stack.size() - 1; index >= 0; index--) {
            LayeredMenu menu = stack.get(index);
            if (((menu.getSlotMask() >> slot) & 1) == 1) {
                return menu;
            }
        }
        return null;
    }

    public static int[] getSlotsFromMask(long mask) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 9 * 4; i++) {
            if ((mask >> i & 1) == 1) {
                slots.add(i);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    public static long getMaskFromSlots(int[] slots) {
        long mask = 0;
        for (int slot : slots) {
            mask += Math.pow(2, slot);
        }
        return mask;
    }
}
