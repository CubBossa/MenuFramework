package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryHandler {

    //TODO allow dynamic registration of listeners

    @Getter
    private static InventoryHandler instance;

    @Getter
    private final InventoryListener inventoryListener = new InventoryListener();

    private final Map<UUID, Stack<TopInventoryMenu>> navigationTopMap;
    private final Map<UUID, Stack<BottomInventoryMenu>> navigationBottomMap;
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

    public void registerBottomInventory(Player player, BottomInventoryMenu menu, @Nullable BottomInventoryMenu previous) {
        Stack<BottomInventoryMenu> stack = navigationBottomMap.getOrDefault(player.getUniqueId(), new Stack<>());
        if (previous != null && (stack.isEmpty() || stack.peek() != previous)) {
            stack.clear();
            stack.push(previous);
        }
        if (stack.isEmpty() || stack.peek() != menu) {
            stack.push(menu);
        }
        navigationBottomMap.put(player.getUniqueId(), stack);

        if (menu.getViewer().size() == 1) {
            inventoryListener.register(menu);
        }

        loadInventory(player, menu, previous);
    }

    public TopInventoryMenu getCurrentTopMenu(Player player) {
        Stack<TopInventoryMenu> stack = navigationTopMap.get(player.getUniqueId());
        return stack.isEmpty() ? null : stack.pop();
    }

    public BottomInventoryMenu getCurrentBottomMenu(Player player) {
        Stack<BottomInventoryMenu> stack = navigationBottomMap.get(player.getUniqueId());
        return stack.isEmpty() ? null : stack.pop();
    }

    public void closeAllMenus() {
        for (Stack<TopInventoryMenu> stack : navigationTopMap.values()) {
            if (stack.isEmpty()) continue;
            AbstractInventoryMenu open = stack.peek();
            stack.clear();
            open.closeAll();
        }
        for (Stack<BottomInventoryMenu> stack : navigationBottomMap.values()) {
            if (stack.isEmpty()) continue;
            AbstractInventoryMenu open = stack.peek();
            stack.clear();
            open.closeAll();
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

    public void closeCurrentBottomMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentBottomMenu);
    }

    public void closeCurrentBottomMenu(Player player) {
        Stack<BottomInventoryMenu> menuStack = navigationBottomMap.get(player.getUniqueId());
        BottomInventoryMenu old = null;
        BottomInventoryMenu current = null;
        if (!menuStack.isEmpty()) {
            old = menuStack.pop();
        }
        if (!menuStack.isEmpty()) {
            current = menuStack.peek();
        }
        loadInventory(player, old, current);
        if(current != null) {
            current.open(player);
        }
    }

    private void loadInventory(Player player, @Nullable BottomInventoryMenu previous, @Nullable BottomInventoryMenu current) {
        int[] storeInventoryMask = createMask(current == null ? new int[0] : current.getRows(),
                previous == null ? new int[0] : previous.getRows());
        int[] loadInventoryMask = createMask(previous == null ? new int[0] : previous.getRows(),
                current == null ? new int[0] : current.getRows());

        ItemStack[] store = coveredInventories.getOrDefault(player.getUniqueId(), new ItemStack[9 * 4]);
        for (int row : storeInventoryMask) {
            for (int i = 0; i < 9; i++) {
                int slot = row * 9 + i;
                store[slot] = player.getInventory().getItem(slot);
            }
        }
        for (int row : loadInventoryMask) {
            for (int i = 0; i < 9; i++) {
                int slot = row * 9 + i;
                player.getInventory().setItem(slot, store[slot]);
            }
        }
    }

    private int[] createMask(int[] mask, int[] substract) {
        List<Integer> i = Arrays.stream(substract).boxed().toList();
        return Arrays.stream(mask).filter(value -> !i.contains(value)).toArray();
    }
}
