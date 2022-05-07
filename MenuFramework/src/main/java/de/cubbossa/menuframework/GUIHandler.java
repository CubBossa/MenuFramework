package de.cubbossa.menuframework;

import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.listener.HotbarListener;
import de.cubbossa.menuframework.inventory.listener.InventoryListener;
import de.cubbossa.menuframework.scoreboard.CustomScoreboardHandler;
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

	private InventoryListener listener;

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
		if(listener != null) {
			listener.onServerStop();
		}

		this.audiences.close();
		this.audiences = null;
	}

	public void registerDefaultListeners() {
		listener = new InventoryListener();
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
