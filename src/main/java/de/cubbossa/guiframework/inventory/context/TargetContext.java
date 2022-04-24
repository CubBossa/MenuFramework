package de.cubbossa.guiframework.inventory.context;

import de.cubbossa.guiframework.context.PlayerContext;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.Menu;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class TargetContext<T> extends PlayerContext {

    private final Menu menu;
    private final int slot;
    private final Action<? extends TargetContext<T>> action;
    private boolean cancelled;
    private final T target;

    public TargetContext(Player player, Menu menu, int slot, Action<? extends TargetContext<T>> action, boolean cancelled, T target) {
        super(player);
        this.menu = menu;
        this.slot = slot;
        this.action = action;
        this.cancelled = cancelled;
        this.target = target;
    }
}
