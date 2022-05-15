package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.context.OpenContext;
import lombok.Getter;

@Getter
public class OpenMenuException extends MenuException {

	private final OpenContext context;

	public OpenMenuException(OpenContext c, Throwable t) {
		super(c.getMenu(), t);
		this.context = c;
	}

}
