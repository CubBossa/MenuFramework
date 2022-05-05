package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.util.ChatUtils;
import de.cubbossa.menuframework.util.InventoryUpdate;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

@Getter
public abstract class TopInventoryMenu extends AbstractMenu implements TopMenu {

    protected final Map<UUID, TopMenu> previous;
    private Component title;
    private final Map<Integer, Component> pageTitles;

    public TopInventoryMenu(ComponentLike title, int slotsPerPage) {
        super(slotsPerPage);
        this.title = title.asComponent();
        this.pageTitles = new TreeMap<>();
        this.previous = new HashMap<>();
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

    public void openPreviousMenu(Player viewer) {
        handleClose(viewer);

        Menu previous = this.previous.remove(viewer.getUniqueId());
        if (previous != null) {
            previous.open(viewer, ViewMode.MODIFY);
        }
    }

    @Override
    public void setPage(Player player, int page) {
        super.setPage(player, page);
        updateCurrentInventoryTitle(getTitle(page));
    }

    public Component getTitle(int page) {
        return pageTitles.getOrDefault(page, title);
    }

    public void updateTitle(ComponentLike title) {
        this.title = title.asComponent();
        if (!pageTitles.containsKey(getCurrentPage())) {
            updateCurrentInventoryTitle(title);
        }
    }

    public void updateTitle(ComponentLike title, int... pages) {
        int currentPage = getCurrentPage();
        for (int page : pages) {
            pageTitles.put(page, title.asComponent());
            if (currentPage == page) {
                updateCurrentInventoryTitle(title);
            }
        }
    }

    private void updateCurrentInventoryTitle(ComponentLike title) {

        String name = ChatUtils.toLegacy(title);
        viewer.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
                InventoryUpdate.updateInventory(GUIHandler.getInstance().getPlugin(), player, name));
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }
}
