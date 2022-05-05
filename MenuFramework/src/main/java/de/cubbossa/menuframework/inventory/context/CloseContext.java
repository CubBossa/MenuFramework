package de.cubbossa.menuframework.inventory.context;

import de.cubbossa.menuframework.context.PlayerContext;
import de.cubbossa.menuframework.inventory.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class CloseContext extends PlayerContext {

	private final Menu menu;
	private final int page;

	public CloseContext(Player player, Menu menu, int page) {
		super(player);
		this.menu = menu;
		this.page = page;
	}
}
