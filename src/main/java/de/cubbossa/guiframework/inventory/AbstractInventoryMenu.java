package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.AnimationContext;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.CloseContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractInventoryMenu<T, C extends ClickContext> {

    public enum ViewMode {
        MODIFY,
        VIEW
    }

    protected final SortedMap<Integer, ItemStack> itemStacks;
    protected final SortedMap<Integer, Map<T, ContextConsumer<C>>> clickHandler;
    protected final SortedMap<Integer, Consumer<Player>> soundPlayer;

    protected final List<DynamicMenuProcessor<T, C>> dynamicProcessors;
    protected final SortedMap<Integer, ItemStack> dynamicItemStacks;
    protected final SortedMap<Integer, Map<T, ContextConsumer<C>>> dynamicClickHandler;

    protected final Map<T, ContextConsumer<C>> defaultClickHandler;
    protected final Map<T, Boolean> defaultCancelled;
    @Setter
    protected ContextConsumer<CloseContext> closeHandler;

    protected final Map<Integer, Collection<Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;

    protected final int slotsPerPage;
    protected int currentPage = 0;

    protected Inventory inventory;

    public AbstractInventoryMenu(int slotsPerPage) {

        this.itemStacks = new TreeMap<>();
        this.clickHandler = new TreeMap<>();
        this.soundPlayer = new TreeMap<>();

        this.dynamicProcessors = new ArrayList<>();
        this.dynamicItemStacks = new TreeMap<>();
        this.dynamicClickHandler = new TreeMap<>();

        this.defaultClickHandler = new HashMap<>();
        this.animations = new TreeMap<>();
        this.viewer = new HashMap<>();
        this.defaultCancelled = new HashMap<>();

        this.slotsPerPage = slotsPerPage;
    }

    protected abstract Inventory createInventory(Player player, int page);

    protected abstract void openInventory(Player player, Inventory inventory);

    public void open(Player viewer) {
        open(viewer, ViewMode.MODIFY);
    }

    public void open(Player viewer, ViewMode viewMode) {
        GUIHandler.getInstance().callSynchronized(() -> openInventorySynchronized(viewer, viewMode, null));
    }

    public void open(Collection<Player> viewers, ViewMode viewMode) {
        viewers.forEach(player -> open(player, viewMode));
    }

    public void open(Player viewer, AbstractInventoryMenu<T, C> previous) {
        GUIHandler.getInstance().callSynchronized(() -> openInventorySynchronized(viewer, previous));
    }

    public void open(Collection<Player> viewers, AbstractInventoryMenu<T, C> previous) {
        viewers.forEach(player -> open(player, previous));
    }

    public AbstractInventoryMenu<T, C> openSubMenu(Player player, Supplier<AbstractInventoryMenu<T, C>> menuSupplier) {
        AbstractInventoryMenu<T, C> menu = menuSupplier.get();
        menu.open(player, this);
        return menu;
    }

    public void openNextPage(Player player) {
        openPage(player, currentPage + 1);
    }

    public void openPreviousPage(Player player) {
        openPage(player, currentPage - 1);
    }

    public void openPage(Player player, int page) {
        currentPage = page;
        open(player);
    }

    protected void openInventorySynchronized(Player viewer, @Nullable AbstractInventoryMenu<?, ?> previous) {
        openInventorySynchronized(viewer, ViewMode.MODIFY, previous);
    }

    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable AbstractInventoryMenu<?, ?> previous) {

        if (inventory == null) {
            inventory = createInventory(viewer, currentPage);
        }
        clearContent();

        if (viewer.isSleeping()) {
            viewer.wakeup(true);
        }
        int minPage = getMinPage();
        int maxPage = getMaxPage();
        if (currentPage < minPage) {
            currentPage = minPage;
        } else if (currentPage > maxPage) {
            currentPage = maxPage;
        }

        dynamicItemStacks.clear();
        dynamicClickHandler.clear();
        for (DynamicMenuProcessor<T, C> processor : dynamicProcessors) {
            processor.placeDynamicEntries(this, dynamicItemStacks::put, dynamicClickHandler::put);
        }

        for (int slot : getSlots()) {
            ItemStack item = itemStacks.getOrDefault(currentPage * slotsPerPage + slot, dynamicItemStacks.get(slot));
            if (item == null) {
                continue;
            }
            inventory.setItem(slot, item.clone());
        }

        openInventory(viewer, inventory);
        this.viewer.put(viewer.getUniqueId(), viewMode);
        InventoryHandler.getInstance().registerInventory(viewer, this, previous);
    }

    public boolean handleInteract(Player player, int clickedSlot, T action, C context) {

        if (Arrays.stream(getSlots()).noneMatch(value -> value == clickedSlot)) {
            return false;
        }
        if (viewer.containsKey(player.getUniqueId()) && viewer.get(player.getUniqueId()).equals(ViewMode.VIEW)) {
            return true;
        }

        int actualSlot = currentPage * slotsPerPage + clickedSlot;
        if (soundPlayer.containsKey(actualSlot)) {
            soundPlayer.get(actualSlot).accept(context.getPlayer());
        }

        ContextConsumer<C> clickHandler = getClickHandlerOrFallback(clickedSlot, action);
        if (clickHandler == null) {
            clickHandler = dynamicClickHandler.getOrDefault(clickedSlot, new HashMap<>()).get(action);
        }
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

    /**
     * Close this menu for a player.
     *
     * @param viewer the player to close this menu for
     */
    public void close(Player viewer) {

        if (this.viewer.remove(viewer.getUniqueId()) == null) {
            return;
        }
        if (closeHandler != null) {
            try {
                closeHandler.accept(new CloseContext(viewer, currentPage));
            } catch (Exception exc) {
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while closing gui for " + viewer.getName(), exc);
            }
        }
        if (this instanceof TopInventoryMenu topMenu && this.viewer.size() == 0) {
            InventoryHandler.getInstance().unregisterTopMenuListener(topMenu);
        }
    }

    /**
     * Close this menu for a group of players.
     *
     * @param viewers the group of players to close this menu for.
     */
    public void closeAll(Collection<Player> viewers) {
        viewers.forEach(this::close);
    }

    /**
     * Close all player views of this menu.
     */
    public void closeAll() {
        closeAll(viewer.keySet().stream().map(Bukkit::getPlayer).collect(Collectors.toSet()));
    }

    /**
     * loads a dynamic preset that only exists as long as the current page is opened. This might be useful to
     * implement pagination, as pagination may need to extend dynamically based on the page count.
     *
     * @param menuProcessor the instance of the processor. Use the BiConsumer parameters to add items and clickhandler
     *                      to a specific slot.
     */
    public DynamicMenuProcessor<T, C> loadPreset(DynamicMenuProcessor<T, C> menuProcessor) {
        dynamicProcessors.add(menuProcessor);
        return menuProcessor;
    }

    /**
     * Unloads a certain menu processor / preset. The preset items will stay until their slot is updated.
     *
     * @param menuProcessor the preset to remove
     */
    public void unloadPreset(DynamicMenuProcessor<T, C> menuProcessor) {
        dynamicProcessors.remove(menuProcessor);
    }

    /**
     * Removes all presets. The preset icons will stay in all open menus of this instance until the menu gets refreshed.
     * Reopen them or call {@link #refresh(int...)} on the according or just all slots with {@link #getSlots()}
     */
    public void unloadAllPresets() {
        dynamicProcessors.clear();
    }

    /**
     * @return all slots that are part of this inventory.
     */
    public abstract int[] getSlots();

    /**
     * @param slot the slot to get the itemstack from
     * @return the itemstack of the menu at the given slot. This does not return the actual item in the inventory but the stored item instance.
     */
    public ItemStack getItemStack(int slot) {
        return itemStacks.get(slot);
    }

    protected ContextConsumer<C> getClickHandlerOrFallback(int slot, T action) {
        return clickHandler.getOrDefault(currentPage * slotsPerPage + slot, new HashMap<>()).getOrDefault(action, defaultClickHandler.get(action));
    }

    /**
     * Clears all minecraft inventory slots. It does not clear the menu item map or any click handlers.
     * After reopening or refreshing the menu, all items will be back.
     */
    public void clearContent() {
        for (int slot : getSlots()) {
            inventory.setItem(slot, null);
        }
    }

    /**
     * Refreshes the itemstack at certain slots of this menu.
     * This method needs to be called after all methods that insert items. {@link #setItem(ItemStack, int...)}
     *
     * @param slots the slots to refresh
     */
    public void refresh(int... slots) {
        for (int slot : slots) {
            int realIndex = currentPage * slotsPerPage + slot;
            inventory.setItem(slot, itemStacks.getOrDefault(realIndex, dynamicItemStacks.get(slot)));
        }
    }

    /**
     * Sets an inventory icon, sound and click handler from a button builder
     *
     * @param button the button builder. Use {@link #buttonBuilder()} to get a new button builder instance
     * @param slots  the absolute slots to apply the button builder on. {@code ((current_page * slots_per_page) + page_slot)}
     */
    public void setButton(ButtonBuilder<T, C> button, int... slots) {
        if (button.stack != null) {
            setItem(button.stack, slots);
        }
        for (int slot : slots) {
            soundPlayer.put(slot, player -> player.playSound(player.getLocation(), button.sound, button.volume, button.pitch));
        }
        if (!button.clickHandler.isEmpty()) {
            setClickHandler(button.clickHandler, slots);
        }
    }

    /**
     * Sets an inventory icon
     *
     * @param item  the item instance to insert into the inventory
     * @param slots the slots to add the item at. Use slots larger than the slots on one page to place them on a different page.
     *              {@code slot = (current_page * slots_per_page) + inventory_slot}
     */
    public void setItem(ItemStack item, int... slots) {
        for (int slot : slots) {
            itemStacks.put(slot, item);
        }
    }

    /**
     * Populates the inventory with itemstacks that are rendered on each page, if no real item was found.
     *
     * @param item  the item to render
     * @param slots the slots to render the item at (not paginated, only 0 to rows*cols)
     */
    public void setDynamicItem(ItemStack item, int... slots) {
        for (int slot : slots) {
            dynamicItemStacks.put(slot, item);
        }
    }

    public void setClickHandler(T action, ContextConsumer<C> clickHandler, int... slots) {
        for (int slot : slots) {
            Map<T, ContextConsumer<C>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            this.clickHandler.put(slot, map);
        }
    }

    public void setClickHandler(Map<T, ContextConsumer<C>> clickHandler, int... slots) {
        for (int slot : slots) {
            Map<T, ContextConsumer<C>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
            map.putAll(clickHandler);
            this.clickHandler.put(slot, map);
        }
    }

    public void setItemAndClickHandler(ItemStack item, T action, ContextConsumer<C> clickHandler, int... slots) {
        setItem(item, slots);
        setClickHandler(action, clickHandler, slots);
    }

    public void setDefaultClickHandler(T action, ContextConsumer<C> clickHandler) {
        defaultClickHandler.put(action, clickHandler);
    }

    public void removeItem(int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
        }
    }

    public void removeClickHandler(int... slots) {
        for (int slot : slots) {
            clickHandler.remove(slot);
        }
    }

    public void removeClickHandler(T action, int... slots) {
        for (int slot : slots) {
            Map<T, ContextConsumer<C>> map = clickHandler.get(slot);
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

    public void removeItemAndClickHandler(T action, int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            Map<T, ContextConsumer<C>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public void removeDefaultClickHandler(T action) {
        defaultClickHandler.remove(action);
    }

    public boolean isThisInventory(Inventory inventory, Player player) {
        return this.inventory != null && this.inventory.equals(inventory);
    }

    public int getPageCount() {
        return getMaxPage() - getMinPage();
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

    public Collection<Animation> playAnimation(int ticks, Function<AnimationContext, ItemStack> itemUpdater, int... slots) {
        return playAnimation(-1, ticks, itemUpdater, slots);
    }

    public Collection<Animation> playAnimation(int intervals, int ticks, Function<AnimationContext, ItemStack> itemUpdater, int... slots) {
        Collection<Animation> newAnimations = new HashSet<>();
        for (int slot : slots) {
            Animation animation = new Animation(slot, intervals, ticks, itemUpdater);

            Collection<Animation> animations = this.animations.get(slot);
            if (animations == null) {
                animations = new HashSet<>();
            }
            animations.add(animation);
            newAnimations.add(animation);
            if (inventory != null && viewer.size() > 0) {
                animation.play();
            }
        }
        return newAnimations;
    }

    public void stopAnimation(int... slots) {
        for (int slot : slots) {
            Collection<Animation> animations = this.animations.get(slot);
            if (animations != null) {
                animations.forEach(Animation::stop);
            }
        }
    }

    public ButtonBuilder<T, C> buttonBuilder() {
        return new ButtonBuilder<>();
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
                            setItem(itemUpdater.apply(new AnimationContext(slot, intervals, item, Bukkit.getCurrentTick(), Bukkit.getCurrentTick() % 20)), slot);
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

    @Getter
    public static class ButtonBuilder<T, C extends ClickContext> {

        private ItemStack stack;
        private Sound sound;
        private float pitch = 1f;
        private float volume = .8f;
        private final Map<T, ContextConsumer<C>> clickHandler = new HashMap<>();

        /**
         * @param stack the icon itemstack
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withItemStack(ItemStack stack) {
            this.stack = stack;
            return this;
        }

        /**
         * @param material the material of the icon
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withItemStack(Material material) {
            this.stack = new ItemStack(material);
            return this;
        }

        /**
         * @param material the material of the icon
         * @param name     the name component of the icon
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withItemStack(Material material, Component name) {
            stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(name);
            stack.setItemMeta(meta);
            return this;
        }

        /**
         * @param material the material of the icon
         * @param name     the name component of the icon
         * @param lore     the lore of the icon
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withItemStack(Material material, Component name, List<Component> lore) {
            stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();
            meta.displayName(name);
            meta.lore(lore);
            stack.setItemMeta(meta);
            return this;
        }

        /**
         * @param sound a {@link Sound} to play when clicked
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withSound(Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Keep in mind that pitch only ranges from 0.5f to 2f.
         * Volume has its maximum at 1, from then on it only increases in range (falloff distance)
         *
         * @param sound  a {@link Sound} to play when clicked
         * @param volume the volume to play the sound with
         * @param pitch  the pitch to play the sound with
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withSound(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            return this;
        }

        /**
         * Keep in mind that pitch only ranges from 0.5f to 2f.
         * Volume has its maximum at 1, from then on it only increases in range (falloff distance)
         *
         * @param sound      a {@link Sound} to play when clicked
         * @param volumeFrom the lower limit for the random volume
         * @param volumeTo   the upper limit for the random volume
         * @param pitchFrom  the lower limit for the random pitch
         * @param pitchTo    the upper limit for the random pitch
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withSound(Sound sound, float volumeFrom, float volumeTo, float pitchFrom, float pitchTo) {
            this.sound = sound;
            this.volume = (float) (volumeFrom + Math.random() * (volumeTo - volumeFrom));
            this.pitch = (float) (pitchFrom + Math.random() * (pitchTo - pitchFrom));
            return this;
        }

        /**
         * @param clickHandler a click handler to run
         * @param actions      all actions to run the click handler for
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withClickHandler(ContextConsumer<C> clickHandler, T... actions) {
            for (T action : actions) {
                this.clickHandler.put(action, clickHandler);
            }
            return this;
        }

        /**
         * @param clickHandler a map of click handlers for each action
         * @return the builder instance
         */
        public ButtonBuilder<T, C> withClickHandler(Map<T, ContextConsumer<C>> clickHandler) {
            this.clickHandler.putAll(clickHandler);
            return this;
        }
    }
}
