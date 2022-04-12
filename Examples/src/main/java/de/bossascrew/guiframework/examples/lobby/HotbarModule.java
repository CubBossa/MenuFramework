package de.bossascrew.guiframework.examples.lobby;

import de.cubbossa.guiframework.inventory.HotbarAction;
import de.cubbossa.guiframework.inventory.HotbarMenu;
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

    private Map<UUID, HotbarMenu> hotbars;
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
        HotbarMenu hotbar = new HotbarMenu(player);

        if(plugin.getCommand(GameSelectorModule.COMMAND) != null) {
            hotbar.setItemAndClickHandler(new ItemStack(Material.NETHER_STAR), HotbarAction.LEFT_CLICK_AIR, clickContext -> {
                player.performCommand(GameSelectorModule.COMMAND);
            }, 4);
        }
        if(plugin.getCommand(LobbySelectorModule.COMMAND) != null) {
            hotbar.setItemAndClickHandler(new ItemStack(Material.EMERALD), HotbarAction.LEFT_CLICK_AIR, clickContext -> {
                player.performCommand(LobbySelectorModule.COMMAND);
            }, 7);
        }

        hotbars.put(player.getUniqueId(), hotbar);
        hotbar.open(player);
    }

    public void hide(Player player) {
        HotbarMenu hotbar = hotbars.remove(player.getUniqueId());
        if(hotbar != null) {
            hotbar.close(player);
        }
    }
}
