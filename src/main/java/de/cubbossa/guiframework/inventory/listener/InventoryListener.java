package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.AbstractMenu;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.InvMenuHandler;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class InventoryListener implements Listener {

	public InventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			// Top Inventory
			InvMenuHandler.getInstance().getTopMenus(player).forEach(menu -> {
				if (menu.getViewer().size() == 0 || !menu.isThisInventory(event.getClickedInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)));
			});
			// Bottom Inventory
			if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
				AbstractMenu menu = (AbstractMenu) InvMenuHandler.getInstance().getMenuAtSlot(player, event.getSlot());
				if (menu == null) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)));
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			int slot = event.getInventorySlots().stream().findAny().get();

			InvMenuHandler.getInstance().getTopMenus(player).forEach(menu -> {
				if (menu.getViewer().size() == 0 || event.getInventorySlots().size() > 1) {
					return;
				}
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, slot, true)));
			});
			if (event.getInventory().getType() == InventoryType.PLAYER && event.getInventorySlots().size() == 1) {
				AbstractMenu menu = ((AbstractMenu) InvMenuHandler.getInstance().getMenuAtSlot(player, slot));
				if (menu == null) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, slot, true)));
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player player) {
			InvMenuHandler.getInstance().getTopMenus().forEach(menu -> menu.close(player));
			InvMenuHandler.getInstance().closeAllBottomMenus(player);
		}
	}
}
