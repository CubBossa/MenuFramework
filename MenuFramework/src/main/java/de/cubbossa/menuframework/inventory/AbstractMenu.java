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
import de.cubbossa.menuframework.inventory.panel.MenuIcon;
import de.cubbossa.menuframework.inventory.panel.SimplePanel;
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
import java.util.function.Supplier;

@Getter
public abstract class AbstractMenu extends SimplePanel implements Menu {

    @Getter
    protected final MenuPreset.PresetApplier applier = new MenuPreset.PresetApplier(this) {

        @Override
        public void addItem(int slot, ItemStack itemStack) {
            getDynamicItemStacks().put(slot, itemStack);
        }

        @Override
        public void addItem(int slot, Supplier<ItemStack> itemStack) {
            getDynamicItemStacks().put(slot, itemStack.get());
        }

        public void addItemOnTop(int slot, ItemStack itemStack) {
            getDynamicItemStacksOnTop().put(slot, itemStack);
        }

        @Override
        public void addItemOnTop(int slot, Supplier<ItemStack> itemStack) {
            getDynamicItemStacksOnTop().put(slot, itemStack.get());
        }

        @Override
        public <C extends TargetContext<?>> void addClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = getDynamicClickHandler().getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            getDynamicClickHandler().put(slot, map);
        }

        @Override
        public <C extends TargetContext<?>> void addClickHandlerOnTop(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
            Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = getDynamicClickHandlerOnTop().getOrDefault(slot, new HashMap<>());
            map.put(action, clickHandler);
            getDynamicClickHandlerOnTop().put(slot, map);
        }
    };

    protected @Nullable ContextConsumer<? extends TargetContext<?>> fallbackDefaultClickHandler = null;
    protected final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> defaultClickHandler;

    protected List<ContextConsumer<OpenContext>> openHandlers;
    protected List<ContextConsumer<CloseContext>> closeHandlers;

    protected final Map<Integer, Collection<Animation>> animations;
    protected final Map<UUID, ViewMode> viewer;

    protected final int slotsPerPage;
    protected int offset = 0;

    protected Inventory inventory;

    public AbstractMenu(int[] slots, int slotsPerPage) {
        super(slots);

        this.animations = new TreeMap<>();
        this.viewer = new HashMap<>();
        this.slotsPerPage = slotsPerPage;
        this.defaultClickHandler = new HashMap<>();
        this.openHandlers = new ArrayList<>();
        this.closeHandlers = new ArrayList<>();
    }

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

    public void setNextPage() {
        addOffset(slotsPerPage);
    }

    public void setPreviousPage() {
        removeOffset(slotsPerPage);
    }

    public void setPage(int page) {
        this.setOffset(page * slotsPerPage);
    }

    public void addOffset(int offset) {
        this.setOffset(this.offset + offset);
    }

    public void removeOffset(int offset) {
        this.setOffset(this.offset - offset);
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

        for (int slot : getSlots()) {
            render(slot);
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

    @Deprecated
    public MenuPreset<? extends TargetContext<?>> addPreset(MenuPreset<? extends TargetContext<?>> menuPreset) {
        try {
            menuPreset.placeDynamicEntries(applier);
            applier.generate().forEach(this::addSubPanel);
        } catch (Throwable ignored) {
        }
        return menuPreset;
    }

    public void clearContent() {
        if (inventory == null) {
            return;
        }
        for (int slot : getSlots()) {
            inventory.setItem(slot, null);
        }
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
        setButton(slot, Button.builder().withItemStack(itemSupplier));
    }

    public void refresh(int... slots) {
        if (inventory == null) {
            return;
        }
        for (int slot : slots) {
            try {
                render(slot + offset);
            } catch (ItemPlaceException e) {
                GUIHandler.getInstance().getExceptionHandler().accept(e);
            }
        }
    }

    public <T, C extends TargetContext<T>> boolean handleInteract(C context) {

        Player player = context.getPlayer();
        int slot = context.getSlot();

        if (!isPanelSlot(slot)) {
            return false;
        }
        if (viewer.containsKey(player.getUniqueId()) && viewer.get(player.getUniqueId()).equals(ViewMode.VIEW)) {
            return true;
        }

        int actualSlot = slot + offset;
        try {
            perform(actualSlot, context);
        } catch (Exception exc) {
            GUIHandler.getInstance().getExceptionHandler().accept(new MenuHandlerException(context, exc));
        }
        return context.isCancelled();
    }

    public <C extends TargetContext<?>> void setClickHandler(int slot, Action<C> action, ContextConsumer<C> clickHandler) {
        Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> map = new HashMap<>();
        map.put(action, clickHandler);
        setClickHandler(slot, map);
    }

    public void setClickHandler(int slot, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        setButton(slot, Button.builder().withClickHandler(clickHandler));
    }

    public <C extends TargetContext<?>> void setItemAndClickHandler(int slot, ItemStack item, Action<C> action, ContextConsumer<C> clickHandler) {
        setButton(slot, Button.builder().withItemStack(item).withClickHandler(action, clickHandler));
    }

    public <C extends TargetContext<?>> void setItemAndClickHandler(int slot, Supplier<ItemStack> item, Action<C> action, ContextConsumer<C> clickHandler) {
        setButton(slot, Button.builder().withItemStack(item).withClickHandler(action, clickHandler));
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

    public void removeDefaultClickHandler(Action<?> action) {
        defaultClickHandler.remove(action);
    }

    public boolean isThisInventory(Inventory inventory, Player player) {
        return this.inventory != null && this.inventory.equals(inventory);
    }

    public int getPageCount() {
        return getMaxPage() - getMinPage();
    }

    public int getCurrentPage() {
        return (int) Math.floor((double) offset / slotsPerPage);
    }

    public int getMinPage() {
        int minPage = 0;

        int smallestSlot = getSubPanels().stream()
                .filter(panel -> panel instanceof MenuIcon)
                .map(panel -> ((MenuIcon) panel).getSlot())
                .min(Integer::compareTo).orElse(0);

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

        int highestSlot = getSubPanels().stream()
                .filter(panel -> panel instanceof MenuIcon)
                .map(panel -> ((MenuIcon) panel).getSlot())
                .max(Integer::compareTo).orElse(0);

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
