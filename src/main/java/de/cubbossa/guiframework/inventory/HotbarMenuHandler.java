package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.implementations.HotbarMenu;
import de.cubbossa.guiframework.inventory.listener.HotbarListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class HotbarMenuHandler {

    //TODO why though -> move to inventory handler

    @Getter
    private static HotbarMenuHandler instance;

    private Map<UUID, HotbarMenu> openHotbars;
    private Map<UUID, Stack<HotbarMenu>> navigationMap;
    private Map<UUID, ItemStack[]> storedInventory;

    private final HotbarListener listener;

    public HotbarMenuHandler() {
        instance = this;

        openHotbars = new HashMap<>();
        navigationMap = new HashMap<>();
        storedInventory = new HashMap<>();

        listener = new HotbarListener();
    }

    public void registerInventory(Player player, HotbarMenu menu, @Nullable HotbarMenu previous) {

        openHotbars.put(player.getUniqueId(), menu);
        Stack<HotbarMenu> stack = navigationMap.getOrDefault(player.getUniqueId(), new Stack<>());

        if (stack.isEmpty()) {
            storedInventory.put(player.getUniqueId(), Arrays.copyOf(player.getInventory().getContents(), 9));
            IntStream.range(0, 9).forEach(value -> player.getInventory().setItem(value, null));
        }

        if (!stack.isEmpty() && previous != null && previous != stack.peek()) {
            stack.clear();
            stack.push(previous);
        }
        stack.push(menu);
        navigationMap.put(player.getUniqueId(), stack);
        listener.register(menu);
    }

    public void closeAllMenus() {
        for (HotbarMenu menu : openHotbars.values()) {
            menu.closeAll();
        }
    }

    public void unregisterHotbarMenuListener(HotbarMenu topMenu) {
        listener.unregister(topMenu);
    }

    public void closeCurrentHotbar(Player player) {
        Stack<HotbarMenu> menuStack = navigationMap.get(player.getUniqueId());
        if (menuStack == null) {
            GUIHandler.getInstance().getLogger().log(Level.SEVERE, "No hotbar stack found for " + player.getName());
            return;
        }
        menuStack.pop();
        if (menuStack.isEmpty() || menuStack.peek() == null) {
            ItemStack[] items = storedInventory.getOrDefault(player.getUniqueId(), new ItemStack[9]);
            for (int slot = 0; slot < 9; slot++) {
                player.getInventory().setItem(slot, items[slot]);
            }
        } else {
            menuStack.peek().open(player);
        }
        openHotbars.remove(player.getUniqueId());
    }
}
