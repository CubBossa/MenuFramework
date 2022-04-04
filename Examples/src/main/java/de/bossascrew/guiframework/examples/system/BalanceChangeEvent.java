package de.bossascrew.guiframework.examples.system;

import jdk.jfr.Registered;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
@Setter
public class BalanceChangeEvent extends Event {

    private final Player player;

    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}
