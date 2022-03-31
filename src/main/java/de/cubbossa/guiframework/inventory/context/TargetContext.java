package de.cubbossa.guiframework.inventory.context;

import lombok.Getter;
import org.bukkit.entity.Player;

public class TargetContext<T> extends ClickContext {

    @Getter
    private final T target;

    public TargetContext(Player player, int slot, boolean cancelled, T target) {
        super(player, slot, cancelled);
        this.target = target;
    }
}
