package de.cubbossa.menuframework.inventory.context;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Menu;
import org.bukkit.entity.Player;

public class ClickContext extends TargetContext<Void> {

	public ClickContext(Player player, Menu menu, int slot, Action<? extends TargetContext<Void>> action, boolean cancelled) {
		super(player, menu, slot, action, cancelled, null);
	}
}
