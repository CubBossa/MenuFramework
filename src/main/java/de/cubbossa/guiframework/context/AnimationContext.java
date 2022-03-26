package de.cubbossa.guiframework.context;

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
