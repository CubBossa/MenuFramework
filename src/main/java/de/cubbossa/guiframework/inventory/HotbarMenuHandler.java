package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.HotbarListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class HotbarMenuHandler {

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

        listener = new HotbarListener();
    }

    public void registerInventory(Player player, HotbarMenu menu, @Nullable HotbarMenu previous) {

        openHotbars.put(player.getUniqueId(), menu);
        Stack<HotbarMenu> stack = navigationMap.getOrDefault(player.getUniqueId(), new Stack<>());

        if(stack.isEmpty()) {
            storedInventory.put(player.getUniqueId(), Arrays.copyOf(player.getInventory().getContents(), 9));
        }

        boolean prevOnStack = !stack.isEmpty() && previous != stack.peek();
        if (previous == null || !prevOnStack) {
            stack.clear();
            if (!prevOnStack) {
                stack.add(previous);
            }
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
        if (menuStack.isEmpty()) {
            for(int slot = 0; slot < 9; slot++) {
                player.getInventory().setItem(slot, storedInventory.get(player.getUniqueId())[slot]);
            }
            return;
        }
        openHotbars.remove(player.getUniqueId());
        menuStack.peek().open(player);
    }
}
