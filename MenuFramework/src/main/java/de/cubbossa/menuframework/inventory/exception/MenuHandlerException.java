package de.cubbossa.menuframework.inventory.exception;

import de.cubbossa.menuframework.inventory.context.TargetContext;
import lombok.Getter;

@Getter
public class MenuHandlerException extends MenuException {

	private final TargetContext<?> context;

	public MenuHandlerException(TargetContext<?> context, Throwable t) {
		super(context.getMenu(), t);
		this.context = context;
	}
}
