package de.cubbossa.menuframework.inventory.listener;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.context.ClickContext;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class HotbarListener implements Listener {

	public HotbarListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		int slot = player.getInventory().getHeldItemSlot();
		ItemStack stack = event.getItemDrop().getItemStack();

		LayeredMenu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
		if(menu == null) {
			return;
		}
		ClickContext clickContext = new ClickContext(player, menu, slot, Action.HOTBAR_DROP, true);
		event.setCancelled(menu.handleInteract(Action.HOTBAR_DROP, clickContext));
		if (clickContext.isCancelled()) {
			Bukkit.getScheduler().runTaskLater(GUIHandler.getInstance().getPlugin(), () -> player.getInventory().removeItem(stack), 1);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		int slot = player.getInventory().getHeldItemSlot();

		LayeredMenu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot);
		if(menu == null) {
			return;
		}

		switch (event.getAction()) {
			case LEFT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.LEFT_CLICK_AIR, new ClickContext(player, menu, slot, Action.LEFT_CLICK_AIR, true)));
			case RIGHT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.RIGHT_CLICK_AIR, new ClickContext(player, menu, slot, Action.RIGHT_CLICK_AIR, true)));
			case LEFT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.LEFT_CLICK_BLOCK, new TargetContext<>(player, menu, slot, Action.LEFT_CLICK_BLOCK, true, event.getClickedBlock())));
			case RIGHT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.RIGHT_CLICK_BLOCK, new TargetContext<>(player, menu, slot, Action.RIGHT_CLICK_BLOCK, true, event.getClickedBlock())));
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			// Bottom Inventory
			if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
				AbstractMenu menu = (AbstractMenu) InvMenuHandler.getInstance().getMenuAtSlot(player, event.getSlot());
				if (menu == null) {
					return;
				}
				Action<ClickContext> action = Action.fromClickType(event.getClick());
				event.setCancelled(menu.handleInteract(action, new ClickContext(player, menu, event.getSlot(), action, true)));
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player) {
			ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			int slot = event.getInventorySlots().stream().findAny().get();

			if (event.getInventory().getType() == InventoryType.PLAYER && event.getInventorySlots().size() == 1) {
				AbstractMenu menu = ((AbstractMenu) InvMenuHandler.getInstance().getMenuAtSlot(player, slot));
				if (menu == null) {
					return;
				}
				Action<ClickContext> action = Action.fromClickType(type);
				event.setCancelled(menu.handleInteract(Action.fromClickType(type), new ClickContext(player, menu, slot, action, true)));
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		InvMenuHandler.getInstance().closeAllBottomMenus(player);
	}
}
