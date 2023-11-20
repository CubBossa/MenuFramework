package de.cubbossa.menuframework.testing;

import co.aikar.commands.PaperCommandManager;
import de.cubbossa.menuframework.GUIHandler;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TestMain extends JavaPlugin {

	private BukkitAudiences audiences;
	private MiniMessage miniMessage;

	@SneakyThrows
	@Override
	public void onEnable() {

		audiences = BukkitAudiences.create(this);
		miniMessage = MiniMessage.miniMessage();

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
