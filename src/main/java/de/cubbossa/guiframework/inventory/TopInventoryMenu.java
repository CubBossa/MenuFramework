package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.util.ChatUtils;
import de.cubbossa.guiframework.util.InventoryUpdate;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.TreeMap;

@Getter
public abstract class TopInventoryMenu extends AbstractMenu implements Listener {

    private Component fallbackTitle;
    private final Map<Integer, Component> pageTitles;

    public TopInventoryMenu(Component title, int slotsPerPage) {
        super(slotsPerPage);
        this.fallbackTitle = title;
        this.pageTitles = new TreeMap<>();
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (isThisInventory(event.getInventory(), player)) {
                close(player);
            }
        }
    }

    @Override
    public void openPage(Player player, int page) {
        super.openPage(player, page);
        updateCurrentInventoryTitle(getTitle(page));
    }

    public Component getTitle(int page) {
        return pageTitles.getOrDefault(page, fallbackTitle);
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

        String name = ChatUtils.toLegacy(title);
        viewer.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
                InventoryUpdate.updateInventory(GUIHandler.getInstance().getPlugin(), player, name));
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }

    @Override
    public void firstOpen() {
        super.firstOpen();
        Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
    }

    @Override
    public void lastClose() {
        super.lastClose();
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}
