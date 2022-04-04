package de.bossascrew.guiframework.examples.lobby;

import de.bossascrew.guiframework.examples.system.ServerHandler;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GameSelectorModule implements CommandExecutor {

    InventoryMenu gameSelector;

    public GameSelectorModule(JavaPlugin plugin) {

        gameSelector = new InventoryMenu(3, Component.text("Choose A Game"));
        gameSelector.loadPreset(MenuPresets.fill(MenuPresets.FILLER_LIGHT));
        gameSelector.setButton(gameSelector.buttonBuilder()
                        .withItemStack(Material.NETHERITE_SWORD, Component.text("Hierarchy", NamedTextColor.DARK_RED))
                        .withClickHandler(clickContext -> ServerHandler.getInstance().connect(clickContext.getPlayer(), "hierachy_01")),
                4);
        gameSelector.setButton(gameSelector.buttonBuilder()
                        .withItemStack(Material.GOLD_BLOCK, Component.text("Creative", NamedTextColor.GOLD))
                        .withClickHandler(clickContext -> ServerHandler.getInstance().connect(clickContext.getPlayer(), "creative_01")),
                10);
        gameSelector.setButton(gameSelector.buttonBuilder()
                        .withItemStack(Material.COBBLESTONE, Component.text("Skyblock", NamedTextColor.AQUA))
                        .withClickHandler(clickContext -> ServerHandler.getInstance().connect(clickContext.getPlayer(), "skyblock_01")),
                16);
        gameSelector.setButton(gameSelector.buttonBuilder()
                        .withItemStack(Material.EMERALD, Component.text("Lobby", NamedTextColor.GREEN))
                        .withClickHandler(clickContext -> ServerHandler.getInstance().connect(clickContext.getPlayer(), "lobby_01")),
                25);

        Objects.requireNonNull(plugin.getCommand("teleport")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            gameSelector.open(player);
        }
        return true;
    }
}
