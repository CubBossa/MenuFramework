package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import org.bukkit.entity.Player;

import java.util.List;

public interface Panel {

	int getPageSize();

	int[] getSlots();

	void setOffsetInherited(boolean inherited);

	boolean isOffsetInherited();

	void setOffset(int offset);

	int getOffset();

	Panel getParentPanel();

	void setParentPanel(Panel parentPanel);

	List<Panel> getSubPanels();

	void setSubPanel(Panel subPanel);

	void render(Player player) throws ItemPlaceException;
}
