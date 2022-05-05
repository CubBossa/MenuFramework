package de.cubbossa.menuframework.inventory.context;

import de.cubbossa.menuframework.context.PlayerContext;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Menu;
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

    public static <A> TargetContext<A> recreate(TargetContext<?> context, A target) {
        return new TargetContext<A>(context.getPlayer(), context.menu, context.slot, (Action<? extends TargetContext<A>>) context.action, context.cancelled, target);
    }
}
