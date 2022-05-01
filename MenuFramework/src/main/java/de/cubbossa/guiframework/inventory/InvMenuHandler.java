package de.cubbossa.guiframework.inventory;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InvMenuHandler {

    private static final long INVENTORY_MASK = (long) Math.pow(2, 37) - 1;

    @Getter
    private static InvMenuHandler instance;

    private final Map<UUID, Stack<LayeredMenu>> navigationBottomMap;
    private final Map<UUID, ItemStack[]> coveredInventories;

    private final Set<MenuListener> listeners;

    public InvMenuHandler() {
        instance = this;

        this.navigationBottomMap = new HashMap<>();
        this.coveredInventories = new HashMap<>();
        this.listeners = new HashSet<>();
    }

    public Collection<LayeredMenu> getBottomMenus() {
        Set<LayeredMenu> set = new HashSet<>();
        navigationBottomMap.forEach((uuid, menus) -> set.addAll(menus));
        return set;
    }

    public <T> Collection<T> getBottomMenus(Class<T> type) {
        Set<T> set = new HashSet<>();
        navigationBottomMap.forEach((uuid, menus) -> set.addAll((Collection<? extends T>) menus.stream().filter(menu -> menu.getClass().equals(type)).toList()));
        return set;
    }

    public Stack<LayeredMenu> getBottomMenus(Player player) {
        if (!navigationBottomMap.containsKey(player.getUniqueId())) {
            return new Stack<>();
        }
        return (Stack<LayeredMenu>) navigationBottomMap.get(player.getUniqueId()).clone();
    }

    public void closeAllMenus() {
        for (Stack<LayeredMenu> stack : navigationBottomMap.values()) {
            while (stack.isEmpty()) {
                LayeredMenu layered = stack.peek();
                if (layered instanceof AbstractMenu menu) {
                    menu.closeAll();
                }
            }
        }
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
        for (int slot : LayeredMenu.getSlotsFromMask(storeItemsMask)) {
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
        Stack<LayeredMenu> stack = navigationBottomMap.get(player.getUniqueId());
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
        for (int slot : LayeredMenu.getSlotsFromMask(uncoveredMenuSlots)) {
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
