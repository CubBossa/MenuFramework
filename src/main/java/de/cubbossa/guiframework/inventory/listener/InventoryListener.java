package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.InvMenuHandler;
import de.cubbossa.guiframework.inventory.Menu;
import de.cubbossa.guiframework.inventory.MenuListener;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;

import java.util.HashSet;
import java.util.Set;

public class InventoryListener implements MenuListener {

	private final Set<Menu> menus = new HashSet<>();

	public InventoryListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
		InvMenuHandler.getInstance().registerListener(this);
	}

	@Override
	public void register(Menu menu) {
		menus.add(menu);
	}

	@Override
	public void unregister(Menu menu) {
		menus.remove(menu);
	}

	@EventHandler
	public void onCreativeClick(InventoryCreativeEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			int slot = event.getSlot();

			menus.forEach(menu -> {
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, slot, true)));
			});
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			menus.forEach(menu -> {
				if (menu.getViewer().size() == 0 || !menu.isThisInventory(event.getClickedInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(event.getClick()), new ClickContext(player, event.getSlot(), true)));
			});

		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			int slot = event.getInventorySlots().stream().findAny().get();

			menus.forEach(menu -> {
				if (menu.getViewer().size() == 0 || event.getInventorySlots().size() > 1) {
					return;
				}
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, slot, true)));
			});
		}
	}
}
