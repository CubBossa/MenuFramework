package de.cubbossa.guiframework.testing;

import de.cubbossa.guiframework.GUIHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMain extends JavaPlugin {

	@Override
	public void onEnable() {
		GUIHandler guiHandler = new GUIHandler(this);
		guiHandler.enable();

		Bukkit.getPluginCommand("guitest").setExecutor(new TestCommand());
	}

	@Override
	public void onDisable() {
		GUIHandler.getInstance().disable();
	}
}
