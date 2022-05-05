package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.TopInventoryMenu;
import de.cubbossa.menuframework.inventory.context.ClickContext;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.util.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.ComponentLike;
import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class AnvilMenu extends TopInventoryMenu {

	public static final Action<TargetContext<String>> WRITE = new Action<>();
	public static final Action<TargetContext<String>> CONFIRM = new Action<>();

	private static final VersionWrapper WRAPPER = new VersionMatcher().match();

	@Getter
	private final int[] slots = {0, 1, 2};
	@Getter
	@Setter
	private String startText;
	@Getter
	@Setter
	private Map<Action<ClickContext>, ContextConsumer<ClickContext>> leftInputClickHandler;
	@Getter
	@Setter
	private Map<Action<ClickContext>, ContextConsumer<ClickContext>> rightInputClickHandler;
	@Getter
	@Setter
	private Map<Action<TargetContext<String>>, ContextConsumer<TargetContext<String>>> outputClickHandler;

	private int containerId;

	public AnvilMenu(ComponentLike title, String startText) {
		super(title, 3);
		this.startText = startText;
	}

	public <C extends TargetContext<?>> void setLeftInputClickHandler(Action<C> action, ContextConsumer<C> clickHandler) {
		setClickHandler(AnvilGUI.Slot.INPUT_LEFT, action, clickHandler);
	}

	public <C extends TargetContext<?>> void setRightInputClickHandler(Action<C> action, ContextConsumer<C> clickHandler) {
		setClickHandler(AnvilGUI.Slot.INPUT_RIGHT, action, clickHandler);
	}

	public <C extends TargetContext<?>> void setOutputClickHandler(Action<C> action, ContextConsumer<C> clickHandler) {
		setClickHandler(AnvilGUI.Slot.OUTPUT, action, clickHandler);
	}

	@Override
	protected Inventory createInventory(Player player, int page) {
		String title = ChatUtils.toLegacy(getTitle());

		final Object container = WRAPPER.newContainerAnvil(player, title);
		Inventory inventory = WRAPPER.toBukkitInventory(container);

		containerId = WRAPPER.getNextContainerId(player, container);
		WRAPPER.sendPacketOpenWindow(player, containerId, title);
		WRAPPER.setActiveContainer(player, container);
		WRAPPER.setActiveContainerId(container, containerId);
		WRAPPER.addActiveContainerSlotListener(container, player);

		return inventory;
	}

	@Override
	public <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context) {
		boolean cancelled = super.handleInteract(action, context);
		if (context.getAction().equals(Action.LEFT)) {
			String renameText = ((AnvilInventory) inventory).getRenameText();
			var c = new TargetContext<>(context.getPlayer(), context.getMenu(), context.getSlot(), CONFIRM, true, renameText.isEmpty() ? startText : renameText);
			return super.handleInteract(CONFIRM, c);
		}
		return cancelled;
	}

	@Override
	protected void openInventory(Player player, Inventory inventory) {

		ItemStack i = inventory.getItem(0);
		if (startText != null) {
			if (i == null) {
				i = getItemStack(0);
				if (i == null) {
					i = new ItemStack(Material.PAPER);
				}
			}
			ItemMeta paperMeta = i.getItemMeta();
			paperMeta.setDisplayName(startText);
			i.setItemMeta(paperMeta);
			inventory.setItem(0, i);
		}
	}

	@Override
	public void close(Player viewer) {

		WRAPPER.handleInventoryCloseEvent(viewer);
		WRAPPER.setActiveContainerDefault(viewer);
		WRAPPER.sendPacketCloseWindow(viewer, containerId);
		inventory = null;

		handleClose(viewer);
	}
}
