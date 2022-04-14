package de.cubbossa.guiframework.inventory.context;

import org.bukkit.entity.Player;

public class ClickContext extends TargetContext<Void> {

	public ClickContext(Player player, int slot, boolean cancelled) {
		super(player, slot, cancelled, null);
	}
}
