package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.BottomMenu;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SimplePanel implements Panel {

    @Setter
    private int priority = 1;
    private final long slotMask;
    private final int[] slots;
    private Panel parentPanel;
    private final Map<Integer, SortedSet<Panel>> subPanels;
    @Setter
    private int offset = 0;
    private Menu menu;

    public SimplePanel(int... slots) {
        this.slots = slots;
        this.subPanels = new TreeMap<>();
        this.slotMask = BottomMenu.getMaskFromSlots(slots);
    }

    @Override
    public boolean isPanelSlot(int slot) {
		if(slot < 0) {
			return false;
		}
        return (slotMask >> slot & 1) == 1;
    }

    @Override
    public int getPageSize() {
        return slots.length;
    }

    @Override
    public void addSubPanel(int position, Panel subPanel) {
        SortedSet<Panel> set = subPanels.getOrDefault(position, new TreeSet<>(Comparator.comparingInt(Panel::getPriority)));
        set.add(subPanel);
        subPanels.put(position, set);
        subPanel.setParentPanel(this);
    }

    @Override
    public void clearSubPanels() {
        subPanels.clear();
    }

    public void setParentPanel(Panel parentPanel) {
        this.parentPanel = parentPanel;
        menu = getMenuPanel();
    }

    public List<Panel> getSubPanels() {
        return subPanels.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public void setButton(int slot, Button button) {
        addSubPanel(slot, new MenuIcon(button.getStackSupplier(),
                player -> player.playSound(player.getLocation(), button.getSound(), button.getVolume(), button.getPitch()),
                button.getClickHandler()));
    }

    private Menu getMenuPanel() {
		if (this instanceof Menu) {
			return (Menu) this;
		}
		Panel current = this;
		Collection<Panel> visited = new HashSet<>();
		while (current != null && !visited.contains(current)) {
			visited.add(current);
			current = current.getParentPanel();
			if (current instanceof Menu) {
				return (Menu) current;
			}
		}
		return null;
    }

    @Override
    public void render(int slot) throws ItemPlaceException {
		if (menu == null) {
			menu = getMenuPanel();
		}

		Panel fitting = null;
		int position = -1;
		for (Map.Entry<Integer, SortedSet<Panel>> entry : subPanels.entrySet()) {
			for (Panel panel : entry.getValue()) {
				if (panel.isPanelSlot(slot - entry.getKey()) && (fitting == null || panel.getPriority() > fitting.getPriority())) {
					fitting = panel;
					position = entry.getKey();
				}
			}
		}
		if (fitting != null) {
			if (fitting instanceof MenuIcon) {
				MenuIcon icon = (MenuIcon) fitting;
				try {
					if (menu.getInventory() != null) {
						menu.getInventory().setItem(slot, icon.getItem() != null ? icon.getItem().get().clone() : null);
					}
				} catch (Throwable t) {
					throw new ItemPlaceException(menu, t);
				}
			} else {
				fitting.render(slot - position);
			}
		}
	}

	@Override
	public <T> boolean perform(int slot, TargetContext<T> context) throws MenuHandlerException {
		int position = 0;
		Panel fitting = null;
		for (Map.Entry<Integer, SortedSet<Panel>> entry : subPanels.entrySet()) {
			for (Panel panel : entry.getValue()) {
				if (panel.isPanelSlot(slot - entry.getKey()) && (fitting == null || panel.getPriority() > fitting.getPriority())) {
					fitting = panel;
					position = entry.getKey();
					break;
				}
			}
		}
		return fitting != null && fitting.perform(position + slot, context);
	}

	@Override
	public int getMinPage() {
		int minSlot = 0;
		for (Map.Entry<Integer, SortedSet<Panel>> panels : this.subPanels.entrySet()) {
			for (Panel panel : panels.getValue()) {
				int panelSlot = panel.getMinPage() + panels.getKey();
				if (minSlot < panelSlot) {
					minSlot = panelSlot;
				}
			}
		}
		return minSlot % getPageSize();
	}

	@Override
	public int getMaxPage() {
		int maxSlot = 0;
		for (Map.Entry<Integer, SortedSet<Panel>> panels : this.subPanels.entrySet()) {
			for (Panel panel : panels.getValue()) {
				int panelSlot = panel.getMinPage() + panels.getKey();
				if (maxSlot > panelSlot) {
					maxSlot = panelSlot;
				}
			}
		}
		return maxSlot % getPageSize();
	}
}
