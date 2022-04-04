package de.bossascrew.guiframework.examples.lobby;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.scoreboard.CustomScoreboardHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyMain extends JavaPlugin {

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
        // Introduces a command to open a lobby selector menu.
        new LobbySelectorModule(this);
        // Introduces a command to open a game selector menu.
        new GameSelectorModule(this);

        // Required to use scoreboards
        new CustomScoreboardHandler();
        // Displays player variables on a scoreboard
        new ScoreboardModule(this);

        // Displays a custom boss bar
        new BossBarModule();

        //TODO hotbar of course
    }
}
