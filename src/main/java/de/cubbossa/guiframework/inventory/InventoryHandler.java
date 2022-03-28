package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

public class InventoryHandler {

	@Getter
	private static InventoryHandler instance;

	@Getter
	private final InventoryListener inventoryListener = new InventoryListener();

	private final Collection<AbstractInventoryMenu<?>> openInventories;
	private final Map<UUID, Stack<AbstractInventoryMenu<?>>> navigationMap;

	public InventoryHandler() {
		instance = this;

		this.openInventories = new HashSet<>();
		this.navigationMap = new HashMap<>();
	}

	public void registerInventory(Player player, AbstractInventoryMenu<?> menu, boolean clearStack) {
		openInventories.add(menu);
		Stack<AbstractInventoryMenu<?>> stack = navigationMap.getOrDefault(player.getUniqueId(), new Stack<>());
		if (clearStack) {
			stack.clear();
		}
		stack.push(menu);
		navigationMap.put(player.getUniqueId(), stack);
	}

	public void closeAllMenus() {
		for (AbstractInventoryMenu<?> menu : openInventories) {
			menu.closeAll();
		}
	}

	public void closeCurrentMenu(Collection<Player> players) {
		players.forEach(this::closeCurrentMenu);
	}

	public void closeCurrentMenu(Player player) {
		Stack<AbstractInventoryMenu<?>> menuStack = navigationMap.get(player.getUniqueId());
		if (menuStack.isEmpty()) {
			return;
		}
		AbstractInventoryMenu<?> menu = menuStack.pop();
		openInventories.remove(menu);

		menuStack.peek().open(player);
	}
}
