package de.cubbossa.guiframework.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@UtilityClass
public class ItemStackUtils {

	public ItemStack createItemStack(Material material, Component name, @Nullable List<Component> lore) {
		ItemStack stack = new ItemStack(material);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatUtils.toLegacy(name));
		if (lore != null) {
			meta.setLore(lore.stream().map(ChatUtils::toLegacy).toList());
		}
		stack.setItemMeta(meta);
		return stack;
	}

	public ItemStack createCustomHead(OfflinePlayer player) {
		return createCustomHead(new ItemStack(Material.PLAYER_HEAD, 1), player);
	}

	public ItemStack createCustomHead(OfflinePlayer player, Component name, List<Component> lore) {
		return createCustomHead(createItemStack(Material.PLAYER_HEAD, name, lore), player);
	}

	public ItemStack createCustomHead(ItemStack itemStack, OfflinePlayer player) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta instanceof SkullMeta meta) {
			meta.setOwningPlayer(player);
			itemStack.setItemMeta(meta);
		} else {
			throw new UnsupportedOperationException("Trying to add a skull texture to a non-playerhead item");
		}
		return itemStack;
	}
}
