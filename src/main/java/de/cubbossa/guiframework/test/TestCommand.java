package de.cubbossa.guiframework.test;

import com.destroystokyo.paper.MaterialTags;
import de.cubbossa.guiframework.Animations;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.bossbar.CustomBossBar;
import de.cubbossa.guiframework.chat.ComponentMenu;
import de.cubbossa.guiframework.chat.TextMenu;
import de.cubbossa.guiframework.inventory.*;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import de.cubbossa.guiframework.inventory.implementations.ListMenu;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class TestCommand implements CommandExecutor {

    /*
    Testcases
    1) Scoreboards
        - show
        - hide
        - stacked
        - animations
        - thread safety
    2) Chat Menus
        - pagination
        - itemstacks
        - string component mixed
    3) Top Inventory Menus
        - setting items and clickhandlers
        - default click handlers
        - pagination
        - presets
            - pagination
            - backhandler
            - static filling
            - dynamic filling
        - sub and parent menus
        - animations
        - shared view in top menus
        - view modes


     */

    CustomScoreboard.Animation scoreboardAnimation = null;
    CustomScoreboard scoreboard = new CustomScoreboard("test_scoreboard_1", Component.text("Nur ein Scoreboard"), 10);
    CustomScoreboard higherBoard = new CustomScoreboard("test_scoreboard_2", Component.text("Noch eins"), 5);

    InventoryMenu exampleMenu = new InventoryMenu(4, Component.text("Example Inventory"));
    BottomInventoryMenu bottomMenu = new BottomInventoryMenu(1);
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

    public TestCommand() {
        exampleMenu.addPreset(MenuPresets.fill(MenuPresets.FILLER_LIGHT));
        exampleMenu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 3));
        exampleMenu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.Inventory.LEFT));
        int i = 0;
        for (Material material : MaterialTags.BEDS.getValues()) {
            exampleMenu.setButton(i++, ButtonBuilder.buttonBuilder()
                    .withItemStack(material)
                    .withSound(Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 0f, 2f, .5f, 2f)
                    .withClickHandler(clickContext -> clickContext.getPlayer().getInventory().addItem(new ItemStack(material)), Action.Inventory.RIGHT));
            if (i / 9 % 4 == 3) {
                i += 9;
            }
        }
        exampleMenu.setItem(60, new ItemStack(Material.STONE));
        exampleMenu.setItem(-70, new ItemStack(Material.STONE));
        exampleMenu.updateTitle(Component.text("Seite 2"), 1);

        bottomMenu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 1));
        bottomMenu.addPreset(MenuPresets.paginationRow(exampleMenu, 1, 0, 1, false, Action.Inventory.LEFT, Action.Inventory.RIGHT));

        for (Material material : MaterialTags.ENCHANTABLE.getValues()) {
            listMenu.addListEntry(ButtonBuilder.buttonBuilder().withItemStack(material));
        }
        for (Material material : MaterialTags.TERRACOTTA.getValues()) {
            listMenu.addListEntry(ButtonBuilder.buttonBuilder().withItemStack(material));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        TextMenu online = new TextMenu("Spieler online:");
        Bukkit.getOnlinePlayers().forEach(p -> online.addSub(new ComponentMenu(p.displayName())));
        scoreboard.setLine(3, online);
        scoreboard.setLine(7, online.asComponent());

        TextMenu inventory = new TextMenu("Your Inventory:");
        Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(stack -> inventory.addSub(new TextMenu(stack.getAmount() + "x " + stack.getType().toString())));

        switch (strings[0]) {
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
                player.sendMessage(inventory);
                break;
            case "2.3":
                inventory.send(player, Integer.parseInt(strings[1]), 3);
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
                MenuPresets.newPlayerListMenu(Component.text("Online"), 3, Action.Inventory.LEFT, playerTargetContext -> {
                    playerTargetContext.getTarget().getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
                    playerTargetContext.getPlayer().closeInventory();
                }).open(player);
                break;

            case "6.1":
                customBossBar.show(player);
                break;

            case "7.1":
                HotbarMenu hotbarMenu = new HotbarMenu(player);
                hotbarMenu.setButton(4, ButtonBuilder.buttonBuilder()
                        .withClickHandler(Action.Hotbar.LEFT_CLICK_AIR, clickContext -> clickContext.getPlayer().sendMessage("lol"))
                        .withItemStack(Material.DIAMOND)
                        .withSound(Sound.ENTITY_VILLAGER_NO));
                hotbarMenu.setDefaultClickHandler(Action.Hotbar.DROP, clickContext -> hotbarMenu.close(clickContext.getPlayer()));
                hotbarMenu.open(player);
                break;

        }
        return false;
    }
}
