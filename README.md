# Minecraft GUI Framework

## Content

- Inventory Menus
- Scoreboards
- Chat Menus

## Inventory Menus

### Overview

To prevent programmers from creating every single GUI from scratch and dealing with endless Inventory Listeners, MCGUI Framework
automatically handles listening to bukkit events and calling your code.

Inventory Menus are defined by InventoryMenu objects. One InventoryMenu displays one paginated inventory, that can be shared by multiple players.
Keep in mind, that every player sees the same changes in the inventory, so if you want to show items based on the user, create one InventoryMenu per player.
(It might be best practice to just create a new InventoryMenu on each opening)

```Java
public class Example {
    public void openExampleMenu(Player player) {
        // Creates a chest menu with 2 rows and the title Lobby Selector
        InventoryMenu menu = new InventoryMenu(2, Component.text("Lobby Selector");

        // Iterate lobbys
        int slot = 0;
        for(Lobby lobby : ServerHandler.getInstance().getLobbies()) {
            // Add a menu entry for each lobby, connect player to lobby when clicked
            menu.addItemAndClickHandler(
                // Creaate display item
                ItemUtils.createItem(Material.EMERALD, lobby.getName(), lobby.getOnlineCount(),
                // Only call on right click
                ClickType.RIGHT_CLICK,
                // Open lambda expression with code to execute on interaction.
                // clickContext contains relevant information for the item interaction
                clickContext -> {

                // important to use clickContext.getPlayer(), so if the inventory is shared by multiple players, the clicking player will be teleported
                ServerHandler.getInstance().connect(clickContext.getPlayer(), lobby);
            }, slot);
            slot++;
        }
        // Open the GUI for the given player
        menu.open(player);
    }
}
```

### Default ClickHandler

### Default Cancelling

### CloseHandler

### Pagination

### Animations

## Scoreboards

### Overview

CustomScoreboards provide simplified methods to display sidebars in Minecraft.
CustomScoreboards are structured in a stack, so hiding a scoreboard will reveal the CustomScoreboard that was visible before.
This only accounts for CustomScoreboard from this Framework and if the scoreboard hasn't been hidden for the player before.

```Java
public class Example {
    public void showScoreboard(Player player) {
        // New Scoreboard with lobby as key, the name of the lobby as name and 5 lines. Store this instance to toggle and update the scoreboard lateron.
	CustomScoreboard scoreboard = new CustomScoreboard("lobby", ServerHandler.getInstance().getCurrentServer().getDisplayName(), 5);

        // Static entry in line 2 -> won't update
	scoreboard.registerStaticEntry(2, Component.text("Jump'n'Run Score:"));
	// Dynamic entry in line 3 -> can be updated, e.g. in a JumpNRun Listener to refresh the high score
	scoreboard.registerDynamicEntry(3, () -> Component.text(JumpNRunHandler.getInstance().getFormattedHighScore(player)));

        // Show the scoreboard instance to the player
	scoreboard.show(player);
    }
}
```

## Chat Menus
