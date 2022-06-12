package de.cubbossa.menuframework.protocol;

import com.google.common.collect.Lists;
import de.cubbossa.translations.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class MenuIcon {

	private final ItemStack stack;
	private final Message name;
	private TagResolver nameResolver;
	private final Message lore;
	private TagResolver loreResolver;

	public ItemStack build() {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) {
			meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
			if (meta == null) {
				throw new RuntimeException("Could not create menu icon, no meta provided.");
			}
		}
		meta.setDisplayName("<§translation:" + name.getKey() + "§>");
		meta.setLore(Lists.newArrayList("<§translation:" + lore.getKey() + "§>"));
		ItemStack stack = this.stack.clone();
		stack.setItemMeta(meta);
		return stack;
	}
}
