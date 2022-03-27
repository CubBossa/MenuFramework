package de.cubbossa.guiframework.inventory;

import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class GUIHandler {

	@Getter
	JavaPlugin plugin;

	@Getter
	private static GUIHandler instance;

	public GUIHandler(JavaPlugin plugin) {
		instance = this;
		this.plugin = plugin;
	}

	public Logger getLogger() {
		return plugin.getLogger();
	}

	public abstract void callSynchronized(Runnable runnable);

	public abstract MiniMessage getMiniMessage();
}
