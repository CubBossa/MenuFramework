package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.Menu;
import lombok.Getter;

@Getter
public class MenuException extends Exception {

	private final Menu menu;

	public MenuException(Menu menu, Throwable t) {
		super(t);
		this.menu = menu;
	}
}
