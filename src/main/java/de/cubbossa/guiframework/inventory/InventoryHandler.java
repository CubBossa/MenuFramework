package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryHandler {

    @Getter
    private static InventoryHandler instance;

    @Getter
    private final InventoryListener inventoryListener = new InventoryListener();

    private final Map<UUID, AbstractInventoryMenu<?, ?>> openTopInventories;
    private final Map<UUID, Stack<AbstractInventoryMenu<?, ?>>> navigationTopMap;

    private final Map<UUID, AbstractInventoryMenu<?, ?>> openBottomInventories;
    private final Map<UUID, Stack<AbstractInventoryMenu<?, ?>>> navigationBottomMap;

    public InventoryHandler() {
        instance = this;

        this.openTopInventories = new HashMap<>();
        this.navigationTopMap = new HashMap<>();

        this.openBottomInventories = new HashMap<>();
        this.navigationBottomMap = new HashMap<>();
    }

    public void registerInventory(Player player, AbstractInventoryMenu<?, ?> menu, @Nullable AbstractInventoryMenu<?, ?> previous) {

        boolean top = menu instanceof TopInventoryMenu<?>;
        if (!top && !(menu instanceof BottomInventoryMenu<?>)) {
            return;
        }

        Map<UUID, AbstractInventoryMenu<?, ?>> openInventories = top ? openTopInventories : openBottomInventories;
        Map<UUID, Stack<AbstractInventoryMenu<?, ?>>> navigation = top ? navigationTopMap : navigationBottomMap;

        openInventories.put(player.getUniqueId(), menu);
        Stack<AbstractInventoryMenu<?, ?>> stack = navigation.getOrDefault(player.getUniqueId(), new Stack<>());
        boolean prevOnStack = !stack.isEmpty() && previous != stack.peek();
        if (previous == null || !prevOnStack) {
            stack.clear();
            if (!prevOnStack) {
                stack.add(previous);
            }
        }
        stack.push(menu);
        navigation.put(player.getUniqueId(), stack);
        if (menu instanceof TopInventoryMenu topMenu && topMenu.getViewer().size() == 1) {
            inventoryListener.register(topMenu);
        }
    }

    public <T> TopInventoryMenu<T> getCurrentTopMenu(Player player) {
        return (TopInventoryMenu<T>) openTopInventories.get(player.getUniqueId());
    }

    public <T> BottomInventoryMenu<T> getCurrentBottomMenu(Player player) {
        return (BottomInventoryMenu<T>) openBottomInventories.get(player.getUniqueId());
    }

    public void closeAllMenus() {
        for (AbstractInventoryMenu<?, ?> menu : openTopInventories.values()) {
            menu.closeAll();
        }
        for (AbstractInventoryMenu<?, ?> menu : openBottomInventories.values()) {
            menu.closeAll();
        }
    }

    public void closeCurrentTopMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentTopMenu);
    }

    public void closeCurrentTopMenu(Player player) {
        Stack<AbstractInventoryMenu<?, ?>> menuStack = navigationTopMap.get(player.getUniqueId());
        if (menuStack.isEmpty()) {
            return;
        }
        AbstractInventoryMenu<?, ?> oldMenu = openTopInventories.remove(player.getUniqueId());
        menuStack.peek().open(player);
    }

    public void unregisterTopMenuListener(TopInventoryMenu<ClickType> topMenu) {
        inventoryListener.unregister(topMenu);
    }

    public void closeCurrentBottomMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentBottomMenu);
    }

    public void closeCurrentBottomMenu(Player player) {
        Stack<AbstractInventoryMenu<?, ?>> menuStack = navigationBottomMap.get(player.getUniqueId());
        if (menuStack.isEmpty()) {
            return;
        }
        openBottomInventories.remove(player.getUniqueId());

        menuStack.peek().open(player);
    }
}
