package de.cubbossa.guiframework.chat;

import de.cubbossa.guiframework.util.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class ItemMenu extends ChatMenu<ItemStack> {

	public ItemMenu(ItemStack item) {
		super(item);
	}

	@Override
	public Component toComponent(ItemStack item) {
		return ChatUtils.toComponent(item);
	}
}
