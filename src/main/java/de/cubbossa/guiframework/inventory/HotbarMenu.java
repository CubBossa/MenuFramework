package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.AnimationContext;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class HotbarMenu extends ItemStackMenu {

    protected final SortedMap<Integer, Map<HotbarAction<?>, ContextConsumer<?>>> clickHandler;
    protected final List<DynamicMenuProcessor<HotbarAction<?>, ?>> dynamicProcessors;
    protected final SortedMap<Integer, Map<HotbarAction<?>, ContextConsumer<?>>> dynamicClickHandler;
    protected final Map<HotbarAction<?>, ContextConsumer<?>> defaultClickHandler;
    protected final Map<HotbarAction<?>, Boolean> defaultCancelled;

    private final Player player;

    public HotbarMenu(Player player) {
        super(9);

        this.clickHandler = new TreeMap<>();
        this.dynamicProcessors = new ArrayList<>();
        this.dynamicClickHandler = new TreeMap<>();
        this.defaultClickHandler = new HashMap<>();
        this.defaultCancelled = new HashMap<>();

        this.player = player;
    }

    @Override
    public int[] getSlots() {
        return IntStream.range(0, 9).toArray();
    }

    @Override
    protected Inventory createInventory(Player player, int page) {
        return player.getInventory();
    }

    public void setDefaultCancelled(HotbarAction<?> action, boolean cancelled) {
        this.defaultCancelled.put(action, cancelled);
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {

    }

    protected void openInventorySynchronized(Player viewer, ViewMode viewMode, @Nullable ItemStackMenu previous) {

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
        for (DynamicMenuProcessor processor : dynamicProcessors) {
            processor.placeDynamicEntries(this, (integer, itemStack) -> dynamicItemStacks.put((Integer) integer, (ItemStack) itemStack),
                    (key, value) -> dynamicClickHandler.put((Integer) key, (Map<HotbarAction<?>, ContextConsumer<?>>) value));
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
        HotbarMenuHandler.getInstance().registerInventory(viewer, this, (HotbarMenu) previous);
    }

    public <C extends ClickContext> boolean handleInteract(HotbarAction<C> action, C context) {

        if (Arrays.stream(getSlots()).noneMatch(value -> value == context.getSlot())) {
            return false;
        }
        if (viewer.containsKey(player.getUniqueId()) && viewer.get(player.getUniqueId()).equals(ViewMode.VIEW)) {
            return true;
        }

        int actualSlot = currentPage * slotsPerPage + context.getSlot();
        if (soundPlayer.containsKey(actualSlot)) {
            soundPlayer.get(actualSlot).accept(context.getPlayer());
        }

        ContextConsumer<C> clickHandler = getClickHandlerOrFallback(context.getSlot(), action);
        if (clickHandler == null) {
            clickHandler = (ContextConsumer<C>) dynamicClickHandler.getOrDefault(context.getSlot(), new HashMap<>()).get(action);
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
     * loads a dynamic preset that only exists as long as the current page is opened. This might be useful to
     * implement pagination, as pagination may need to extend dynamically based on the page count.
     *
     * @param menuProcessor the instance of the processor. Use the BiConsumer parameters to add items and clickhandler
     *                      to a specific slot.
     */
    public <C extends ClickContext> DynamicMenuProcessor<HotbarAction<C>, C> loadPreset(DynamicMenuProcessor<HotbarAction<C>, C> menuProcessor) {
        DynamicMenuProcessor d = menuProcessor;
        dynamicProcessors.add(d);
        return menuProcessor;
    }

    /**
     * Unloads a certain menu processor / preset. The preset items will stay until their slot is updated.
     *
     * @param menuProcessor the preset to remove
     */
    public <C extends ClickContext> void unloadPreset(DynamicMenuProcessor<HotbarAction<C>, C> menuProcessor) {
        dynamicProcessors.remove(menuProcessor);
    }

    /**
     * Removes all presets. The preset icons will stay in all open menus of this instance until the menu gets refreshed.
     * Reopen them or call {@link #refresh(int...)} on the according or just all slots with {@link #getSlots()}
     */
    public void unloadAllPresets() {
        dynamicProcessors.clear();
    }

    protected <C extends ClickContext> ContextConsumer<C> getClickHandlerOrFallback(int slot, HotbarAction<C> action) {
        return (ContextConsumer<C>) clickHandler.getOrDefault(currentPage * slotsPerPage + slot, new HashMap<>()).getOrDefault(action, defaultClickHandler.get(action));
    }

    /**
     * Sets an inventory icon, sound and click handler from a button builder
     *
     * @param button the button builder. Use {@link #buttonBuilder()} to get a new button builder instance
     * @param slots  the absolute slots to apply the button builder on. {@code ((current_page * slots_per_page) + page_slot)}
     */
    public <C extends ClickContext> void setButton(ButtonBuilder<HotbarAction<C>, C> button, int... slots) {
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

    public <C extends ClickContext> void setClickHandler(HotbarAction<C> action, ContextConsumer<C> clickHandler, int... slots) {
        for (int slot : slots) {
            Map<HotbarAction<?>, ContextConsumer<?>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            this.clickHandler.put(slot, map);
        }
    }

    public <C extends ClickContext> void setClickHandler(Map<HotbarAction<C>, ContextConsumer<C>> clickHandler, int... slots) {
        for (int slot : slots) {
            Map<HotbarAction<?>, ContextConsumer<?>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
            map.putAll(clickHandler);
            this.clickHandler.put(slot, map);
        }
    }

    public <C extends ClickContext> void setItemAndClickHandler(ItemStack item, HotbarAction<C> action, ContextConsumer<C> clickHandler, int... slots) {
        setItem(item, slots);
        setClickHandler(action, clickHandler, slots);
    }

    public <C extends ClickContext> void setDefaultClickHandler(HotbarAction<C> action, ContextConsumer<C> clickHandler) {
        defaultClickHandler.put(action, clickHandler);
    }

    public void removeClickHandler(int... slots) {
        for (int slot : slots) {
            clickHandler.remove(slot);
        }
    }

    public <C extends ClickContext> void removeClickHandler(HotbarAction<C> action, int... slots) {
        for (int slot : slots) {
            Map<HotbarAction<?>, ContextConsumer<?>> map = clickHandler.get(slot);
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

    public <C extends ClickContext> void removeItemAndClickHandler(HotbarAction<C> action, int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
            itemStacks.remove(slot);
            Map<HotbarAction<?>, ContextConsumer<?>> map = clickHandler.get(slot);
            if (map != null) {
                map.remove(action);
            }
        }
    }

    public <C extends ClickContext> void removeDefaultClickHandler(HotbarAction<C> action) {
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

    public <C extends ClickContext> ButtonBuilder<HotbarAction<C>, C> buttonBuilder() {
        return new ButtonBuilder<>();
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
