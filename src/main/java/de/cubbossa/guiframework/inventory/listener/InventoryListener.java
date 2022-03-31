package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.inventory.TopInventoryMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class InventoryListener implements Listener {

	private final Set<TopInventoryMenu<ClickType>> menus = new HashSet<>();

	public void register(TopInventoryMenu<ClickType> menu) {
		menus.add(menu);
	}

	public void unregister(TopInventoryMenu<ClickType> menu) {
		menus.remove(menu);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		menus.forEach(menu -> {
			if (!menu.isThisInventory(event.getInventory())) {
				return;
			}
			if (event.getWhoClicked() instanceof Player player) {
				event.setCancelled(menu.handleInteract(player, event.getSlot(), event.getClick()));
			}
		});
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		menus.forEach(menu -> {
			if (!menu.isThisInventory(event.getInventory())) {
				return;
			}
			if (event.getWhoClicked() instanceof Player player) {
				if (event.getInventorySlots().size() > 1) {
					return;
				}
				ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
				event.setCancelled(menu.handleInteract(player, event.getInventorySlots().stream().findAny().get(), type));
			}
		});
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		menus.forEach(menu -> {
			if (!menu.isThisInventory(event.getInventory())) {
				return;
			}
			if (event.getPlayer() instanceof Player player) {
				menu.close(player);
			}
		});
	}
}
