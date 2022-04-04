package de.bossascrew.guiframework.examples.system;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ServerHandler {

    @Getter
    private static ServerHandler instance = new ServerHandler();

    public Collection<NetworkServer> getServers(ServerType type) {
        return new HashSet<>();
    }

    public NetworkPlayer getPlayer(UUID uuid) {
        return null;
    }

    public boolean canConnect(Player player, String serverName) {
        return true;
    }

    public void connect(Player player, String serverName) {

    }

    public boolean canConnect(Player player, NetworkServer server) {
        return true;
    }

    public void connect(Player player, NetworkServer server) {

    }

    public enum ServerType {
        LOBBY
    }
}
