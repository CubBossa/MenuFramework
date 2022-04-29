package examples.lobby;

import examples.system.BalanceChangeEvent;
import examples.system.NetworkPlayer;
import examples.system.ServerHandler;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class ScoreboardModule implements Listener {

    private Map<UUID, CustomScoreboard> playerScoreboards;

    public ScoreboardModule(JavaPlugin plugin) {
        // local map to store instances of scoreboards
        this.playerScoreboards = new HashMap<>();

        // Register as event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        scoreboard.setLine(2, Component.text("Balance:", NamedTextColor.GRAY));
        scoreboard.setLine(3, () -> Component.text(networkPlayer.getBalance(), NamedTextColor.GOLD));

        scoreboard.setLine(5, Component.text("Level:", NamedTextColor.GRAY));
        scoreboard.setLine(6, () -> Component.text(networkPlayer.getLevel(), NamedTextColor.DARK_PURPLE));

        scoreboard.setLine(8, Component.text("Votes:", NamedTextColor.GRAY));
        scoreboard.setLine(9, () -> Component.text(networkPlayer.getTotalVotes(), NamedTextColor.GREEN));

        return scoreboard;
    }
}
