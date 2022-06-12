package de.cubbossa.menuframework.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

public class InventoryListener {


	public InventoryListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter((Plugin) this,
				ListenerPriority.NORMAL,
				PacketType.Play.Client.CHAT) {


			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.CHAT) {
					PacketContainer packet = event.getPacket();
					String message = packet.getStrings().read(0);

					if (message.contains("shit")
							|| message.contains("damn")) {
						event.setCancelled(true);
						event.getPlayer().sendMessage("Bad manners!");
					}
				}
			}

		});
	}


}
