package de.cubbossa.guiframework.inventory.context;

import de.cubbossa.guiframework.context.PlayerContext;
import de.cubbossa.guiframework.inventory.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class OpenContext extends PlayerContext {

	private final Menu menu;

	public OpenContext(Player player, Menu menu) {
		super(player);
		this.menu = menu;
	}
}
