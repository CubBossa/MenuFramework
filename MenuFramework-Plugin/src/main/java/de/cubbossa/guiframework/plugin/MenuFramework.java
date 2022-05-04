package de.cubbossa.guiframework.plugin;

import de.cubbossa.guiframework.GUIHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuFramework extends JavaPlugin {

    @Override
    public void onEnable() {
        GUIHandler guiHandler = new GUIHandler(this);
        guiHandler.enable();
    }

    @Override
    public void onDisable() {
        GUIHandler.getInstance().disable();
    }
}
