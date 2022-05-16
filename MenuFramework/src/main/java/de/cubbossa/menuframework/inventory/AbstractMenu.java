package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.inventory.context.CloseContext;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.OpenContext;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.CloseMenuException;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import de.cubbossa.menuframework.inventory.exception.OpenMenuException;
import de.cubbossa.menuframework.util.Animation;
import lombok.Getter;
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

@Getter
public abstract class AbstractMenu implements Menu {

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

        @Override
        public void addItem(int slot, Supplier<ItemStack> itemStack) {
            dynamicItemStacks.put(slot, itemStack.get());
        }

        public void addItemOnTop(int slot, ItemStack itemStack) {
            dynamicItemStacksOnTop.put(slot, itemStack);
        }

        @Override
        public void addItemOnTop(int slot, Supplier<ItemStack> itemStack) {
            dynamicItemStacksOnTop.put(slot, itemStack.get());
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

    protected List<ContextConsumer<OpenContext>> openHandlers;
    protected List<ContextConsumer<CloseContext>> closeHandlers;

    protected final Map<Integer, Collection<Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;

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
        this.slotsPerPage = slotsPerPage;
        this.clickHandler = new TreeMap<>();
        this.defaultClickHandler = new HashMap<>();
        this.openHandlers = new ArrayList<>();
        this.closeHandlers = new ArrayList<>();
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
        try {
            render(player, true);
        } catch (ItemPlaceException e) {
            GUIHandler.getInstance().getExceptionHandler().accept(e);
        }
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

        try {
            render(viewer, true);
        } catch (ItemPlaceException e) {
            GUIHandler.getInstance().getExceptionHandler().accept(e);
        }
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

        OpenContext openContext = new OpenContext(viewer, this);
        for (ContextConsumer<OpenContext> c : openHandlers) {
            try {
                c.accept(openContext);
            } catch (Exception e) {
                GUIHandler.getInstance().getExceptionHandler().accept(new OpenMenuException(openContext, e));
            }
        }
    }

    public void render(Player viewer, boolean clear) throws ItemPlaceException {

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
                throw new ItemPlaceException(this, viewer, slot, t);
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
        CloseContext closeContext = new CloseContext(viewer, this, getCurrentPage());
        for (ContextConsumer<CloseContext> c : closeHandlers) {
            try {
                c.accept(closeContext);
            } catch (Exception exc) {
                GUIHandler.getInstance().getExceptionHandler().accept(new CloseMenuException(closeContext, exc));
            }
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

        ContextConsumer<C> clickHandler = (ContextConsumer<C>) getClickHandler(actualSlot, action);

        if (clickHandler != null) {
            //execute and catch exceptions so users can't dupe itemstacks.
            try {
                clickHandler.accept(context);
            } catch (Exception exc) {
                GUIHandler.getInstance().getExceptionHandler().accept(new MenuHandlerException(context, exc));
            }
        }
        return context.isCancelled();
    }

    public ContextConsumer<? extends TargetContext<?>> getClickHandler(int slot, Action<?> action) {
        int fixedSlot = slot % slotsPerPage;
        fixedSlot = fixedSlot < 0 ? fixedSlot + slotsPerPage : fixedSlot;
        ContextConsumer<? extends TargetContext<?>> result = dynamicClickHandlerOnTop.getOrDefault(fixedSlot, new HashMap<>()).get(action);
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
        return clickHandler.getOrDefault(slot, new HashMap<>()).get(action);
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
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = new HashMap<>();
        map.put(action, clickHandler);
        setClickHandler(slot, map);
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

    public void setOpenHandler(ContextConsumer<OpenContext> openHandler) {
        openHandlers.add(openHandler);
    }

    public void removeOpenHandler(ContextConsumer<OpenContext> openHandler) {
        openHandlers.remove(openHandler);
    }

    public void clearOpenHandlers() {
        openHandlers.clear();
    }

    public void setCloseHandler(ContextConsumer<CloseContext> closeHandler) {
        closeHandlers.add(closeHandler);
    }

    @Override
    public void removeCloseHandler(ContextConsumer<CloseContext> closeHandler) {
        closeHandlers.remove(closeHandler);
    }

    @Override
    public void clearCloseHandlers() {
        closeHandlers.clear();
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

    public void refreshDynamicItemSuppliers() throws ItemPlaceException {
        dynamicItemStacks.clear();
        dynamicClickHandler.clear();
        dynamicItemStacksOnTop.clear();
        dynamicClickHandlerOnTop.clear();

        for (MenuPreset<?> processor : dynamicProcessors) {
            try {
                processor.placeDynamicEntries(applier);
            } catch (Throwable t) {
                throw new ItemPlaceException(this, t);
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
