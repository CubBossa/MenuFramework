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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class MenuIcon {

	private static int counter = 1;
	public static final Map<Integer, TagResolver[]> resolvers = new HashMap<>();

	private final ItemStack stack;
	private final Message name;
	private TagResolver[] nameResolver = null;
	private final Message lore;
	private TagResolver[] loreResolver = null;

	public synchronized ItemStack createItem() {

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) {
			meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
			if (meta == null) {
				throw new RuntimeException("Could not create menu icon, no meta provided.");
			}
		}
		if (name != null) {
			int nameId = nameResolver == null ? 0 : counter++;
			if (nameResolver != null && nameResolver.length > 0) {
				resolvers.put(nameId, nameResolver);
			}
			meta.setDisplayName("<message:" + name.getKey() + ";" + nameId + ">");
		}

		if (lore != null) {
			int loreId = loreResolver == null ? 0 : counter++;
			if (loreResolver != null && loreResolver.length > 0) {
				resolvers.put(loreId, loreResolver);
			}
			meta.setLore(Lists.newArrayList("<message:" + lore.getKey() + ";" + loreId + ">"));
		}

		if (counter >= Integer.MAX_VALUE - 3) {
			counter = 1;
		}

		ItemStack stack = this.stack.clone();
		stack.setItemMeta(meta);
		return stack;
	}

	public static class Builder {

		private final ItemStack stack;
		private Message name = null;
		private Message lore = null;
		private final List<TagResolver> nameResolvers = new ArrayList<>();
		private final List<TagResolver> loreResolvers = new ArrayList<>();

		public Builder(ItemStack stack) {
			this.stack = stack;
		}

		public Builder withName(Message message) {
			this.name = message;
			return this;
		}

		public Builder withLore(Message message) {
			this.lore = message;
			return this;
		}

		public Builder withNameResolver(TagResolver resolver) {
			this.nameResolvers.add(resolver);
			return this;
		}

		public Builder withLoreResolver(TagResolver resolver) {
			this.loreResolvers.add(resolver);
			return this;
		}

		public MenuIcon build() {
			return new MenuIcon(stack, name, nameResolvers.toArray(TagResolver[]::new), lore, loreResolvers.toArray(TagResolver[]::new));
		}
	}
}
