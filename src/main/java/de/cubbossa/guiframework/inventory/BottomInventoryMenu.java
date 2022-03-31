package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;

public abstract class BottomInventoryMenu<T> extends AbstractInventoryMenu<T, ClickContext> {

    //TODO

    public BottomInventoryMenu(int slotsPerPage) {
        super(slotsPerPage);
    }

    public BottomInventoryMenu() {
        super(4 * 9);
    }
}
