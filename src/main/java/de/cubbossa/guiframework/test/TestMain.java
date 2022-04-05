package de.cubbossa.guiframework.test;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.HotbarMenuHandler;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.scoreboard.CustomScoreboardHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMain extends JavaPlugin {

    @Override
    public void onEnable() {
        new GUIHandler(this) {
            @Override
            public MiniMessage getMiniMessage() {
                return MiniMessage.miniMessage();
            }
        };
        new CustomScoreboardHandler();
        new InventoryHandler();
        new HotbarMenuHandler();

        Bukkit.getPluginCommand("guitest").setExecutor(new TestCommand());
    }
}
