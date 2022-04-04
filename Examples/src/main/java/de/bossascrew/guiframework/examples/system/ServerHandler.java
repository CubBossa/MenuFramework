package de.bossascrew.guiframework.examples.system;

import lombok.Getter;

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

    public enum ServerType {
        LOBBY
    }
}
