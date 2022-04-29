package de.cubbossa.guiframework.testing;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import de.cubbossa.guiframework.inventory.InventoryRow;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

@CommandAlias("menuframework test")
public class TestCommand extends BaseCommand {

	/* Test cases

	- Open and close Inventory Menu, BottomInventoryMenu, AnvilMenu, ListMenu, RectInventoryMenu
	- Apply each Preset
	- Submenu Hierarchy
	- Stacked BottomInventoryMenus
	- Rect Scroll Types
	- Animations
	- Workbench, Furnace Menu
	- Drop, Shift Move to other Inventory, Collect all Double Click, Creative Click
	- Default Click Handler
	- Default Cancelled
	- ViewModes
	 */


	// Open and close menu types

	InventoryMenu openInventoryMenu = new InventoryMenu(InventoryType.BREWING, Component.text("Yay"));

	@Subcommand("open InventoryMenu")
	public void openInventoryMenu(Player player) {
		openInventoryMenu.addPreset(MenuPresets.fill(new ItemStack(Material.DIAMOND)));
		openInventoryMenu.open(player);
	}

	@Subcommand("close InventoryMenu")
	public void closeInventoryMenu(Player player) {
		openInventoryMenu.close(player);
		openInventoryMenu.removeAllPresets();
	}

	BottomInventoryMenu openBottomInventoryMenu = new BottomInventoryMenu(InventoryRow.FIRST_ROW, InventoryRow.THIRD_ROW);

	@Subcommand("open BottomInventoryMenu")
	public void openBottomInventoryMenu(Player player) {
		openBottomInventoryMenu.addPreset(MenuPresets.fill(new ItemStack(Material.DIAMOND)));
		openBottomInventoryMenu.open(player);
	}

	@Subcommand("close BottomInventoryMenu")
	public void closeBottomInventoryMenu(Player player) {
		openBottomInventoryMenu.close(player);
		openBottomInventoryMenu.removeAllPresets();
	}

	AnvilMenu openAnvilMenu = new AnvilMenu(Component.text("Yay"), "Eingabe");

	@Subcommand("open AnvilMenu")
	public void openAnvilMenu(Player player) {
		openAnvilMenu.open(player);
	}

	@Subcommand("close AnvilMenu")
	public void closeAnvilMenu(Player player) {
		openAnvilMenu.close(player);
	}

	ListMenu openListMenu = new ListMenu(Component.text("Yay"), 3);

	@Subcommand("open ListMenu")
	public void openListMenu(Player player) {
		openListMenu.addPreset(MenuPresets.fill(new ItemStack(Material.DIAMOND)));
		openListMenu.open(player);
	}

	@Subcommand("close ListMenu")
	public void closeListMenu(Player player) {
		openListMenu.close(player);
		openListMenu.removeAllPresets();
	}

	RectInventoryMenu openRectInventoryMenu = new RectInventoryMenu(Component.text("Yay"), 4);

	@Subcommand("open RectInventoryMenu")
	public void openRectInventoryMenu(Player player) {
		openRectInventoryMenu.addPreset(MenuPresets.fillRowOnTop(new ItemStack(Material.DIAMOND), 3));
		openRectInventoryMenu.open(player);
	}

	@Subcommand("close RectInventoryMenu")
	public void closeRectInventoryMenu(Player player) {
		openRectInventoryMenu.close(player);
		openRectInventoryMenu.removeAllPresets();
	}


	// test all presets

}