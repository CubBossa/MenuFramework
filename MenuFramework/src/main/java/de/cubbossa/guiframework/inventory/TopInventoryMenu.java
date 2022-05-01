package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.util.ChatUtils;
import de.cubbossa.guiframework.util.InventoryUpdate;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.TreeMap;

@Getter
public abstract class TopInventoryMenu extends AbstractMenu {

    private Component fallbackTitle;
    private final Map<Integer, Component> pageTitles;

    public TopInventoryMenu(ComponentLike title, int slotsPerPage) {
        super(slotsPerPage);
        this.fallbackTitle = title.asComponent();
        this.pageTitles = new TreeMap<>();
    }

    @Override
    public void setPage(Player player, int page) {
        super.setPage(player, page);
        updateCurrentInventoryTitle(getTitle(page));
    }

    public Component getTitle(int page) {
        return pageTitles.getOrDefault(page, fallbackTitle);
    }

    public void updateTitle(ComponentLike title) {
        this.fallbackTitle = title.asComponent();
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
