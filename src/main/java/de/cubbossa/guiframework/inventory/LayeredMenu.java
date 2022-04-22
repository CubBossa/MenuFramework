package de.cubbossa.guiframework.inventory;

public interface LayeredMenu {

	long getSlotMask();

	void restoreSlots(long mask);
}
