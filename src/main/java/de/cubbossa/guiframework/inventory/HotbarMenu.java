package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.context.Context;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class HotbarMenu {

    //TODO

    private final Player player;

    public HotbarMenu(Player player) {
        super(9);
        this.player = player;
    }

    @Override
    public Inventory createInventory(int page) {
        return player.getInventory();
    }

    @Override
    public int[] getSlots() {
        return IntStream.range(0, 9).toArray();
    }

    @Override
    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable AbstractInventoryMenu<?, ?> previous) {
        if (!viewer.equals(player)) {
            GUIHandler.getInstance().getLogger().log(Level.SEVERE, "A hotbar menu can only be opened for one player.");
            return;
        }
        super.openInventorySynchronized(viewer, viewMode, previous);

        setClickHandler(HotbarAction.test, itemStackTargetContext -> itemStackTargetContext.getTarget(), 1);
    }

    public <C extends Context> void setClickHandler(HotbarAction<C> action, ContextConsumer<C> clickHandler, int... slots) {
        for (int slot : slots) {
            Map<HotbarAction<?>, ContextConsumer<TargetContext<?>>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            this.clickHandler.put(slot, map);
        }
    }

    public <T> void setItemAndClickHandler(ItemStack item, HotbarAction<T> action, ContextConsumer<TargetContext<T>> clickHandler, int... slots) {
        setItem(item, slots);
        setClickHandler(action, clickHandler, slots);
    }

    public <T> void setDefaultClickHandler(HotbarAction<T> action, ContextConsumer<TargetContext<T>> clickHandler) {
        defaultClickHandler.put(action, clickHandler);
    }
}
