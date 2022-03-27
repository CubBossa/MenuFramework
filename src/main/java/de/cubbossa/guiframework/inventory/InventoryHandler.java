package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.listener.InventoryListener;
import lombok.Getter;

public class InventoryHandler {

	@Getter
	private static InventoryHandler instance;

	@Getter
	private final InventoryListener inventoryListener = new InventoryListener();

	public InventoryHandler() {
		instance = this;
	}
}
