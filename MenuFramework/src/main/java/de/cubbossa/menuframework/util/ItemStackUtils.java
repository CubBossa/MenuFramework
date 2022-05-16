package de.cubbossa.menuframework.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemStackUtils {

	public ItemStack createItemStack(Material material, Component name, @Nullable List<Component> lore) {
		ItemStack stack = new ItemStack(material);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatUtils.toLegacy(name));
		if (lore != null) {
			meta.setLore(lore.stream().map(ChatUtils::toLegacy).collect(Collectors.toList()));
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
		if (itemMeta instanceof SkullMeta) {
			((SkullMeta) itemMeta).setOwningPlayer(player);
		} else {
			throw new UnsupportedOperationException("Trying to add a skull texture to a non-playerhead item");
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
}
