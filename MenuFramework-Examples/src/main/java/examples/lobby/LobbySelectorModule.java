package examples.lobby;

import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.MenuPresets;
import de.cubbossa.menuframework.inventory.implementations.ListMenu;
import de.cubbossa.menuframework.util.ChatUtils;
import examples.system.NetworkServer;
import examples.system.ServerHandler;
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
import java.util.stream.IntStream;

public class LobbySelectorModule implements CommandExecutor {

    public static final String COMMAND = "lobbypicker";
    private ListMenu lobbyMenu;

    public LobbySelectorModule(JavaPlugin plugin) {
        lobbyMenu = new ListMenu(Component.text("Choose a lobby"), 4, IntStream.range(0, 3 * 9).toArray());
        lobbyMenu.addPreset(MenuPresets.fillRowOnTop(MenuPresets.FILLER_DARK, 3));
        lobbyMenu.addPreset(MenuPresets.paginationRow(3, 0, 1, true, Action.LEFT, Action.RIGHT));

        // Add all lobbies to list menu
        for (NetworkServer lobby : ServerHandler.getInstance().getServers(ServerHandler.ServerType.LOBBY)) {
            lobbyMenu.addListEntry(Button.builder()
                    // Use dynamic stack so it can be refreshed later.
                    .withItemStack(() -> {
                        ItemStack stack = new ItemStack(Material.EMERALD);
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName(ChatUtils.toLegacy(Component.text(lobby.getName(), NamedTextColor.GREEN)));
                        meta.setLore(Lists.newArrayList(Component.text("Online: " + lobby.getOnlineCount(), NamedTextColor.GRAY))
                                .stream().map(ChatUtils::toLegacy).toList());
                        stack.setItemMeta(meta);
                        return stack;
                    })
                    .withClickHandler(Action.LEFT, clickContext -> {
                        if (ServerHandler.getInstance().canConnect(clickContext.getPlayer(), lobby)) {
                            ServerHandler.getInstance().connect(clickContext.getPlayer(), lobby);
                        } else {
                            clickContext.getPlayer().playSound(clickContext.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, .8f, 1f);
                        }
                    }));
        }
        // Refresh Menu every second.
        lobbyMenu.playEndlessAnimation(20, lobbyMenu.getEntryPanel().getSlots());

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
