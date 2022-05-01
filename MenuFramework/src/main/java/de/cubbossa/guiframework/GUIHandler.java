package de.cubbossa.guiframework;

import de.cubbossa.guiframework.inventory.InvMenuHandler;
import de.cubbossa.guiframework.inventory.listener.HotbarListener;
import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import de.cubbossa.guiframework.scoreboard.CustomScoreboardHandler;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class GUIHandler {

	@Getter
	JavaPlugin plugin;

	@Getter
	private static GUIHandler instance;

	@Getter
	private BukkitAudiences audiences;

	public GUIHandler(JavaPlugin plugin) {
		instance = this;
		this.plugin = plugin;
	}

	public void enable() {
		this.audiences = BukkitAudiences.create(plugin);

		new InvMenuHandler();
		new CustomScoreboardHandler();

		registerDefaultListeners();
	}

	public void disable() {
		this.audiences.close();
		this.audiences = null;
	}

	public void registerDefaultListeners() {
		new InventoryListener();
		new HotbarListener();
	}

	public Logger getLogger() {
		return plugin.getLogger();
	}

	public void callSynchronized(Runnable runnable) {
		if(Bukkit.isPrimaryThread()) {
			runnable.run();
			return;
		}
		Bukkit.getScheduler().runTask(plugin, runnable);
	}
}
