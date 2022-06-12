package de.cubbossa.menuframework.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryListener {

	private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.builder().build();
	private static final String NAME_REGEX = "\\{\"text\":\"<message:([^>]+);([0-9]+)>\"}";
	private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
	private static final String LORE_REGEX = "\\{\"text\":\"<message:([^>]+);([0-9]+)>\"}";
	private static final Pattern LORE_PATTERN = Pattern.compile(LORE_REGEX);

	public InventoryListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(GUIHandler.getInstance().getPlugin(),
				ListenerPriority.NORMAL,
				PacketType.Play.Server.WINDOW_ITEMS) {


			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
					PacketContainer packet = event.getPacket();

					List<ItemStack> list = packet.getItemListModifier().read(0);
					for (int i = 0; i < list.size(); i++) {
						list.set(i, translateStack(list.get(i), event.getPlayer()));
						packet.getItemListModifier().write(0, list);
					}
				}
			}
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(GUIHandler.getInstance().getPlugin(),
				ListenerPriority.NORMAL,
				PacketType.Play.Server.SET_SLOT) {

			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
					PacketContainer packet = event.getPacket();
					packet.getItemModifier().write(0, translateStack(packet.getItemModifier().read(0), event.getPlayer()));
				}
			}
		});
	}

	/**
	 * {id:"iron_sword",display:{Name:'{"text":"abc"}',Lore:['{"text":"def"}']}}
	 */
	private ItemStack translateStack(ItemStack stack, Player player) {
		if (stack.getType() == Material.AIR) {
			return stack;
		}

		NBTItem item = new NBTItem(stack);
		if (item.getCompound("display") == null) {
			return stack;
		}
		NBTCompound display = item.getCompound("display");

		name:
		if (display.hasKey("Name")) {
			String name = display.getString("Name");
			Matcher matcher = NAME_PATTERN.matcher(name);
			if (!matcher.find()) {
				break name;
			}
			String messageKey = matcher.group(1);
			String resolverIdString = matcher.group(2);

			TagResolver[] resolver = resolverIdString.equals("0") ? new TagResolver[0] : MenuIcon.resolvers.get(Integer.parseInt(resolverIdString));

			display.setString("Name", SERIALIZER.serialize(TranslationHandler.getInstance()
					.translateLine(new Message(messageKey), player, resolver)));
		}

		lore:
		if (display.hasKey("Lore")) {
			List<String> lore = display.getStringList("Lore");
			if (lore.size() != 1) {
				break lore;
			}
			Matcher matcher = LORE_PATTERN.matcher(lore.get(0));
			if (!matcher.find()) {
				break lore;
			}
			String messageKey = matcher.group(1);
			String resolverIdString = matcher.group(2);

			TagResolver[] resolver = resolverIdString.equals("0") ? new TagResolver[0] : MenuIcon.resolvers.get(Integer.parseInt(resolverIdString));

			display.setObject("Lore", Lists.newArrayList(SERIALIZER.serialize(TranslationHandler.getInstance()
					.translateLine(new Message(messageKey), player, resolver))));

		}
		return item.getItem();
	}
}
