package de.cubbossa.guiframework;

import de.cubbossa.guiframework.context.AnimationContext;
import de.cubbossa.guiframework.context.ClickContext;
import de.cubbossa.guiframework.context.CloseContext;
import de.cubbossa.guiframework.context.ContextConsumer;
import lombok.Getter;
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
public abstract class InventoryMenu {

	private Component title;

	private final SortedMap<Integer, ItemStack> itemStacks;
	private final Map<Integer, ContextConsumer<ClickContext>> clickHandler;
	private final ContextConsumer<CloseContext> closeHandler;

	private final Map<Integer, Collection<Animation>> animations;
	private final Map<Player, Navigation> viewer;

	private int slotsPerPage = -1;
	private int currentPage = 0;

	private Inventory inventory;

	public InventoryMenu(Component title, int slotsPerPage, ContextConsumer<CloseContext> closeHandler) {
		this.title = title;
		this.slotsPerPage = slotsPerPage;

		this.itemStacks = new TreeMap<>();
		this.clickHandler = new TreeMap<>();
		this.animations = new TreeMap<>();
		this.viewer = new HashMap<>();
		this.closeHandler = closeHandler;
	}

	public abstract void open(Player viewer);

	public void open(Collection<Player> viewers) {
		viewers.forEach(this::open);
	}

	public void open(Player player, InventoryMenu previous) {
		Navigation navigation = new Navigation();
		navigation.previous = previous;
		viewer.put(player, navigation);
		open(player);
	}

	public void open(Collection<Player> viewers, InventoryMenu previous) {
		viewers.forEach(player -> open(player, previous));
	}

	public abstract void close(Player viewer);

	public void closeAll(Collection<Player> viewers) {
		viewers.forEach(this::close);
	}

	public void loadPreset(Consumer<InventoryMenu> menuProcessor) {
		menuProcessor.accept(this);
	}

	public InventoryMenu openSubMenu(Player player, Supplier<InventoryMenu> menuSupplier) {
		InventoryMenu menu = menuSupplier.get();
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
		open(player);
	}

	public void updateTitle(Component title) {
		this.title = title;
		//TODO
	}

	public void handleInteract(Player player) {
		//TODO
	}

	public abstract int[] getSlots();

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

	public void setClickHandler(ContextConsumer<ClickContext> clickHandler, int... slots) {
		for (int slot : slots) {
			this.clickHandler.put(slot, clickHandler);
		}
	}

	public void setItemAndClickHandler(ItemStack item, ContextConsumer<ClickContext> clickHandler, int... slots) {
		setItem(item, slots);
		setClickHandler(clickHandler, slots);
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

	public void removeItemAndClickHandler(int... slots) {
		for (int slot : slots) {
			inventory.setItem(slot, null);
			itemStacks.remove(slot);
			clickHandler.remove(slot);
		}
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
		private ContextConsumer<AnimationContext> itemUpdater;

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
