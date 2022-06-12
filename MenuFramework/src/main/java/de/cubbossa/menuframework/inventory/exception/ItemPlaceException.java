package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.Menu;
import lombok.Getter;

@Getter
public class ItemPlaceException extends MenuException {

	private final String slot;

	public ItemPlaceException(Menu menu, Throwable t) {
		super(menu, t);
		this.slot = "unknown";
	}

	public ItemPlaceException(Menu menu, int slot, Throwable t) {
		super(menu, t);
		this.slot = "" + slot;
	}
}
