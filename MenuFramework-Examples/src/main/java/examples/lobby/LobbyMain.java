package examples.lobby;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.scoreboard.CustomScoreboardHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyMain extends JavaPlugin {

    private BossBarModule bossBarModule;

    @Override
    public void onEnable() {
        // Required for menus and scoreboards
        new GUIHandler(this).enable();

        // Required to use menus
        new InvMenuHandler();
        // Introduces a command to open a lobby selector menu.
        new LobbySelectorModule(this);
        // Introduces a command to open a game selector menu.
        new GameSelectorModule(this);

        // Required to use scoreboards
        new CustomScoreboardHandler();
        // Displays player variables on a scoreboard
        new ScoreboardModule(this);
        // Creates a Hotbar to run the commands for lobby and game selector
        new HotbarModule(this);

        // Displays a custom boss bar
        bossBarModule = new BossBarModule();
    }

    @Override
    public void onDisable() {
        bossBarModule.hideAll();
        GUIHandler.getInstance().disable();
    }
}
