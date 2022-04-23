package de.bossascrew.guiframework.examples.lobby;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.BottomInventoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarModule implements Listener {

    private Map<UUID, BottomInventoryMenu> hotbars;
    private JavaPlugin plugin;

    public HotbarModule(JavaPlugin plugin) {
        this.plugin = plugin;
        hotbars = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        show(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hide(event.getPlayer());
    }

    public void show(Player player) {
        // One hotbar per player as players individual inventories are being used.
        BottomInventoryMenu hotbar = MenuPresets.newHotbarMenu();

        if(plugin.getCommand(GameSelectorModule.COMMAND) != null) {
            hotbar.setItemAndClickHandler(4, new ItemStack(Material.NETHER_STAR), Action.LEFT_CLICK_AIR, clickContext -> {
                clickContext.getPlayer().performCommand(GameSelectorModule.COMMAND);
            });
        }
        if(plugin.getCommand(LobbySelectorModule.COMMAND) != null) {
            hotbar.setItemAndClickHandler(7, new ItemStack(Material.EMERALD), Action.LEFT_CLICK_AIR, clickContext -> {
                clickContext.getPlayer().performCommand(LobbySelectorModule.COMMAND);
            });
        }

        // not exactly useful in a lobby hotbar but for tutorials sake:
        hotbar.setDefaultClickHandler(Action.HOTBAR_DROP, clickContext -> {
            hotbar.close(clickContext.getPlayer());
        });

        // keep track of hotbars to close them once player quits. Not necessary but restores players original items if needed.
        // You might want this useful for administrators to build in the lobby. Close the hotbar if they execute a build command
        // and reopen it once they toggle the build mode off again.
        hotbars.put(player.getUniqueId(), hotbar);
        hotbar.open(player);
    }

    public void hide(Player player) {
        BottomInventoryMenu hotbar = hotbars.remove(player.getUniqueId());
        if(hotbar != null) {
            hotbar.close(player);
        }
    }
}
