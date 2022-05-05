package de.cubbossa.menuframework.testing;

import co.aikar.commands.PaperCommandManager;
import de.cubbossa.menuframework.GUIHandler;
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
