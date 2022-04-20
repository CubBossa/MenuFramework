package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.AbstractInventoryMenu;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;

public class InventoryListener implements Listener {

	private final Set<AbstractInventoryMenu> menus = new HashSet<>();

	public InventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	public void register(AbstractInventoryMenu menu) {
		menus.add(menu);
	}

	public void unregister(AbstractInventoryMenu menu) {
		menus.remove(menu);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		menus.forEach(menu -> {
			if (event.getWhoClicked() instanceof Player player) {
				if (!menu.isThisInventory(event.getClickedInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.Inventory.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)));
			}
		});
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		menus.forEach(menu -> {
			if (event.getWhoClicked() instanceof Player player) {
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				if (event.getInventorySlots().size() > 1) {
					return;
				}
				ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
				int slot = event.getInventorySlots().stream().findAny().get();
				event.setCancelled(menu.handleInteract(Action.Inventory.fromClickType(type), new ClickContext(player, slot, true)));
			}
		});
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		for (AbstractInventoryMenu menu : new HashSet<>(menus)) {
			if (event.getPlayer() instanceof Player player) {
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				menu.close(player);
			}
		}
	}
}
