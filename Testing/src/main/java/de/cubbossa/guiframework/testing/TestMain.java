package de.cubbossa.guiframework.testing;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.PaperCommandManager;
import de.cubbossa.guiframework.GUIHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMain extends JavaPlugin {

	@Override
	public void onEnable() {
		GUIHandler guiHandler = new GUIHandler(this);
		guiHandler.enable();

		PaperCommandManager manager = new PaperCommandManager(this);

		manager.registerCommand(new TestCommand());
	}

	@Override
	public void onDisable() {
		GUIHandler.getInstance().disable();
	}
}
