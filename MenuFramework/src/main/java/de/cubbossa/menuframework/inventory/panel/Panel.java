package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;

import java.util.List;

public interface Panel {

	int getPriority();

	int getPageSize();

	int[] getSlots();

	boolean isPanelSlot(int slot);

	void setOffset(int offset);

	int getOffset();

	Panel getParentPanel();

	void setParentPanel(Panel parentPanel);

	List<Panel> getSubPanels();

	void addSubPanel(Panel panel);

	void render(int slot) throws ItemPlaceException;

	<T> boolean perform(int slot, TargetContext<T> context) throws MenuHandlerException;
}
