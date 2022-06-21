package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.inventory.listener.MenuListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InvMenuHandler {

    private static final long INVENTORY_MASK = (long) Math.pow(2, 37) - 1;

    @Getter
    private static InvMenuHandler instance;

    private final Map<UUID, Stack<BottomMenu>> navigationBottomMap;
    private final Map<UUID, ItemStack[]> coveredInventories;

    private final Map<UUID, Collection<Menu>> openMenus;

    private final Set<MenuListener> listeners;

    public InvMenuHandler() {
        instance = this;

        this.navigationBottomMap = new HashMap<>();
        this.coveredInventories = new HashMap<>();
        this.openMenus = new HashMap<>();
        this.listeners = new HashSet<>();
    }

    public Collection<BottomMenu> getBottomMenus() {
        Set<BottomMenu> set = new HashSet<>();
        navigationBottomMap.forEach((uuid, menus) -> set.addAll(menus));
        return set;
    }

    public <T> Collection<T> getBottomMenus(Class<T> type) {
        Set<T> set = new HashSet<>();
        navigationBottomMap.forEach((uuid, menus) -> set.addAll((Collection<? extends T>) menus.stream().filter(menu -> menu.getClass().equals(type)).toList()));
        return set;
    }

    public Stack<BottomMenu> getBottomMenus(Player player) {
        if (!navigationBottomMap.containsKey(player.getUniqueId())) {
            return new Stack<>();
        }
        return (Stack<BottomMenu>) navigationBottomMap.get(player.getUniqueId()).clone();
    }

    public void registerBottomInventory(Player player, BottomMenu menu) {
        Stack<BottomMenu> stack = navigationBottomMap.getOrDefault(player.getUniqueId(), new Stack<>());
        //Remove from stack to put it back on top
        stack.remove(menu);

        // Create masks for all items that are already saved
        long maskBelowMenus = 0;
        for (BottomMenu layered : stack) {
            maskBelowMenus = maskBelowMenus | layered.getSlotMask();
        }
        // Find all slots that still need to be saved
        long storeItemsMask = (maskBelowMenus ^ INVENTORY_MASK) & menu.getSlotMask();
        // Save all required slots
        ItemStack[] inventory = coveredInventories.getOrDefault(player.getUniqueId(), new ItemStack[9 * 4]);
        for (int slot : BottomMenu.getSlotsFromMask(storeItemsMask)) {
            inventory[slot] = player.getInventory().getItem(slot);
        }
        coveredInventories.put(player.getUniqueId(), inventory);

        // Insert menu into stack
        navigationBottomMap.put(player.getUniqueId(), stack);
        if (stack.isEmpty() || stack.peek() != menu) {
            stack.push(menu);
        }
    }

    public void closeAllBottomMenus(Player player) {
        Stack<BottomMenu> stack = navigationBottomMap.get(player.getUniqueId());
        if (stack == null) {
            return;
        }
        for (int i = stack.size() - 1; i >= 0; i--) {
            closeBottomMenu(player, stack.get(i));
        }
    }

    public void closeCurrentBottomMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentBottomMenu);
    }

    public void closeCurrentBottomMenu(Player player) {
        if (!navigationBottomMap.containsKey(player.getUniqueId())) {
            return;
        }
        Stack<BottomMenu> stack = navigationBottomMap.get(player.getUniqueId());
        if (stack.isEmpty()) {
            return;
        }
        closeBottomMenu(player, navigationBottomMap.get(player.getUniqueId()).peek());
    }

    public void closeBottomMenu(Player player, BottomMenu bottomMenu) {
        // filter all slots of the menu that were not covered by other menus.
        // loop through each lower menu and restore the corresponding slot and reduce mask
        // after all menus restore actual inventory if mask is still not 0

        Stack<BottomMenu> menuStack = navigationBottomMap.get(player.getUniqueId());

        int index = menuStack.indexOf(bottomMenu);
        if (index == -1) {
            return;
        }

        // Create mask for all menus above
        long upperInventoryMask = 0;
        if (menuStack.size() > index + 1) {
            for (BottomMenu layered : menuStack.subList(index + 1, menuStack.size())) {
                upperInventoryMask = upperInventoryMask | layered.getSlotMask();
            }
        }
        // Filter all slots that are not covered
        long uncoveredMenuSlots = (upperInventoryMask ^ INVENTORY_MASK) & bottomMenu.getSlotMask();
        // Iterate all Menus beneath and restore
        List<BottomMenu> menusBeneath = menuStack.subList(0, index);
        Collections.reverse(menusBeneath);
        for (BottomMenu layered : menusBeneath) {
            layered.restoreSlots(uncoveredMenuSlots);
            uncoveredMenuSlots = uncoveredMenuSlots & (layered.getSlotMask() ^ INVENTORY_MASK);
            if (uncoveredMenuSlots == 0) {
                break;
            }
        }
        //Restore player inventory with remaining slots
        ItemStack[] inventory = coveredInventories.getOrDefault(player.getUniqueId(), new ItemStack[9 * 4]);
        for (int slot : BottomMenu.getSlotsFromMask(uncoveredMenuSlots)) {
            player.getInventory().setItem(slot, inventory[slot]);
        }

        menuStack.remove(bottomMenu);
    }

    public BottomMenu getMenuAtSlot(Player player, int slot) {
        Stack<BottomMenu> stack = navigationBottomMap.get(player.getUniqueId());
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (int index = stack.size() - 1; index >= 0; index--) {
            BottomMenu menu = stack.get(index);
            if (((menu.getSlotMask() >> slot) & 1) == 1) {
                return menu;
            }
        }
        return null;
    }

    public void registerMenu(Menu menu) {
        listeners.forEach(listener -> listener.register(menu));
    }

    public void unregisterMenu(Menu menu) {
        listeners.forEach(listener -> listener.unregister(menu));
    }

    public void registerListener(MenuListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(MenuListener listener) {
        listeners.remove(listener);
    }
}
