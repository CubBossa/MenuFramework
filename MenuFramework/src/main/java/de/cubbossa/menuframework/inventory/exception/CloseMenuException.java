package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.context.CloseContext;
import lombok.Getter;

@Getter
public class CloseMenuException extends MenuException {

	private final CloseContext context;

	public CloseMenuException(CloseContext context, Throwable t) {
		super(context.getMenu(), t);
		this.context = context;
	}
}
