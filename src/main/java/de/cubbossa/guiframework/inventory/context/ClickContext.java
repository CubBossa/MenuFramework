package de.cubbossa.guiframework.inventory.context;

import de.cubbossa.guiframework.context.PlayerContext;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class ClickContext extends PlayerContext {

	private final int slot;
	private boolean cancelled;

	public ClickContext(Player player, int slot, boolean cancelled) {
		super(player);
		this.slot = slot;
		this.cancelled = cancelled;
	}
}
