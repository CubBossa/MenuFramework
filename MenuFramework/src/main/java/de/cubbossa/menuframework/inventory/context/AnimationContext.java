package de.cubbossa.menuframework.inventory.context;

import de.cubbossa.menuframework.context.Context;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class AnimationContext extends Context {

    private final int slot;
    private final int interval;
    private final ItemStack item;
}
