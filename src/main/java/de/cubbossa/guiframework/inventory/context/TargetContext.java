package de.cubbossa.guiframework.inventory.context;

import de.cubbossa.guiframework.context.PlayerContext;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class TargetContext<T> extends PlayerContext {

    private final int slot;
    private boolean cancelled;
    private final T target;

    public TargetContext(Player player, int slot, boolean cancelled, T target) {
        super(player);
        this.slot = slot;
        this.cancelled = cancelled;
        this.target = target;
    }
}
