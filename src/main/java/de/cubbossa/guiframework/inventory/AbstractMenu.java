package de.cubbossa.guiframework.inventory;

import com.google.common.base.Preconditions;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractMenu implements Menu {

    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> clickHandler;
    protected final SortedMap<Integer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>>> dynamicClickHandler;
    protected final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> defaultClickHandler;
    protected final Map<Action<?>, Boolean> defaultCancelled;

    protected final SortedMap<Integer, ItemStack> itemStacks;
    protected final SortedMap<Integer, Consumer<Player>> soundPlayer;

    protected final List<MenuPreset<? extends TargetContext<?>>> dynamicProcessors;
    protected final SortedMap<Integer, ItemStack> dynamicItemStacks;

    @Setter
    protected ContextConsumer<OpenContext> openHandler;
    @Setter
    protected ContextConsumer<CloseContext> closeHandler;

    protected final Map<Integer, Collection<Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;
    protected final Map<UUID, Menu> previous;

    protected final int slotsPerPage;
    protected int currentPage = 0;

    protected Inventory inventory;

    public AbstractMenu(int slotsPerPage) {

        this.itemStacks = new TreeMap<>();
        this.soundPlayer = new TreeMap<>();
        this.dynamicProcessors = new ArrayList<>();
        this.dynamicItemStacks = new TreeMap<>();
        this.animations = new TreeMap<>();
        this.viewer = new HashMap<>();
        this.previous = new HashMap<>();
        this.slotsPerPage = slotsPerPage;
        this.clickHandler = new TreeMap<>();
        this.dynamicClickHandler = new TreeMap<>();
        this.defaultClickHandler = new HashMap<>();
        this.defaultCancelled = new HashMap<>();
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

    public Menu openSubMenu(Player player, Menu menu) {
        menu.setPrevious(player, this);
        menu.open(player);
        return menu;
    }

    public Menu openSubMenu(Player player, Supplier<Menu> menuSupplier) {
        return openSubMenu(player, menuSupplier.get());
    }

    public Menu openSubMenu(Player player, Menu menu, MenuPreset<?> backPreset) {
        return openSubMenu(player, menu, ViewMode.MODIFY, backPreset);
    }

    public Menu openSubMenu(Player player, Supplier<Menu> menuSupplier, MenuPreset<?> backPreset) {
        return openSubMenu(player, menuSupplier.get(), ViewMode.MODIFY, backPreset);
    }

    public Menu openSubMenu(Player player, Menu menu, ViewMode viewMode, MenuPreset<?> backPreset) {
        menu.setPrevious(player, this);
        menu.addPreset(backPreset);
        menu.open(player);
        return menu;
    }

    public Menu openSubMenu(Player player, Supplier<Menu> menuSupplier, ViewMode viewMode, MenuPreset<?> backPreset) {
        return openSubMenu(player, menuSupplier.get(), viewMode, backPreset);
    }

    @Override
    public void setPrevious(Player player, Menu previous) {
        this.previous.put(player.getUniqueId(), previous);
    }

    public void openNextPage(Player player) {
        openPage(player, currentPage + 1);
    }

    public void openPreviousPage(Player player) {
        openPage(player, currentPage - 1);
    }

    public void openPage(Player player, int page) {
        currentPage = page;
        render(player, true);
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
                int i = integer - currentPage * slotsPerPage;
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

        if (inventory == null) {
            inventory = createInventory(viewer, currentPage);
        }

        if (clear) {
            clearContent();
        }

        int minPage = getMinPage();
        int maxPage = getMaxPage();
        if (currentPage < minPage) {
            currentPage = minPage;
        } else if (currentPage > maxPage) {
            currentPage = maxPage;
        }

        refreshDynamicItemSuppliers();

        for (int slot : getSlots()) {
            ItemStack item = getItemStack(currentPage * slotsPerPage + slot);
            if (item == null) {
                continue;
            }
            //TODO apply nbt tag to prevent from stacking
            inventory.setItem(slot, item.clone());
        }
    }

    public void close(Player viewer) {
        GUIHandler.getInstance().callSynchronized(() -> {

            Menu previous = this.previous.remove(viewer.getUniqueId());
            if (this.viewer.remove(viewer.getUniqueId()) == null) {
                return;
            }
            if (this.viewer.size() == 0) {
                animations.forEach((integer, animations1) -> animations1.forEach(Animation::stop));
                lastClose();
            }
            if (closeHandler != null) {
                try {
                    closeHandler.accept(new CloseContext(viewer, currentPage));
                } catch (Exception exc) {
                    GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while calling CloseHandler", exc);
                }
            }
            if (previous != null) {
                previous.openSync(viewer, ViewMode.MODIFY);
            }
        });
    }

    public void closeAll(Collection<Player> viewers) {
        viewers.forEach(this::close);
    }

    public void closeAll() {
        closeAll(viewer.keySet().stream().map(Bukkit::getPlayer).collect(Collectors.toSet()));
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
        for (int slot : getSlots()) {
            inventory.setItem(slot, null);
        }
    }

    public ItemStack getItemStack(int slot) {
        ItemStack stack = itemStacks.get(slot);
        if (stack != null) {
            return stack;
        }
        int dynSlot = slot % slotsPerPage;
        return dynamicItemStacks.get(dynSlot < 0 ? dynSlot + slotsPerPage : dynSlot);
    }

    public void setItem(int slot, ItemStack item) {
        itemStacks.put(slot, item);
    }

    public void removeItem(int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
        }
    }

    public void setDynamicItem(int slot, ItemStack item) {
        Preconditions.checkArgument(slotsPerPage <= slot ||slot < 0, "Slot must be on first page.");
        dynamicItemStacks.put(slot, item);
    }

    public void refresh(int... slots) {
        for (int slot : slots) {
            int realIndex = currentPage * slotsPerPage + slot;
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

        int actualSlot = currentPage * slotsPerPage + slot;
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
        int i = slot % slotsPerPage;
        var result = dynamicClickHandler.getOrDefault(i < 0 ? i + slotsPerPage : i, new HashMap<>()).get(action);
        if (result != null) {
            return result;
        }
        return clickHandler.getOrDefault(currentPage * slotsPerPage + slot, new HashMap<>()).get(action);
    }

    public void setButton(int slot, ButtonBuilder button) {
        if (button.getStack() != null) {
            setItem(slot, button.getStack());
        }
        if (button.getSound() != null) {
            soundPlayer.put(slot, player -> player.playSound(player.getLocation(), button.getSound(), button.getVolume(), button.getPitch()));
        }
        if (!button.getClickHandler().isEmpty()) {
            setClickHandler(slot, button.getClickHandler());
        }
    }

    public <C extends TargetContext<?>> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
        map.put(action, clickHandler);
        this.clickHandler.put(slot, map);
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

        MenuPreset.PresetApplier applier = new MenuPreset.PresetApplier(this) {
            @Override
            public void addItem(int slot, ItemStack itemStack) {
                dynamicItemStacks.put(slot, itemStack);
            }

            @Override
            public <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
                Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = dynamicClickHandler.getOrDefault(slot, new HashMap<>());
                map.put(action, clickHandler);
                dynamicClickHandler.put(slot, map);
            }
        };

        for (MenuPreset<?> processor : dynamicProcessors) {
            processor.placeDynamicEntries(applier);
        }
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
        return Integer.min(negative ? --minPage : minPage, currentPage);
    }

    public int getMaxPage() {
        int maxPage = 0;
        int highestSlot = Integer.max(itemStacks.isEmpty() ? 0 : itemStacks.lastKey(), clickHandler.isEmpty() ? 0 : clickHandler.lastKey());
        while (highestSlot > slotsPerPage) {
            maxPage++;
            highestSlot -= slotsPerPage;
        }
        return Integer.max(maxPage, currentPage);
    }

    public Animation playAnimation(int slot, int ticks, Function<AnimationContext, ItemStack> itemUpdater) {
        return playAnimation(slot, -1, ticks, itemUpdater);
    }

    public Animation playAnimation(int slot, int intervals, int ticks, Function<AnimationContext, ItemStack> itemUpdater) {
        Animation animation = new Animation(slot, intervals, ticks, itemUpdater);

        Collection<Animation> animations = this.animations.getOrDefault(slot, new HashSet<>());
        animations.add(animation);
        this.animations.put(slot, animations);
        if (inventory != null && viewer.size() > 0) {
            animation.play();
        }
        return animation;
    }

    public void stopAnimation(int... slots) {
        for (int slot : slots) {
            Collection<Animation> animations = this.animations.get(slot);
            if (animations != null) {
                animations.forEach(AbstractMenu.Animation::stop);
            }
        }
    }

    public class Animation {

        private final int slot;
        private int intervals = -1;
        private final int ticks;
        private final Function<AnimationContext, ItemStack> itemUpdater;

        private BukkitTask task;

        public Animation(int slot, int ticks, Function<AnimationContext, ItemStack> itemUpdater) {
            this.slot = slot;
            this.ticks = ticks;
            this.itemUpdater = itemUpdater;
        }

        public Animation(int slot, int intervals, int ticks, Function<AnimationContext, ItemStack> itemUpdater) {
            this.slot = slot;
            this.intervals = intervals;
            this.ticks = ticks;
            this.itemUpdater = itemUpdater;
        }

        public void play() {
            final ItemStack item = itemStacks.getOrDefault(slot, new ItemStack(Material.AIR));
            AtomicInteger interval = new AtomicInteger(0);
            task = Bukkit.getScheduler().runTaskTimer(GUIHandler.getInstance().getPlugin(), () -> {
                if (intervals == -1 || interval.get() < intervals) {
                    if (item != null) {
                        try {
                            setItem(slot, itemUpdater.apply(new AnimationContext(slot, intervals, item)));
                            refresh(slot);
                        } catch (Throwable t) {
                            GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while playing animation in inventory menu", t);
                        }
                        interval.addAndGet(1);
                    }
                } else {
                    stop();
                }
            }, 0, ticks);
        }

        public void stop() {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        public boolean isRunning() {
            return !task.isCancelled();
        }
    }
}
