package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class InventoryHandler {

    //TODO allow dynamic registration of listeners

    @Getter
    private static InventoryHandler instance;

    @Getter
    private final InventoryListener inventoryListener = new InventoryListener();

    private final Map<UUID, AbstractInventoryMenu> openTopInventories;
    private final Map<UUID, Stack<AbstractInventoryMenu>> navigationTopMap;

    private final Map<UUID, AbstractInventoryMenu> openBottomInventories;
    private final Map<UUID, Stack<AbstractInventoryMenu>> navigationBottomMap;

    public InventoryHandler() {
        instance = this;

        this.openTopInventories = new HashMap<>();
        this.navigationTopMap = new HashMap<>();

        this.openBottomInventories = new HashMap<>();
        this.navigationBottomMap = new HashMap<>();
    }

    public void registerInventory(Player player, AbstractInventoryMenu menu, @Nullable AbstractInventoryMenu previous) {

        boolean top = menu instanceof TopInventoryMenu;
        if (!top && !(menu instanceof BottomInventoryMenu)) {
            return;
        }

        Map<UUID, AbstractInventoryMenu> openInventories = top ? openTopInventories : openBottomInventories;
        Map<UUID, Stack<AbstractInventoryMenu>> navigation = top ? navigationTopMap : navigationBottomMap;

        openInventories.put(player.getUniqueId(), menu);
        Stack<AbstractInventoryMenu> stack = navigation.getOrDefault(player.getUniqueId(), new Stack<>());
        if (previous != null && (stack.isEmpty() || stack.peek() != previous)) {
            stack.clear();
            stack.push(previous);
        }
        if (stack.isEmpty() || stack.peek() != menu) {
            stack.push(menu);
        }
        navigation.put(player.getUniqueId(), stack);

        if (menu.getViewer().size() == 1) {
            inventoryListener.register(menu);
        }
    }

    public TopInventoryMenu getCurrentTopMenu(Player player) {
        return (TopInventoryMenu) openTopInventories.get(player.getUniqueId());
    }

    public BottomInventoryMenu getCurrentBottomMenu(Player player) {
        return (BottomInventoryMenu) openBottomInventories.get(player.getUniqueId());
    }

    public void closeAllMenus() {
        for (AbstractInventoryMenu menu : openTopInventories.values()) {
            menu.closeAll();
        }
        for (AbstractInventoryMenu menu : openBottomInventories.values()) {
            menu.closeAll();
        }
    }

    public void closeCurrentTopMenu(Collection<Player> players) {
        players.forEach(this::closeCurrentTopMenu);
    }

    public void closeCurrentTopMenu(Player player) {
        openTopInventories.remove(player.getUniqueId());
        Stack<AbstractInventoryMenu> menuStack = navigationTopMap.get(player.getUniqueId());
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
        Stack<AbstractInventoryMenu> menuStack = navigationBottomMap.get(player.getUniqueId());
        if (menuStack.isEmpty()) {
            return;
        }
        openBottomInventories.remove(player.getUniqueId());

        menuStack.peek().open(player);
    }
}
