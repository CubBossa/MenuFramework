package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.BottomMenu;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class SimplePanel implements Panel {

	@Setter
	private int priority = 1;
	private final long slotMask;
	private final int[] slots;
	private Panel parentPanel;
	private final SortedSet<Panel> subPanels;
	@Setter
	private int offset = 0;
	private Menu menu;

	public SimplePanel(int[] slots) {
		this.slots = slots;
		this.subPanels = new TreeSet<>(Comparator.comparingInt(Panel::getPriority));
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
	public void addSubPanel(Panel subPanel) {
		subPanels.add(subPanel);
		subPanel.setParentPanel(this);
	}

	public void setParentPanel(Panel parentPanel) {
		this.parentPanel = parentPanel;
		menu = getMenuPanel();
	}

	public void setButton(int slot, Button button) {

		addSubPanel(new MenuIcon(this, slot, button.getStackSupplier(),
				player -> player.playSound(player.getLocation(), button.getSound(), button.getVolume(), button.getPitch()),
				button.getClickHandler()) {

			public void render(int slot) throws ItemPlaceException {
				try {
					if (menu.getInventory() != null) {
						menu.getInventory().setItem(slot, getItem() != null ? getItem().get().clone() : null);
					}
				} catch (Throwable t) {
					throw new ItemPlaceException(menu, t);
				}
			}

			public <T> boolean perform(int slot, TargetContext<T> context) throws MenuHandlerException {
				ContextConsumer<TargetContext<T>> clickHandler = (ContextConsumer<TargetContext<T>>) getClickHandler().get(context.getAction());
				if (clickHandler == null) {
					return true;
				}
				try {
					clickHandler.accept(context);
				} catch (Throwable t) {
					throw new MenuHandlerException(context, t);
				}
				return context.isCancelled();
			}
		});
	}

	private Menu getMenuPanel() {
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
		for (Panel panel : subPanels) {
			if (panel.isPanelSlot(slot)) {
				panel.render(slot);
				break;
			}
		}
	}

	@Override
	public <T> boolean perform(int slot, TargetContext<T> context) throws MenuHandlerException {
		for (Panel panel : subPanels) {
			if (panel.isPanelSlot(slot)) {
				return panel.perform(slot, context);
			}
		}
		return false;
	}
}
