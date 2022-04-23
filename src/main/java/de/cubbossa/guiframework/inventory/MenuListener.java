package de.cubbossa.guiframework.inventory;

import org.bukkit.event.Listener;

public interface MenuListener extends Listener {

	void register(Menu menu);

	void unregister(Menu menu);
}
