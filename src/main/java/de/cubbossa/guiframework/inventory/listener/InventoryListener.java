package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.inventory.AbstractMenu;
import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.HashSet;
import java.util.Set;

public class InventoryListener implements Listener {

	private final Set<TopInventoryMenu> menus = new HashSet<>();

	public InventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	public void register(TopInventoryMenu menu) {
		menus.add(menu);
	}

	public void unregister(TopInventoryMenu menu) {
		menus.remove(menu);

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			menus.forEach(menu -> {
				if (!menu.isThisInventory(event.getClickedInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)));
			});
			if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
				AbstractMenu menu = ((AbstractMenu) InventoryHandler.getInstance().getMenuAtSlot(player, event.getSlot()));
				if (menu == null) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)))
				;
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			int slot = event.getInventorySlots().stream().findAny().get();
			menus.forEach(menu -> {
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				if (event.getInventorySlots().size() > 1) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, slot, true)));
			});
			if (event.getInventory().getType() == InventoryType.PLAYER && event.getInventorySlots().size() == 1) {
				AbstractMenu menu = ((AbstractMenu) InventoryHandler.getInstance().getMenuAtSlot(player, slot));
				if (menu == null) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, slot, true)));
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		for (AbstractMenu menu : new HashSet<>(menus)) {
			if (event.getPlayer() instanceof Player player) {
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				menu.close(player);
			}
		}
	}
}
