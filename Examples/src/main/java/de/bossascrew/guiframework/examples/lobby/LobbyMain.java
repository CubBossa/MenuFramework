package de.bossascrew.guiframework.examples.lobby;

import de.bossascrew.guiframework.examples.system.BalanceChangeEvent;
import de.bossascrew.guiframework.examples.system.NetworkPlayer;
import de.bossascrew.guiframework.examples.system.ServerHandler;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import de.cubbossa.guiframework.scoreboard.CustomScoreboardHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyMain extends JavaPlugin implements Listener {

    private Map<UUID, CustomScoreboard> playerScoreboards;

    @Override
    public void onEnable() {
        // Required for menus and scoreboards
        new GUIHandler(this) {
            @Override
            public MiniMessage getMiniMessage() {
                return MiniMessage.miniMessage();
            }
        };
        // Required to use menus
        new InventoryHandler();
        // Required to use scoreboards
        new CustomScoreboardHandler();

        // local map to store instances of scoreboards
        this.playerScoreboards = new HashMap<>();

        // Register as event listener
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // Creating a scoreboard for a player.
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CustomScoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            scoreboard = createLobbyScoreboard(player);
            playerScoreboards.put(player.getUniqueId(), scoreboard);
        }
        scoreboard.show(player);
    }

    // Example for hiding a scoreboard from a player
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CustomScoreboard scoreboard = playerScoreboards.get(event.getPlayer().getUniqueId());
        if (scoreboard != null) {
            scoreboard.hide(event.getPlayer());
        }
    }

    // Example for updating the dynamic entries of the scoreboard.
    @EventHandler
    public void onBalanceChange(BalanceChangeEvent event) {
        CustomScoreboard scoreboard = playerScoreboards.get(event.getPlayer().getUniqueId());
        if (scoreboard == null) {
            return;
        }
        scoreboard.update(event.getPlayer());
    }

    // Example for creating a player scoreboard
    private CustomScoreboard createLobbyScoreboard(Player player) {
        NetworkPlayer networkPlayer = ServerHandler.getInstance().getPlayer(player.getUniqueId());

        CustomScoreboard scoreboard = new CustomScoreboard("lobby_board", Component.text("My Server"), 10);
        scoreboard.registerStaticEntry(2, Component.text("Balance:", NamedTextColor.GRAY));
        scoreboard.registerDynamicEntry(3, () -> Component.text(networkPlayer.getBalance(), NamedTextColor.GOLD));

        scoreboard.registerStaticEntry(5, Component.text("Level:", NamedTextColor.GRAY));
        scoreboard.registerDynamicEntry(6, () -> Component.text(networkPlayer.getLevel(), NamedTextColor.DARK_PURPLE));

        scoreboard.registerStaticEntry(8, Component.text("Votes:", NamedTextColor.GRAY));
        scoreboard.registerDynamicEntry(9, () -> Component.text(networkPlayer.getTotalVotes(), NamedTextColor.GREEN));

        return scoreboard;
    }
}
