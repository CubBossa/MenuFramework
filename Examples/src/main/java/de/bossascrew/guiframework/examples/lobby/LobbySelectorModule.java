package de.bossascrew.guiframework.examples.lobby;

import com.google.common.collect.Lists;
import de.bossascrew.guiframework.examples.system.NetworkServer;
import de.bossascrew.guiframework.examples.system.ServerHandler;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LobbySelectorModule implements CommandExecutor {

    public static final String COMMAND = "lobbypicker";
    private InventoryMenu lobbyMenu;

    public LobbySelectorModule(JavaPlugin plugin) {
        lobbyMenu = new InventoryMenu(4, Component.text("Choose a lobby"));
        lobbyMenu.addPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 3));
        lobbyMenu.addPreset(MenuPresets.paginationRow(3, 0, 1, true, Action.LEFT, Action.RIGHT));

        int slot = 0;
        for(NetworkServer lobby : ServerHandler.getInstance().getServers(ServerHandler.ServerType.LOBBY)) {
            lobbyMenu.playAnimation(slot, 20, animationContext -> {
                ItemStack stack = new ItemStack(Material.EMERALD);
                ItemMeta meta = stack.getItemMeta();
                meta.displayName(Component.text(lobby.getName(), NamedTextColor.GREEN));
                meta.lore(Lists.newArrayList(Component.text("Online: " + lobby.getOnlineCount(), NamedTextColor.GRAY)));
                stack.setItemMeta(meta);
                return stack;
            });
            lobbyMenu.setClickHandler(slot, Action.LEFT, clickContext -> {
                if(ServerHandler.getInstance().canConnect(clickContext.getPlayer(), lobby)) {
                    ServerHandler.getInstance().connect(clickContext.getPlayer(), lobby);
                } else {
                    clickContext.getPlayer().playSound(clickContext.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, .8f, 1f);
                }
            });
            slot++;
        }
        Objects.requireNonNull(plugin.getCommand(COMMAND)).setExecutor(this);
    }

    public void openInventory(Player player) {
        lobbyMenu.open(player);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player) {
            openInventory(player);
        }
        return true;
    }
}
