package de.cubbossa.menuframework.inventory.context;

import de.cubbossa.menuframework.context.PlayerContext;
import de.cubbossa.menuframework.inventory.Menu;
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
