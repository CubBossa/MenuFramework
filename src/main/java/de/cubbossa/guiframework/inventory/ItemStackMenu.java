package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.CloseContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

abstract class ItemStackMenu {

    public enum ViewMode {
        MODIFY,
        VIEW
    }

    protected final SortedMap<Integer, ItemStack> itemStacks;
    protected final SortedMap<Integer, Consumer<Player>> soundPlayer;


    protected final SortedMap<Integer, ItemStack> dynamicItemStacks;

    @Setter
    protected ContextConsumer<CloseContext> closeHandler;

    protected final Map<Integer, Collection<AbstractInventoryMenu.Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;


    protected final int slotsPerPage;
    protected int currentPage = 0;

    protected Inventory inventory;

    public ItemStackMenu(int slotsPerPage) {

        this.itemStacks = new TreeMap<>();
        this.soundPlayer = new TreeMap<>();
        this.dynamicItemStacks = new TreeMap<>();
        this.animations = new TreeMap<>();
        this.viewer = new HashMap<>();
        this.slotsPerPage = slotsPerPage;
    }



}
