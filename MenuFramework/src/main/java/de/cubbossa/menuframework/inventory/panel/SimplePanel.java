package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.BottomMenu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SimplePanel implements Panel {

	private final long slotMask;
    private final int[] slots;
	@Setter
	private Panel parentPanel;
	private final List<Panel> subPanels;
    @Setter
    private int offset = 0;

    public SimplePanel(int[] slots) {
        this.slots = slots;
        this.subPanels = new ArrayList<>();
		this.slotMask = BottomMenu.getMaskFromSlots(slots);
    }

	@Override
	public boolean isPanelSlot(int slot) {
		return (slot >> slot & 1) == 1;
	}

	@Override
    public int getPageSize() {
        return slots.length;
    }

    @Override
    public void setSubPanel(Panel subPanel) {
        subPanels.add(subPanel);
    }

    @Override
    public void render(int slot) throws ItemPlaceException {
        for (Panel panel : subPanels) {
            if (panel.isPanelSlot(slot))
                panel.render(slot);
        }
    }

    @Override
    public boolean perform(int slot, TargetContext<?> context) throws MenuHandlerException {
        for (Panel panel : subPanels) {
            if (panel.isPanelSlot(slot))
                return panel.perform(slot, context);
        }
        return false;
    }
}
