package examples;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.Button;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.RectInventoryMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.IntStream;

public class TradeMenu {

	private final RectInventoryMenu menu;
	private boolean aAccepted = false;
	private boolean bAccepted = false;

	public TradeMenu(Player a, Player b, int rows) {

		List<Integer> aSlots = IntStream.range(0, rows * 9).filter(value -> value <= (rows - 1) * 9 || value % 9 < 4).boxed().toList();
		List<Integer> bSlots = IntStream.range(0, rows * 9).filter(value -> value <= (rows - 1) * 9 || value % 9 > 4).boxed().toList();

		// Set frame to seperate player windows
		menu = new RectInventoryMenu(Component.text("Exchange Items"), 5);
		IntStream.range(0, rows * 9).filter(value -> value > (rows - 1) * 9 || value % 9 == 4).forEach(value -> {
			menu.setItem(value, MenuPresets.FILLER_DARK);
		});
		// Make all slots for player a accessable
		aSlots.forEach(value -> {
			menu.setButton(value, Button.builder()
					.withClickHandler(clickContext -> {
						clickContext.setCancelled(clickContext.getPlayer().equals(a));
						aAccepted = false;
						bAccepted = false;
					}, Action.inventoryValues));
		});
		// Make all slots for player b accessasble
		bSlots.forEach(value -> {
			menu.setButton(value, Button.builder()
					.withClickHandler(clickContext -> {
						clickContext.setCancelled(clickContext.getPlayer().equals(b));
						aAccepted = false;
						bAccepted = false;
					}, Action.inventoryValues));
		});

		// Return all items to player inventory if closed
		menu.setCloseHandler(closeContext -> {
			if (closeContext.getPlayer().equals(a)) {
				aSlots.forEach(integer -> a.getInventory().addItem(menu.getInventory().getItem(integer)));
			} else if (closeContext.getPlayer().equals(b)) {
				bSlots.forEach(integer -> b.getInventory().addItem(menu.getInventory().getItem(integer)));
			}
		});

		Runnable confirmed = () -> {
			aSlots.forEach(integer -> b.getInventory().addItem(menu.getInventory().getItem(integer)));
			bSlots.forEach(integer -> a.getInventory().addItem(menu.getInventory().getItem(integer)));
		};

		// Accept Buttons
		menu.setButton(2, Button.builder()
				.withItemStack(() -> aAccepted ? new ItemStack(Material.LIME_CONCRETE) : new ItemStack(Material.RED_CONCRETE))
				.withClickHandler(Action.LEFT, clickContext -> {
					if (bAccepted) {
						confirmed.run();
					} else {
						aAccepted = true;
						clickContext.getMenu().refresh(clickContext.getSlot());
					}
				}));
		menu.setButton(6, Button.builder()
				.withItemStack(() -> bAccepted ? new ItemStack(Material.LIME_CONCRETE) : new ItemStack(Material.RED_CONCRETE))
				.withClickHandler(Action.LEFT, clickContext -> {
					if (aAccepted) {
						confirmed.run();
					} else {
						bAccepted = true;
						clickContext.getMenu().refresh(clickContext.getSlot());
					}
				}));

		menu.open(a);
		menu.open(b);
	}
}
