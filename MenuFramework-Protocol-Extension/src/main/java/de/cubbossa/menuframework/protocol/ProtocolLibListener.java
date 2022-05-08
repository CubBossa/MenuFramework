package de.cubbossa.menuframework.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.sun.java.accessibility.util.GUIInitializedListener;
import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.InvMenuHandler;
import de.cubbossa.menuframework.inventory.Menu;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.AnvilMenu;
import de.cubbossa.menuframework.inventory.listener.MenuListener;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

public class ProtocolLibListener extends PacketAdapter implements MenuListener {

	private static final int REPAIR_COST_PROPERTY = 0;
	@Getter
	private static ProtocolLibListener instance;

	private final Collection<AnvilMenu> menus = new HashSet<>();

	public ProtocolLibListener(Plugin plugin) {
		super(plugin, PacketType.Play.Client.ITEM_NAME, PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Client.CLOSE_WINDOW);
		instance = this;
		addPacketListerner();

		InvMenuHandler.getInstance().registerListener(this);
	}

	@Override
	public void register(Menu menu) {
		if (menu instanceof AnvilMenu anvilMenu) {
			menus.add(anvilMenu);
		}
	}

	@Override
	public void unregister(Menu menu) {
		menus.remove(menu);
	}

	@Override
	public void onPacketReceiving(PacketEvent packetEvent) {
		if (packetEvent.getPacket().getType() == PacketType.Play.Client.ITEM_NAME) {
			GUIHandler.getInstance().callSynchronized(() -> handleItemNamePacket(packetEvent));
			return;
		}
	}

	public void addPacketListerner() {
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}

	public void removePacketListener() {
		ProtocolLibrary.getProtocolManager().removePacketListener(this);
	}

	public void sendSetItemPacket(AnvilMenu menu, Player player, int slot, @Nullable ItemStack itemStack) {
		if (itemStack == null) {
			itemStack = new ItemStack(Material.AIR);
		}
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
		packet.getIntegers().write(0, menu.getContainerId());
		packet.getIntegers().write(1, slot);
		packet.getItemModifier().write(0, itemStack);
		sendPacket(player, packet);
	}

	public void sendLevelCostPacket(Player player, AnvilMenu menu) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WINDOW_DATA);
		packet.getIntegers().write(0, menu.getContainerId());
		packet.getIntegers().write(1, REPAIR_COST_PROPERTY);
		packet.getIntegers().write(2, menu.getXpCosts());
		sendPacket(player, packet);
	}

	private void handleItemNamePacket(PacketEvent packetEvent) {
		PacketContainer packet = packetEvent.getPacket();
		menus.forEach(anvilMenu -> {
			anvilMenu.handleInteract(AnvilMenu.WRITE, new TargetContext<>(packetEvent.getPlayer(), anvilMenu, 0, AnvilMenu.WRITE, false, packet.getStrings().read(0)));
			sendLevelCostPacket(packetEvent.getPlayer(), anvilMenu);
		});
	}

	private void sendPacket(Player player, PacketContainer packet) {
		if (player == null) {
			return;
		}
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch (Exception exc) {
			GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while sending packet from anvil menu.", exc);
		}
	}
}
