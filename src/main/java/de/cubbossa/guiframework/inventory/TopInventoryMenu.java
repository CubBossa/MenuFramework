package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@Getter
public abstract class TopInventoryMenu<T> extends AbstractInventoryMenu<T, ClickContext> {

    private Component fallbackTitle;
    private final Map<Integer, Component> pageTitles;

    public TopInventoryMenu(Component title, int slotsPerPage) {
        super(slotsPerPage);
        this.fallbackTitle = title;
        this.pageTitles = new TreeMap<>();
    }

    @Override
    public void openPage(Player player, int page) {
        updateCurrentInventoryTitle(getTitle(page));
        super.openPage(player, page);
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
        GUIHandler.getInstance().callSynchronized(() -> {
            Inventory old = inventory;
            Optional<UUID> anyPlayer = viewer.keySet().stream().findAny();
            if (anyPlayer.isPresent()) {
                this.inventory = createInventory(Bukkit.getPlayer(anyPlayer.get()), currentPage);
                this.inventory.setContents(old.getContents());
                for (Player viewer : viewer.keySet().stream().map(Bukkit::getPlayer).toList()) {
                    viewer.openInventory(this.inventory);
                }
            }
        });
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }
}
