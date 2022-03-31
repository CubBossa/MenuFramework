package de.cubbossa.guiframework.inventory;

public abstract class BottomInventoryMenu<T> extends AbstractInventoryMenu<T> {

    public BottomInventoryMenu(int slotsPerPage) {
        super(slotsPerPage);
    }

    public BottomInventoryMenu() {
        super(4 * 9);
    }
}
