package de.cubbossa.guiframework.inventory.context;

import de.cubbossa.guiframework.context.PlayerContext;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class CloseContext extends PlayerContext {

	private final int page;

	public CloseContext(Player player, int page) {
		super(player);
		this.page = page;
	}
}
