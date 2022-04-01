package de.cubbossa.guiframework.test;

import de.cubbossa.guiframework.inventory.HotbarMenuHandler;
import de.cubbossa.guiframework.inventory.InventoryHandler;
import de.cubbossa.guiframework.scoreboard.CustomScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMain extends JavaPlugin {

    @Override
    public void onEnable() {
        new CustomScoreboardHandler();
        new InventoryHandler();
        new HotbarMenuHandler();

        Bukkit.getPluginCommand("guitest").setExecutor(new TestCommand());
    }
}
