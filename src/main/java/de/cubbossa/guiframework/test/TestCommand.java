package de.cubbossa.guiframework.test;

import com.destroystokyo.paper.MaterialTags;
import de.cubbossa.guiframework.chat.ComponentMenu;
import de.cubbossa.guiframework.chat.ItemMenu;
import de.cubbossa.guiframework.chat.TextMenu;
import de.cubbossa.guiframework.inventory.AbstractInventoryMenu;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        CustomScoreboard scoreboard = new CustomScoreboard("test_scoreboard", Component.text("Nur ein Scoreboard"), 10);
        TextMenu online = new TextMenu("Spieler online:");
        Bukkit.getOnlinePlayers().forEach(p -> online.addSub(new ComponentMenu(p.displayName())));
        scoreboard.registerStaticEntry(3, online);
        scoreboard.registerStaticEntry(7, online.asComponent());

        CustomScoreboard higherBoard = new CustomScoreboard("test_scoreboard", Component.text("Noch eins"), 5);

        TextMenu inventory = new TextMenu("Your Inventory:");
        Arrays.stream(player.getInventory().getContents()).forEach(stack -> inventory.addSub(new ItemMenu(stack)));

        InventoryMenu exampleMenu = new InventoryMenu(4, Component.text("Example Inventory"));
        exampleMenu.loadPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 4));
        exampleMenu.loadPreset(MenuPresets.paginationRow(4, 0, 1, false, ClickType.LEFT));
        int i = 0;
        for (Material material : MaterialTags.ARROWS.getValues()) {
            exampleMenu.setButton(exampleMenu.buttonBuilder()
                            .withItemStack(material)
                            .withSound(Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 0, 2, 0, 2)
                            .withClickHandler(clickContext -> clickContext.getPlayer().getInventory().addItem(new ItemStack(material))),
                    i++);
            if (i / 9 % 4 == 0) i += 9;
        }

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
                scoreboardAnimation = scoreboard.playAnimation(3, 10, () -> Component.text("Rainbow",
                        TextColor.color(HSVLike.hsvLike(System.currentTimeMillis() % 100 / 100f, 1f, 1f))));
                break;
            case "1.6":
                scoreboardAnimation.stop();
                break;
            case "1.7":
                Bukkit.getScheduler().runTaskAsynchronously(null, () -> scoreboard.registerStaticEntry(9, Component.text("Asynchron")));
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
                exampleMenu.playAnimation(0, 100, animationContext -> {
                    exampleMenu.setItem(new ItemStack(Material.values()[(int) (System.currentTimeMillis() % Material.values().length)]));
                    //TODO nervig, lieber mit function
                });
                break;
        }
        return false;
    }
}
