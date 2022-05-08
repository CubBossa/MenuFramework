package de.cubbossa.menuframework.testing;

import co.aikar.commands.PaperCommandManager;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.protocol.ProtocolLibListener;
import org.bukkit.plugin.java.JavaPlugin;

public class TestMain extends JavaPlugin {

	ProtocolLibListener listener;

	@Override
	public void onEnable() {
		GUIHandler guiHandler = new GUIHandler(this);
		guiHandler.enable();
		listener = new ProtocolLibListener(this);

		PaperCommandManager manager = new PaperCommandManager(this);

		manager.registerCommand(new TestCommand());
	}

	@Override
	public void onDisable() {
		GUIHandler.getInstance().disable();
		listener.removePacketListener();
	}
}
