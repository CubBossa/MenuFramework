package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.AnimationContext;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.CloseContext;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

@Getter
public abstract class AbstractInventoryMenu<T> {

	private Component fallbackTitle;
	private Map<Integer, Component> pageTitles;

	private final SortedMap<Integer, ItemStack> itemStacks;
	private final Map<Integer, Map<T, ContextConsumer<ClickContext>>> clickHandler;
	private final Map<T, ContextConsumer<ClickContext>> defaultClickHandler;
	private final Map<T, Boolean> defaultCancelled;
	@Setter
	private ContextConsumer<CloseContext> closeHandler;

	private final Map<Integer, Collection<Animation>> animations;
	private final Map<UUID, Navigation> viewer;

	private int slotsPerPage = -1;
	private int currentPage = 0;

	private Inventory inventory;

	public AbstractInventoryMenu(Component title, int slotsPerPage) {
		this.fallbackTitle = title;
		this.pageTitles = new TreeMap<>();
		this.slotsPerPage = slotsPerPage;

		this.itemStacks = new TreeMap<>();
		this.clickHandler = new TreeMap<>();
		this.defaultClickHandler = new HashMap<>();
		this.animations = new TreeMap<>();
		this.viewer = new HashMap<>();
		this.defaultCancelled = new HashMap<>();

		this.inventory = createInventory(currentPage);
	}

	public abstract Inventory createInventory(int page);

	public void open(Player viewer) {
		GUIHandler.getInstance().callSynchronized(() -> openInventorySynchronized(viewer, new Navigation()));
	}

	public void open(Collection<Player> viewers) {
		viewers.forEach(this::open);
	}

	public void open(Player viewer, AbstractInventoryMenu<T> previous) {
		Navigation navigation = new Navigation();
		navigation.previous = previous;
		GUIHandler.getInstance().callSynchronized(() -> openInventorySynchronized(viewer, navigation));
	}

	public void open(Collection<Player> viewers, AbstractInventoryMenu<T> previous) {
		viewers.forEach(player -> open(player, previous));
	}

	protected void openInventorySynchronized(Player viewer, Navigation navigation) {

		if (inventory == null) {
			GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Could not open inventory for " + viewer.getName() + ", inventory is null.");
			return;
		}
		if (viewer.isSleeping()) {
			viewer.wakeup(true);
		}
		for (int slot : getSlots()) {
			ItemStack item = itemStacks.get(slot);
			if (item == null) {
				continue;
			}
			inventory.setItem(slot, item.clone());
		}

		if (viewer.openInventory(inventory) == null) {
			return;
		}
		UUID playerId = viewer.getUniqueId();
		this.viewer.put(viewer.getUniqueId(), navigation);
	}

	public void close(Player viewer) {
		if (this.viewer.remove(viewer.getUniqueId()) == null) {
			return;
		}
		try {
			closeHandler.accept(new CloseContext(viewer, currentPage));
		} catch (Exception exc) {
			GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while closing gui for " + viewer.getName(), exc);
		}
	}

	public void closeAll(Collection<Player> viewers) {
		viewers.forEach(this::close);
	}

	public void loadPreset(Consumer<AbstractInventoryMenu<T>> menuProcessor) {
		menuProcessor.accept(this);
	}

	public AbstractInventoryMenu<T> openSubMenu(Player player, Supplier<AbstractInventoryMenu<T>> menuSupplier) {
		AbstractInventoryMenu<T> menu = menuSupplier.get();
		Navigation nav = viewer.get(player);
		nav.child = menu;
		menu.open(player, this);
		return menu;
	}

	public void openPreviousMenu(Player player) {
		viewer.get(player).parent.open(player);
	}

	public void openNextPage(Player player) {
		openPage(player, currentPage + 1);
	}

	public void openPreviousPage(Player player) {
		openPage(player, currentPage - 1);
	}

	public void openPage(Player player, int page) {
		currentPage = page;
		updateCurrentInventoryTitle(getTitle(page));
		open(player);
	}

	public Component getTitle(int page) {
		return pageTitles.getOrDefault(currentPage, fallbackTitle);
	}

	public void updateTitle(Component title) {
		this.fallbackTitle = title;
		if (!pageTitles.containsKey(currentPage)) {
			updateCurrentInventoryTitle(title);
		}
	}

	public void updateTitle(Component title, int... pages) {
		for (int page : pages) {
			pageTitles.put(page, title);
			if (currentPage == page) {
				updateCurrentInventoryTitle(title);
			}
		}
	}

	private void updateCurrentInventoryTitle(Component title) {
		//TODO update current views
	}

	public boolean isThisInventory(Inventory inventory) {
		return this.inventory != null && this.inventory.equals(inventory);
	}

	public boolean handleInteract(Player player, int clickedSlot, T action) {

		if (Arrays.stream(getSlots()).noneMatch(value -> value == clickedSlot)) {
			return false;
		}
		ClickContext context = new ClickContext(player, clickedSlot, defaultCancelled.getOrDefault(action, true));

		ContextConsumer<ClickContext> clickHandler = getClickHandlerOrFallback(clickedSlot, action);
		if (clickHandler != null) {
			//Handlung ausführen und Exceptions abfangen
			try {
				clickHandler.accept(context);
			} catch (Exception exc) {
				context.setCancelled(true);
				GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while handling GUI interaction of player " + player.getName(), exc);
			}
		}
		return context.isCancelled();
	}

	public abstract int[] getSlots();

	public ItemStack getItemStack(int slot) {
		return itemStacks.get(slot);
	}

	public ContextConsumer<ClickContext> getClickHandlerOrFallback(int slot, T action) {
		return clickHandler.getOrDefault(slot, new HashMap<>()).getOrDefault(action, defaultClickHandler.get(action));
	}

	public void clearContent() {
		for (int slot : getSlots()) {
			inventory.setItem(slot, null);
		}
	}

	public void placeContent() {
		for (Map.Entry<Integer, ItemStack> entry : itemStacks.subMap(currentPage * slotsPerPage, currentPage * (slotsPerPage + 1)).entrySet()) {
			inventory.setItem(entry.getKey(), entry.getValue());
		}
	}

	public void refresh(int... slots) {
		for (int slot : slots) {
			int realIndex = currentPage * slotsPerPage + slot;
			inventory.setItem(slot, itemStacks.get(realIndex));
		}
	}

	public void setItem(ItemStack item, int... slots) {
		for (int slot : slots) {
			itemStacks.put(slot, item);
		}
	}

	public void setClickHandler(T action, ContextConsumer<ClickContext> clickHandler, int... slots) {
		for (int slot : slots) {
			Map<T, ContextConsumer<ClickContext>> map = this.clickHandler.getOrDefault(slot, new HashMap<>());
			map.put(action, clickHandler);
			this.clickHandler.put(slot, map);
		}
	}

	public void setItemAndClickHandler(ItemStack item, T action, ContextConsumer<ClickContext> clickHandler, int... slots) {
		setItem(item, slots);
		setClickHandler(action, clickHandler, slots);
	}

	public void setDefaultClickHandler(T action, ContextConsumer<ClickContext> clickHandler) {
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
			Map<T, ContextConsumer<ClickContext>> map = clickHandler.get(slot);
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
			Map<T, ContextConsumer<ClickContext>> map = clickHandler.get(slot);
			if (map != null) {
				map.remove(action);
			}
		}
	}

	public void removeDefaultClickHandler(T action) {
		defaultClickHandler.remove(action);
	}

	public void playAnimation(int slot, int milliseconds, ContextConsumer<AnimationContext> itemUpdater) {
		playAnimation(slot, -1, milliseconds, itemUpdater);
	}

	public void playAnimation(int slot, int intervals, int milliseconds, ContextConsumer<AnimationContext> itemUpdater) {
		Animation animation = new Animation(slot, intervals, milliseconds, itemUpdater);

		Collection<Animation> animations = this.animations.get(null);
		if (animations == null) {
			animations = new HashSet<>();
		}
		animations.add(animation);
	}

	public void stopAnimation(int... slots) {
		for (int slot : slots) {
			Collection<Animation> animations = this.animations.get(slot);
			if (animations != null) {
				animations.forEach(Animation::stop);
			}
		}
	}

	public class Animation {

		private final int slot;
		private int intervals = -1;
		private final int milliseconds;
		private final ContextConsumer<AnimationContext> itemUpdater;

		private BukkitTask task;

		public Animation(int slot, int milliseconds, ContextConsumer<AnimationContext> itemUpdater) {
			this.slot = slot;
			this.milliseconds = milliseconds;
			this.itemUpdater = itemUpdater;
		}

		public Animation(int slot, int intervals, int milliseconds, ContextConsumer<AnimationContext> itemUpdater) {
			this.slot = slot;
			this.intervals = intervals;
			this.milliseconds = milliseconds;
			this.itemUpdater = itemUpdater;
		}

		public void play() {
			final ItemStack item = itemStacks.get(slot);
			AtomicInteger interval = new AtomicInteger(0);
			task = Bukkit.getScheduler().runTaskTimer(GUIHandler.getInstance().getPlugin(), () -> {
				if ((intervals == -1 || interval.get() < intervals) && item != null) {
					try {
						itemUpdater.accept(new AnimationContext(slot, intervals, item));
					} catch (Throwable t) {
						GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while playing animation in inventory menu", t);
					}
					interval.addAndGet(1);
				}
			}, 0, milliseconds);
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
