package de.cubbossa.menuframework.inventory.listener;

import de.cubbossa.menuframework.inventory.Menu;
import org.bukkit.event.Listener;

public interface MenuListener extends Listener {

	void register(Menu menu);

	void unregister(Menu menu);
}
