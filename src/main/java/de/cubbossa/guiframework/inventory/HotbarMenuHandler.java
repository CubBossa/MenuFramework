package de.cubbossa.guiframework.inventory;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public class HotbarMenuHandler {

    @Getter
    private static HotbarMenuHandler instance;

    private Map<UUID, Stack<HotbarMenu>> navigationMap;

    public HotbarMenuHandler() {
        instance = this;

        navigationMap = new HashMap<>();
    }

    public void registerMenu(HotbarMenu hotbarMenu) {

    }

}
