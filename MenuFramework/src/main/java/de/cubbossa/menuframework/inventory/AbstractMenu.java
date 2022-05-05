package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.context.CloseContext;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.OpenContext;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.util.Animation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

@Getter
public abstract class AbstractMenu implements Menu, TopMenu {

    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> clickHandler;
    protected @Nullable ContextConsumer<? extends TargetContext<?>> fallbackDefaultClickHandler = null;
    protected final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> defaultClickHandler;

    protected final SortedMap<Integer, Supplier<ItemStack>> itemStacks;
    protected final SortedMap<Integer, Consumer<Player>> soundPlayer;

    protected final List<MenuPreset<? extends TargetContext<?>>> dynamicProcessors;
    protected final SortedMap<Integer, ItemStack> dynamicItemStacks;
    protected final SortedMap<Integer, ItemStack> dynamicItemStacksOnTop;
    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandler;
    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandlerOnTop;


    protected final MenuPreset.PresetApplier applier = new MenuPreset.PresetApplier(this) {
        @Override
        public void addItem(int slot, ItemStack itemStack) {
            dynamicItemStacks.put(slot, itemStack);
        }

        public void addItemOnTop(int slot, ItemStack itemStack) {
            dynamicItemStacksOnTop.put(slot, itemStack);
        }

        @Override
        public <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = dynamicClickHandler.getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            dynamicClickHandler.put(slot, map);
        }

        @Override
        public <C extends TargetContext<?>> void addClickHandlerOnTop(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = dynamicClickHandlerOnTop.getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            dynamicClickHandlerOnTop.put(slot, map);
        }
    };

    @Setter
    protected ContextConsumer<OpenContext> openHandler;
    @Setter
    protected ContextConsumer<CloseContext> closeHandler;

    protected final Map<Integer, Collection<Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;
    protected final Map<UUID, TopMenu> previous;

    protected final int slotsPerPage;
    //protected int getCurrentPage() = 0;
    protected int offset = 0;

    protected Inventory inventory;

    public AbstractMenu(int slotsPerPage) {

        this.itemStacks = new TreeMap<>();
        this.soundPlayer = new TreeMap<>();
        this.dynamicProcessors = new ArrayList<>();
        this.dynamicItemStacks = new TreeMap<>();
        this.dynamicClickHandler = new TreeMap<>();
        this.dynamicItemStacksOnTop = new TreeMap<>();
        this.dynamicClickHandlerOnTop = new TreeMap<>();
        this.animations = new TreeMap<>();
        this.viewer = new HashMap<>();
        this.previous = new HashMap<>();
        this.slotsPerPage = slotsPerPage;
        this.clickHandler = new TreeMap<>();
        this.defaultClickHandler = new HashMap<>();
    }


    public abstract int[] getSlots();

    protected abstract Inventory createInventory(Player player, int page);

    protected abstract void openInventory(Player player, Inventory inventory);

    public void firstOpen() {
        InvMenuHandler.getInstance().registerMenu(this);
    }

    public void lastClose() {
        InvMenuHandler.getInstance().unregisterMenu(this);
    }

    public void open(Player viewer) {
        open(viewer, ViewMode.MODIFY);
    }

    public void open(Player viewer, ViewMode viewMode) {
        GUIHandler.getInstance().callSynchronized(() -> openSync(viewer, viewMode));
    }

    public void open(Collection<Player> viewers) {
        GUIHandler.getInstance().callSynchronized(() -> {
            viewers.forEach(player -> openSync(player, ViewMode.MODIFY));
        });
    }

    public void open(Collection<Player> viewers, ViewMode viewMode) {
        GUIHandler.getInstance().callSynchronized(() -> {
            viewers.forEach(player -> openSync(player, viewMode));
        });
    }

    public TopMenu openSubMenu(Player player, TopMenu menu) {
        GUIHandler.getInstance().callSynchronized(() -> {
            handleClose(player);
            menu.setPrevious(player, this);
            menu.open(player);
        });
        return menu;
    }

    public TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier) {
        return openSubMenu(player, menuSupplier.get());
    }

    public TopMenu openSubMenu(Player player, TopMenu menu, MenuPreset<?> backPreset) {
        return openSubMenu(player, menu, ViewMode.MODIFY, backPreset);
    }

    public TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier, MenuPreset<?> backPreset) {
        return openSubMenu(player, menuSupplier.get(), ViewMode.MODIFY, backPreset);
    }

    public TopMenu openSubMenu(Player player, TopMenu menu, ViewMode viewMode, MenuPreset<?> backPreset) {
        GUIHandler.getInstance().callSynchronized(() -> {
            handleClose(player);
            menu.setPrevious(player, this);
            menu.addPreset(backPreset);
            menu.open(player);
        });
        return menu;
    }

    public TopMenu openSubMenu(Player player, Supplier<TopMenu> menuSupplier, ViewMode viewMode, MenuPreset<?> backPreset) {
        return openSubMenu(player, menuSupplier.get(), viewMode, backPreset);
    }

    @Override
    public void setPrevious(Player player, TopMenu previous) {
        this.previous.put(player.getUniqueId(), previous);
    }

    @Override
    public @Nullable TopMenu getPrevious(Player player) {
        return this.previous.get(player.getUniqueId());
    }

    public void setNextPage(Player player) {
        addOffset(player, slotsPerPage);
    }

    public void setPreviousPage(Player player) {
        removeOffset(player, slotsPerPage);
    }

    public void setPage(Player player, int page) {
        this.setOffset(player, page * slotsPerPage);
    }

    public void setOffset(Player player, int offset) {
        this.offset = offset;
        render(player, true);
    }

    public void addOffset(Player player, int offset) {
        this.setOffset(player, this.offset + offset);
    }

    public void removeOffset(Player player, int offset) {
        this.setOffset(player, this.offset - offset);
    }

    public void openSync(Player viewer) {
        openSync(viewer, ViewMode.MODIFY);
    }

    public void openSync(Player viewer, ViewMode viewMode) {
        if (viewer.isSleeping()) {
            viewer.wakeup(true);
        }

        render(viewer, true);
        openInventory(viewer, inventory);

        if (this.viewer.isEmpty()) {
            animations.forEach((integer, animations1) -> {
                int i = integer - offset;
                if (i >= 0 && i < slotsPerPage) {
                    animations1.forEach(Animation::play);
                }
            });
        }
        this.viewer.put(viewer.getUniqueId(), viewMode);

        if (this.viewer.size() == 1) {
            firstOpen();
        }
        if (this.openHandler != null) {
            try {
                openHandler.accept(new OpenContext(viewer, this));
            } catch (Exception e) {
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while calling OpenHandler.", e);
            }
        }
    }

    public void render(Player viewer, boolean clear) {

        int page = getCurrentPage();
        if (inventory == null) {
            inventory = createInventory(viewer, page);
        }

        if (clear) {
            clearContent();
        }

        refreshDynamicItemSuppliers();

        for (int slot : getSlots()) {
            try {
                ItemStack item = getItemStack(slot + offset);
                if (item == null) {
                    continue;
                }
                inventory.setItem(slot, item.clone());

            } catch (Throwable t) {
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Could not place menu item.", t);
            }
        }
    }

    public void close(Player viewer) {
        handleClose(viewer);
        viewer.closeInventory();
    }

    @Override
    public void closeKeepInventory(Player viewer) {
        handleClose(viewer);
    }

    public void handleClose(Player viewer) {

        if (this.viewer.remove(viewer.getUniqueId()) == null) {
            return;
        }
        if (this.viewer.size() == 0) {
            animations.forEach((integer, animations1) -> animations1.forEach(Animation::stop));
            lastClose();
        }
        if (closeHandler != null) {
            try {
                closeHandler.accept(new CloseContext(viewer, this, getCurrentPage()));
            } catch (Exception exc) {
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while calling CloseHandler", exc);
            }
        }
    }

    public void openPreviousMenu(Player viewer) {
        handleClose(viewer);

        Menu previous = this.previous.remove(viewer.getUniqueId());
        if (previous != null) {
            previous.open(viewer, ViewMode.MODIFY);
        }
    }

    public MenuPreset<? extends TargetContext<?>> addPreset(MenuPreset<? extends TargetContext<?>> menuPreset) {
        dynamicProcessors.add(menuPreset);
        return menuPreset;
    }

    public void removePreset(MenuPreset<? extends TargetContext<?>> menuPreset) {
        dynamicProcessors.remove(menuPreset);
    }

    public void removeAllPresets() {
        dynamicProcessors.clear();
    }

    public void clearContent() {
        if (inventory == null) {
            return;
        }
        for (int slot : getSlots()) {
            inventory.setItem(slot, null);
        }
    }

    public ItemStack getItemStack(int slot) {
        int staticSlot = slot - offset;
        ItemStack stack = dynamicItemStacksOnTop.get(staticSlot);
        if (stack != null) {
            return tagStack(stack);
        }
        stack = getStaticItemStack(slot);
        if (stack != null) {
            return tagStack(stack);
        }
        return tagStack(dynamicItemStacks.get(staticSlot));
    }

    protected ItemStack getStaticItemStack(int slot) {
        Supplier<ItemStack> supplier = itemStacks.get(slot);
        return supplier == null ? null : supplier.get();
    }

    private ItemStack tagStack(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(GUIHandler.getInstance().getPlugin(), "prevent_pickup"), PersistentDataType.SHORT, (short) 0);
        stack.setItemMeta(meta);
        return stack;
    }

    public void setItem(int slot, ItemStack item) {
        setItem(slot, () -> item);
    }

    public void setItem(int slot, Supplier<ItemStack> itemSupplier) {
        itemStacks.put(slot, itemSupplier);
    }

    public void removeItem(int... slots) {
        for (int slot : slots) {
            if (inventory != null) {
                inventory.setItem(slot, null);
            }
            itemStacks.remove(slot);
        }
    }

    public void refresh(int... slots) {
        if (inventory == null) {
            return;
        }
        int page = getCurrentPage();
        for (int slot : slots) {
            int realIndex = page * slotsPerPage + slot;
            inventory.setItem(slot, getItemStack(realIndex));
        }
    }

    public <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context) {

        Player player = context.getPlayer();
        int slot = context.getSlot();

        if (Arrays.stream(getSlots()).noneMatch(value -> value == slot)) {
            return false;
        }
        if (viewer.containsKey(player.getUniqueId()) && viewer.get(player.getUniqueId()).equals(ViewMode.VIEW)) {
            return true;
        }

        int actualSlot = slot + offset;
        if (soundPlayer.containsKey(actualSlot)) {
            soundPlayer.get(actualSlot).accept(context.getPlayer());
        }

        ContextConsumer<C> clickHandler = (ContextConsumer<C>) getClickHandler(slot, action);

        if (clickHandler != null) {
            //execute and catch exceptions so users can't dupe itemstacks.
            try {
                clickHandler.accept(context);
            } catch (Exception exc) {
                context.setCancelled(true);
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while handling GUI interaction of player " + player.getName(), exc);
            }
        }
        return context.isCancelled();
    }

    public ContextConsumer<? extends TargetContext<?>> getClickHandler(int slot, Action<?> action) {
        int fixedSlot = slot % slotsPerPage;
        fixedSlot = fixedSlot < 0 ? fixedSlot + slotsPerPage : fixedSlot;
        var result = dynamicClickHandlerOnTop.getOrDefault(fixedSlot, new HashMap<>()).get(action);
        if (result != null) {
            return result;
        }
        result = getStaticClickHandler(slot, action);
        if (result != null) {
            return result;
        }
        result = dynamicClickHandler.getOrDefault(fixedSlot, new HashMap<>()).get(action);
        if (result != null) {
            return result;
        }
        return defaultClickHandler.getOrDefault(action, fallbackDefaultClickHandler);
    }

    protected ContextConsumer<? extends TargetContext<?>> getStaticClickHandler(int slot, Action<?> action) {
        return clickHandler.getOrDefault(slot + offset, new HashMap<>()).get(action);
    }

    public void setButton(int slot, Button button) {
        if (button.getStackSupplier() != null) {
            setItem(slot, button.getStackSupplier());
        }
        if (button.getSound() != null) {
            soundPlayer.put(slot, player -> player.playSound(player.getLocation(), button.getSound(), button.getVolume(), button.getPitch()));
        }
        if (!button.getClickHandler().isEmpty()) {
            setClickHandler(slot, button.getClickHandler());
        }
    }

    public <C extends TargetContext<?>> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
        setClickHandler(slot, Map.of(action, clickHandler));
    }

    public void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
        map.putAll(clickHandler);
        this.clickHandler.put(slot, map);
    }

    public <C extends TargetContext<?>> void setItemAndClickHandler(int slot, ItemStack item, Action<C> action, ContextConsumer<C> clickHandler) {
        setItem(slot, item);
        setClickHandler(slot, action, clickHandler);
    }

    public <C extends TargetContext<?>> void setItemAndClickHandler(int slot, Supplier<ItemStack> item, Action<C> action, ContextConsumer<C> clickHandler) {
        setItem(slot, item);
        setClickHandler(slot, action, clickHandler);
    }

    public void setDefaultClickHandler(ContextConsumer<? extends TargetContext<?>> clickHandler) {
        fallbackDefaultClickHandler = clickHandler;
    }

    public <C extends TargetContext<?>> void setDefaultClickHandler(Action<C> action, ContextConsumer<C> clickHandler) {
        defaultClickHandler.put(action, clickHandler);
    }

    public void removeClickHandler(int... slots) {
        for (int slot : slots) {
            clickHandler.remove(slot);
        }
    }

    public void removeClickHandler(Action<?> action, int... slots) {
        for (int slot : slots) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public void removeItemAndClickHandler(int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            clickHandler.remove(slot);
        }
    }

    public void removeItemAndClickHandler(Action<?> action, int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public void removeDefaultClickHandler(Action<?> action) {
        defaultClickHandler.remove(action);
    }

    public boolean isThisInventory(Inventory inventory, Player player) {
        return this.inventory != null && this.inventory.equals(inventory);
    }

    public int getPageCount() {
        return getMaxPage() - getMinPage();
    }

    public void refreshDynamicItemSuppliers() {
        dynamicItemStacks.clear();
        dynamicClickHandler.clear();
        dynamicItemStacksOnTop.clear();
        dynamicClickHandlerOnTop.clear();

        for (MenuPreset<?> processor : dynamicProcessors) {
            try {
                processor.placeDynamicEntries(applier);
            } catch (Throwable t) {
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Could not place dynamic menu item.", t);
            }
        }
    }

    public int getCurrentPage() {

        return (int) Math.floor((double) offset / slotsPerPage);
    }

    public int getMinPage() {
        int minPage = 0;
        int smallestSlot = Integer.min(itemStacks.isEmpty() ? 0 : itemStacks.firstKey(), clickHandler.isEmpty() ? 0 : clickHandler.firstKey());
        boolean negative = smallestSlot < 0;
        while (negative && smallestSlot < -slotsPerPage || !negative && smallestSlot > slotsPerPage) {
            if (negative) {
                minPage--;
                smallestSlot += slotsPerPage;
            } else {
                minPage++;
                smallestSlot -= slotsPerPage;
            }
        }
        return Integer.min(negative ? --minPage : minPage, getCurrentPage());
    }

    public int getMaxPage() {
        int maxPage = 0;
        int highestSlot = Integer.max(itemStacks.isEmpty() ? 0 : itemStacks.lastKey(), clickHandler.isEmpty() ? 0 : clickHandler.lastKey());
        while (highestSlot > slotsPerPage) {
            maxPage++;
            highestSlot -= slotsPerPage;
        }
        return Integer.max(maxPage, getCurrentPage());
    }

    protected int applyOffset(int slot) {
        return slot + offset;
    }

    public Animation playEndlessAnimation(int ticks, int... slots) {
        return playAnimation(-1, ticks, slots);
    }

    public Animation playAnimation(int intervals, int ticks, int... slots) {
        Animation animation = new Animation(slots, intervals, ticks, this::refresh);

        Arrays.stream(slots).forEach(value -> {
            Collection<Animation> animations = this.animations.getOrDefault(value, new HashSet<>());
            animations.add(animation);
            this.animations.put(value, animations);
        });
        if (inventory != null && viewer.size() > 0) {
            animation.play();
        }
        return animation;
    }

    public Collection<Animation> getAnimations(int... slots) {
        HashSet<Animation> anims = new HashSet<>();
        for (int slot : slots) {
            Collection<Animation> animations = this.animations.get(slot);
            if (animations != null) {
                anims.addAll(animations);
            }
        }
        return anims;
    }

    public void stopAnimation(Animation animation) {
        animation.stop();
        this.animations.values().forEach(a -> a.remove(animation));
    }
}
