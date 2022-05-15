package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class ItemPlaceException extends MenuException {

	private final String player;
	private final String slot;

	public ItemPlaceException(Menu menu, Throwable t) {
		super(menu, t);
		this.player = "unknown";
		this.slot = "unknown";
	}

	public ItemPlaceException(Menu menu, Player player, int slot, Throwable t) {
		super(menu, t);
		this.player = player.getName();
		this.slot = "" + slot;
	}
}
