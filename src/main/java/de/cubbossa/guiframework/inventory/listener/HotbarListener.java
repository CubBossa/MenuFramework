package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.InvMenuHandler;
import de.cubbossa.guiframework.inventory.LayeredMenu;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HotbarListener implements Listener {

	public HotbarListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ClickContext clickContext = new ClickContext(player, player.getInventory().getHeldItemSlot(), true);
		ItemStack stack = event.getItemDrop().getItemStack();

		LayeredMenu menu = InvMenuHandler.getInstance().getMenuAtSlot(player, clickContext.getSlot());

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

		switch (event.getAction()) {
			case LEFT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.LEFT_CLICK_AIR, new ClickContext(player, slot, true)));
			case RIGHT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.RIGHT_CLICK_AIR, new ClickContext(player, slot, true)));
			case LEFT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.LEFT_CLICK_BLOCK, new TargetContext<>(player, slot, true, event.getClickedBlock())));
			case RIGHT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.RIGHT_CLICK_BLOCK, new TargetContext<>(player, slot, true, event.getClickedBlock())));
		} ;
	}
}
