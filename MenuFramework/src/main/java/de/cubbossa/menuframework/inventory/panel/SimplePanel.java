package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SimplePanel implements Panel {

	private final int[] slots;
	private final List<Panel> subPanels;
	@Setter
	private Panel parentPanel;
	@Setter
	private boolean offsetInherited;
	@Setter
	private int offset = 0;

	public SimplePanel(int[] slots) {
		this.slots = slots;
		this.subPanels = new ArrayList<>();
	}

	@Override
	public int getPageSize() {
		return slots.length;
	}

	@Override
	public int getOffset() {
		return offsetInherited && parentPanel != null ? parentPanel.getOffset() + offset : offset;
	}

	@Override
	public void setSubPanel(Panel subPanel) {
		subPanels.add(subPanel);
	}

	@Override
	public void render(Player player) throws ItemPlaceException {
		for (Panel panel : subPanels) {
			panel.render(player);
		}
	}
}
