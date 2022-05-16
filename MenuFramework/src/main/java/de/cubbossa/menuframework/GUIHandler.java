package de.cubbossa.menuframework;

import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.exception.CloseMenuException;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import de.cubbossa.menuframework.inventory.exception.OpenMenuException;
import de.cubbossa.menuframework.inventory.listener.HotbarListener;
import de.cubbossa.menuframework.inventory.listener.InventoryListener;
import de.cubbossa.menuframework.scoreboard.CustomScoreboardHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GUIHandler {

	@Getter
	JavaPlugin plugin;

	@Getter
	private static GUIHandler instance;

	@Getter
	private BukkitAudiences audiences;

	private InventoryListener listener;
	@Getter
	@Setter
	private Consumer<Exception> exceptionHandler = t -> {
		if (t instanceof MenuHandlerException e) {
			e.getContext().setCancelled(true);
			audiences.player(e.getContext().getPlayer()).sendMessage(Component.text("Something went wrong when executing this button action. Please contact an administrator.", NamedTextColor.RED));
			getLogger().log(Level.SEVERE, "Error occured while interacting with menu " + e.getMenu().getClass() + " at slot " + e.getContext().getSlot(), t);
		} else if (t instanceof OpenMenuException e) {
			audiences.player(e.getContext().getPlayer()).sendMessage(Component.text("Something went wrong when opening a menu. Please contact an administrator.", NamedTextColor.RED));
			getLogger().log(Level.SEVERE, "Error occured while opening menu " + e.getMenu().getClass(), t);
		} else if (t instanceof ItemPlaceException e) {
			getLogger().log(Level.SEVERE, "Error occured while filling menu " + e.getMenu().getClass() + " at slot " + e.getSlot() + " for player " + e.getPlayer(), t);
		} else if (t instanceof CloseMenuException e) {
			getLogger().log(Level.SEVERE, "Error occured while closing menu " + e.getMenu().getClass(), t);
		} else {
			t.printStackTrace();
		}
	};

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
