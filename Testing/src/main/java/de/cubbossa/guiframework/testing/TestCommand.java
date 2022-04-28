package de.cubbossa.guiframework.testing;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.bossbar.CustomBossBar;
import de.cubbossa.guiframework.chat.ComponentMenu;
import de.cubbossa.guiframework.chat.TextMenu;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.Button;
import de.cubbossa.guiframework.inventory.InventoryRow;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.*;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import de.cubbossa.guiframework.util.Animations;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

@CommandAlias("menuframework test")
public class TestCommand extends BaseCommand {

	CustomScoreboard.Animation scoreboardAnimation = null;
	CustomScoreboard scoreboard = new CustomScoreboard("test_scoreboard_1", Component.text("Nur ein Scoreboard"), 10);
	CustomScoreboard higherBoard = new CustomScoreboard("test_scoreboard_2", Component.text("Noch eins"), 5);

	RectInventoryMenu exampleMenu = new RectInventoryMenu(Component.text("Example Inventory"), 4);
	BottomInventoryMenu bottomMenu = new BottomInventoryMenu(InventoryRow.FIRST_ROW);
	BottomInventoryMenu bottomMenu2 = new BottomInventoryMenu(IntStream.range(0, 36).filter(value -> value % 2 == 1).toArray());
	ListMenu listMenu = new ListMenu(4, Component.text("Weapons"));
	InventoryMenu craftingMenu = MenuPresets.newCraftMenu(Component.text("Craft Axe:"), new ItemStack(Material.DIAMOND_AXE), 10);

	CustomBossBar customBossBar = CustomBossBar.Builder.builder("lobby")
			.withText("Welcome to Example Empire!")
			.withSegments(BarStyle.SOLID)
			.withAnimationIntervals(300)
			.withAnimationTicks(1)
			.withColorAnimation(integer -> switch (integer / 100) {
				case 0 -> BarColor.RED;
				case 1 -> BarColor.GREEN;
				default -> BarColor.BLUE;
			})
			.withProgressAnimation(Animations.bounceProgress(100, 0, .9))
			.build();

	AnvilMenu menu = new AnvilMenu(Component.text("Lol", TextColor.color(1, 1, 0)), "Eingabe");

	public TestCommand() {

		menu.setItem(0, new ItemStack(Material.PAPER));
		menu.setItem(2, new ItemStack(Material.DIAMOND_AXE));
		menu.setClickHandler(0, Action.LEFT, clickContext -> clickContext.getPlayer().sendMessage("Hi0"));
		menu.setClickHandler(1, Action.LEFT, clickContext -> clickContext.getPlayer().sendMessage("Hi1"));
		menu.setClickHandler(2, Action.LEFT, clickContext -> clickContext.getPlayer().sendMessage("Hi2"));
		menu.setClickHandler(2, AnvilMenu.CONFIRM, clickContext -> {
			clickContext.getPlayer().sendMessage(clickContext.getTarget());
			clickContext.getPlayer().closeInventory();
		});

		exampleMenu.addPreset(MenuPresets.fill(MenuPresets.FILLER_LIGHT));
		exampleMenu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 3));
		exampleMenu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.LEFT));
		int i = 0;
		for (Material material : Tag.BEDS.getValues()) {
			exampleMenu.setButton(i++, Button.builder()
					.withItemStack(material)
					.withSound(Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 0f, 2f, .5f, 2f)
					.withClickHandler(clickContext -> clickContext.getPlayer().getInventory().addItem(new ItemStack(material)), Action.RIGHT));
			if (i / 9 % 4 == 3) {
				i += 9;
			}
		}
		exampleMenu.setItem(60, new ItemStack(Material.STONE));
		exampleMenu.setItem(-70, new ItemStack(Material.STONE));
		exampleMenu.setClickHandler(60, Action.LEFT, clickContext ->
				exampleMenu.openSubMenu(clickContext.getPlayer(), listMenu,
						MenuPresets.back(listMenu.getRows() - 1, 8, Action.LEFT)));
		exampleMenu.setClickHandler(-70, Action.LEFT, clickContext -> clickContext.getMenu().openSubMenu(clickContext.getPlayer(), menu));
		exampleMenu.updateTitle(Component.text("Seite 2"), 1);

		bottomMenu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 0));
		bottomMenu.addPreset(MenuPresets.paginationRow(exampleMenu, 0, 0, 1, false, Action.LEFT, Action.RIGHT));
		bottomMenu2.addPreset(MenuPresets.fill(new ItemStack(Material.EMERALD)));

		listMenu.addPreset(MenuPresets.fillRowOnTop(MenuPresets.FILLER_DARK, 3));
		listMenu.addPreset(MenuPresets.scrollingVertically(27, 28, false, Action.LEFT));
		for (Material material : Tag.STAIRS.getValues()) {
			listMenu.addListEntry(Button.builder().withItemStack(material));
		}
		for (Material material : Tag.TERRACOTTA.getValues()) {
			listMenu.addListEntry(Button.builder().withItemStack(material));
		}
	}

	@Default
	public boolean onCommand(CommandSender commandSender, String strings) {
		Player player = (Player) commandSender;

		TextMenu online = new TextMenu("Spieler online:");
		Bukkit.getOnlinePlayers().forEach(p -> online.addSub(new ComponentMenu(GUIHandler.getInstance().getAudiences().player(player)
				.getOrDefault(Identity.DISPLAY_NAME, Component.text(player.getName())))));
		scoreboard.setLine(3, online);
		scoreboard.setLine(7, online.asComponent());

		TextMenu inventory = new TextMenu("Your Inventory:");
		Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(stack -> inventory.addSub(new TextMenu(stack.getAmount() + "x " + stack.getType().toString())));

		switch (strings) {
			case "1.1":
				scoreboard.show(player);
				break;
			case "1.2":
				scoreboard.hide(player);
				break;
			case "1.3":
				higherBoard.show(player);
				break;
			case "1.4":
				higherBoard.hide(player);
				break;
			case "1.5":
				scoreboardAnimation = scoreboard.playAnimation(3, 200, 2, () -> {
					float val = (System.currentTimeMillis() % 1000) / 1000f;
					return Component.text("Rainbow", TextColor.color(1, val, 1 - val));
				});
				break;
			case "1.6":
				scoreboardAnimation.stop();
				break;
			case "1.7":
				Bukkit.getScheduler().runTaskAsynchronously(GUIHandler.getInstance().getPlugin(), () -> scoreboard.setLine(9, Component.text("Asynchron")));
				break;

			case "2.1":
				inventory.send(player);
				break;
			case "2.2":
				GUIHandler.getInstance().getAudiences().player(player).sendMessage(inventory);
				break;

			case "3.1":
				exampleMenu.open(player);
				break;
			case "3.2":
				exampleMenu.playAnimation(0, 10, animationContext ->
						new ItemStack(Material.values()[(int) (System.currentTimeMillis() % Material.values().length)]));
				break;

			case "4.1":
				bottomMenu.open(player);
				break;
			case "4.2":
				bottomMenu.close(player);
				break;
			case "4.3":
				bottomMenu2.open(player);
				break;
			case "4.4":
				bottomMenu2.close(player);
				break;
			case "5.1":
				listMenu.open(player);
				break;
			case "5.2":
				craftingMenu.open(player);
				break;
			case "5.3":
				MenuPresets.newCraftMenu(Component.text("Craft hand:"), player.getInventory().getItemInMainHand(), 10).open(player);
				break;
			case "5.4":
				MenuPresets.newCookingMenu(Component.text("Burn hand:"), player.getInventory().getItemInMainHand(), 10).open(player);
				break;
			case "5.5":
				MenuPresets.newPlayerListMenu(Component.text("Online"), 3, Action.LEFT, playerTargetContext -> {
					playerTargetContext.getTarget().getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
					playerTargetContext.getPlayer().closeInventory();
				}).open(player);
				break;

			case "6.1":
				customBossBar.show(player);
				break;

			case "7.1":
				BottomInventoryMenu hotbarMenu = MenuPresets.newHotbarMenu();
				hotbarMenu.setButton(4, Button.builder()
						.withClickHandler(Action.LEFT_CLICK_AIR, clickContext -> clickContext.getPlayer().sendMessage("lol"))
						.withItemStack(Material.DIAMOND)
						.withSound(Sound.ENTITY_VILLAGER_NO));
				hotbarMenu.setDefaultClickHandler(Action.HOTBAR_DROP, clickContext -> hotbarMenu.close(clickContext.getPlayer()));
				hotbarMenu.open(player);
				break;
			case "8.1":


				menu.open(player);
				break;

		}
		return false;
	}
}