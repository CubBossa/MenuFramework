package de.cubbossa.menuframework.testing;

import co.aikar.commands.PaperCommandManager;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.protocol.ProtocolLibListener;
import de.cubbossa.translations.PacketTranslationHandler;
import de.cubbossa.translations.TranslationHandler;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;

public class TestMain extends JavaPlugin {

	ProtocolLibListener listener;

	private BukkitAudiences audiences;
	private MiniMessage miniMessage;

	@SneakyThrows
	@Override
	public void onEnable() {

		audiences = BukkitAudiences.create(this);
		miniMessage = MiniMessage.miniMessage();

		GUIHandler guiHandler = new GUIHandler(this);
		guiHandler.enable();

		new File(getDataFolder(), "lang/").mkdirs();
		TranslationHandler translationHandler = new TranslationHandler(this, audiences, miniMessage, new File(getDataFolder(), "lang/"));
		translationHandler.registerAnnotatedLanguageClass(Messages.class);
		translationHandler.loadLanguages();

		listener = new ProtocolLibListener(this);
		new PacketTranslationHandler(this);

		PaperCommandManager manager = new PaperCommandManager(this);

		manager.registerCommand(new TestCommand());
	}

	@Override
	public void onDisable() {
		GUIHandler.getInstance().disable();
		listener.removePacketListener();
	}
}
