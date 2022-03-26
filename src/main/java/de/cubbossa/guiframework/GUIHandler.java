package de.cubbossa.guiframework;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GUIHandler {

	@Getter
	private static GUIHandler instance;

	public abstract Plugin getPlugin();

	public abstract Logger getLogger();

}
