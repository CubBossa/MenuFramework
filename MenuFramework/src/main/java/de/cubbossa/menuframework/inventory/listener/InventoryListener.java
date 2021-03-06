package de.cubbossa.menuframework.inventory.listener;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.*;
import de.cubbossa.menuframework.inventory.context.ClickContext;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.VillagerMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	public void onServerStop() {
		for (Menu menu : new ArrayList<>(menus)) {
			for (Player player : menu.getViewer().keySet().stream().map(Bukkit::getPlayer).collect(Collectors.toSet())) {
				menu.close(player);
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		for (Menu menu : new ArrayList<>(menus)) {
			if (event.getPlayer() instanceof Player && menu instanceof TopInventoryMenu) {
				menu.handleClose((Player) event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onInventoryClickBottom(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
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
	public void onInventoryDragBottom(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
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
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (event.getClickedInventory() == null || player.getOpenInventory() == null) {
				return;
			}
			// Prevent shifting items into the upper menu
			if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
				if (event.getCurrentItem() == null) {
					return;
				}
				// Cancel moving to upper inventory
				if (event.getClickedInventory().equals(player.getOpenInventory().getBottomInventory())) {

					menus.forEach(menu -> {
						if (menu.getViewer().size() == 0) {
							return;
						}
						if (!menu.isThisInventory(player.getOpenInventory().getTopInventory(), player)) {
							return;
						}
						int[] slots = getShiftClickSlots(event.getCurrentItem(), player.getOpenInventory().getTopInventory());

						Bukkit.getScheduler().runTaskLater(GUIHandler.getInstance().getPlugin(), () -> {
							List<Integer> allowedSlots = new ArrayList<>();
							for (int slot : slots) {
								if (!menu.handleInteract(Action.SHIFT_INSERT, new ClickContext(player, menu, slot, Action.SHIFT_INSERT, true))) {
									allowedSlots.add(slot);
								}
							}
							long mask = BottomMenu.getMaskFromSlots(allowedSlots.stream().mapToInt(Integer::intValue).toArray());
							simulatedShiftClick(event.getCurrentItem(), player.getOpenInventory().getTopInventory(), mask);
						}, 1);
						event.setCancelled(true);
					});
				}
				// Cancel moving to lower inventory
				else {
					Menu menu = menus.stream().filter(m -> m.isThisInventory(player.getOpenInventory().getTopInventory(), player)).findFirst().orElse(null);
					if (menu == null) {
						return;
					}
					Action<ClickContext> a = event.getClick().isLeftClick() ? Action.SHIFT_LEFT : Action.SHIFT_RIGHT;
					if (menu.handleInteract(a, new ClickContext(player, menu, event.getSlot(), a, true))) {
						event.setCancelled(true);
						return;
					}
					List<Integer> slots = new ArrayList<>();
					for (int slot : getShiftClickSlots(event.getCurrentItem(), player.getInventory())) {
						if (InvMenuHandler.getInstance().getMenuAtSlot(player, slot) == null) {

							if (!menu.handleInteract(Action.SHIFT_INSERT, new ClickContext(player, menu, slot, Action.SHIFT_INSERT, true))) {
								slots.add(slot);
							}
							slots.add(slot);
						}
					}
					long mask = BottomMenu.getMaskFromSlots(slots.stream().mapToInt(Integer::intValue).toArray());
					simulatedShiftClick(event.getCurrentItem(), player.getInventory(), mask);
					event.setCancelled(true);
				}
			}
			// Prevent collecting all equal items from menu
			if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
				menus.forEach(menu -> {
					if (menu.getViewer().size() == 0) {
						return;
					}
					if (menu.isThisInventory(event.getClickedInventory(), player)) {
						event.setCancelled(true);
					}
				});
			}

			new ArrayList<>(menus).stream().filter(menu -> menu instanceof TopMenu).forEach(menu -> {
				if (menu.getViewer().size() == 0 || !menu.isThisInventory(event.getClickedInventory(), player)) {
					return;
				}
				Action<ClickContext> action = Action.fromClickType(event.getClick());

				ClickContext c = new ClickContext(player, menu, event.getSlot(), action, true);
				event.setCancelled(menu.handleInteract(action, c));
				if (event.getClick().equals(ClickType.CREATIVE) && event.isCancelled()) {
					player.updateInventory();
				}
			});
		}
	}

	private int[] getShiftClickSlots(ItemStack stack, Inventory to) {

		List<Integer> slots = new ArrayList<>();
		ItemStack[] content = to.getContents();

		for (int i = 0; i < content.length; i++) {
			ItemStack s = content[i];
			if (s == null || s.getType() == Material.AIR || s.isSimilar(stack)) {
				slots.add(i);
			}
		}
		return slots.stream().mapToInt(Integer::intValue).toArray();
	}

	private void simulatedShiftClick(ItemStack stack, Inventory to, long allowedSlots) {

		int count = stack.getAmount();
		ItemStack[] content = to.getContents();

		for (int i = 0; i < content.length; i++) {
			if ((allowedSlots >> i & 1) == 0) {
				continue;
			}
			ItemStack s = content[i];
			if (s == null || s.getType() == Material.AIR) {

				to.setItem(i, stack);
				break;

			} else if (s.isSimilar(stack)) {

				int possibleFill = s.getMaxStackSize() - s.getAmount();
				if (count <= possibleFill) {
					s.setAmount(s.getAmount() + count);
					stack.setAmount(0);
					break;
				}
				count -= possibleFill;
				s.setAmount(s.getMaxStackSize());
				stack.setAmount(stack.getAmount() - possibleFill);
			}
		}
	}

	private int[] getDoubleClickSlots(ItemStack stack, InventoryView view) {

		List<Integer> slots = new ArrayList<>();
		ItemStack[] content = view.getTopInventory().getContents();

		for (int i = 0; i < content.length; i++) {
			ItemStack s = content[i];
			if (s.isSimilar(stack)) {
				slots.add(i);
			}
		}
		return slots.stream().mapToInt(Integer::intValue).toArray();
	}

	private void simulatedDoubleClick(ItemStack stack, Inventory to, long allowedSlots) {

		ItemStack[] content = to.getContents();

		for (int i = 0; i < content.length; i++) {
			if ((allowedSlots >> i & 1) == 0) {
				continue;
			}
			ItemStack s = content[i];
			if (s.isSimilar(stack)) {
				int given = s.getAmount();
				int free = stack.getMaxStackSize() - stack.getAmount();

				if (given <= free) {
					stack.setAmount(stack.getAmount() + given);
					s.setAmount(0);
				}
				stack.setAmount(stack.getMaxStackSize());
				s.setAmount(s.getAmount() - free);
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (player.getInventory() == null || player.getOpenInventory() == null) {
				return;
			}

			ClickType type = event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
			int slot = event.getRawSlots().stream().findAny().orElse(-1);
			if (player.getOpenInventory().getInventory(slot) != player.getOpenInventory().getTopInventory()) {
				return;
			}

			new ArrayList<>(menus).stream().filter(menu -> menu instanceof TopMenu).forEach(menu -> {
				if (menu.getViewer().size() == 0 || event.getInventorySlots().size() > 1) {
					return;
				}
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				Action<ClickContext> action = Action.fromClickType(type);
				event.setCancelled(menu.handleInteract(action, new ClickContext(player, menu, slot, action, true)));
			});
		}
	}

	@EventHandler
	public void onTradeSelect(TradeSelectEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();

			new ArrayList<>(menus).stream().filter(menu -> menu instanceof VillagerMenu).forEach(menu -> {
				if (menu.getViewer().size() == 0) {
					return;
				}
				if (!menu.isThisInventory(event.getInventory(), player)) {
					return;
				}
				event.setCancelled(menu.handleInteract(VillagerMenu.TRADE_SELECT,
						new TargetContext<>(player, menu, event.getIndex(), VillagerMenu.TRADE_SELECT, false, event.getMerchant().getRecipe(event.getIndex()))));
			});
		}
	}
}
