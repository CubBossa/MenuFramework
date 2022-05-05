# Minecraft GUI Framework

## Content

- Usage
  - Maven
  - Setup
- Skip Reading
- Inventory Menus
- Scoreboards
- Chat Menus

## Usage

### Maven

Install the framework by adding jitpack as repository and the dependency below
to your pom.xml.

```XML
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.CubBossa</groupId>
        <artifactId>Minecraft-GUI-Framework</artifactId>
        <version>[VERSION]</version>
    </dependency>
</dependencies>
```

The Kyori Adventure API is required to use Minecraft-GUI-Framework.
You can shade both this library and adventure like so:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.3.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <filters>
                    <filter>
                        <artifact>net.kyori:adventure-api</artifact>
                        <includes>
                            <include>**</include>
                        </includes>
                    </filter>
                    <filter>
                        <artifact>de.cubbossa.menuframeworkde.cubbossa.menuframework</artifact>
                        <includes>
                            <include>**</include>
                        </includes>
                    </filter>
                </filters>
                <relocations>
                    <relocation>
                        <pattern>net.kyori</pattern>
                        <shadedPattern>[YOUR_PLUGIN_PATH].kyori</shadedPattern>
                    </relocation>
                    <relocation>
                        <pattern>de.cubbossa.menuframeworkde.cubbossa.menuframework</pattern>
                        <shadedPattern>[YOUR_PLUGIN_PATH].guiframework</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Setup

To get started, you have to initialize the GUIHandler class in your plugin onEnable method and 
disable it in the onDisable method:

```java
    @Override
    public void onEnable() {
        GUIHandler guiHandler = new GUIHandler(this);
        guiHandler.enable();
    }

    @Override
    public void onDisable() {
        GUIHandler.getInstance().disable();
    }
```


## Skip Reading

If you don't want to read the whole documentation but want to get started,
check out the classes:
- [Menu.java](https://github.com/CubBossa/Minecraft-GUI-Framework/blob/master/src/main/java/de/cubbossa/guiframework/inventory/Menu.java) for a method overview
- [InventoryMenu.java](https://github.com/CubBossa/Minecraft-GUI-Framework/blob/master/src/main/java/de/cubbossa/guiframework/inventory/implementations/InventoryMenu.java) as basic menu implementation
- [MenuPresets.java](https://github.com/CubBossa/Minecraft-GUI-Framework/blob/master/src/main/java/de/cubbossa/guiframework/inventory/MenuPresets.java) for predefined inventories and presets

## Inventory Menus

### Overview

To prevent programmers from creating every single GUI from scratch and dealing with endless Inventory Listeners, MCGUI Framework
automatically handles listening to bukkit events and calling your code.

Inventory Menus are defined by InventoryMenu objects. One InventoryMenu displays one paginated inventory, that can be shared by multiple players.
Keep in mind, that every player sees the same changes in the inventory, so if you want to show items based on the user, create one InventoryMenu per player.

Inventory menus are divided into TopInventoryMenus and LayeredMenus.
TopInventoryMenus are, as the name says, all menus that open on top of the player inventory (chest, merchant, hopper, furnace, anvil, ...).
LayeredMenus are all menus, that are displayed in the players actual inventory.

There is one important difference between there types:
There can only ever be one TopInventoryMenu per player, while every player can have as much LayeredMenus opened as the admins want them to.
TopInventoryMenus automatically close, when a new menu is opened, which is not the case for LayeredMenus.

This adds a special behaviour for closing TopInventoryMenus: If you provide a previous menu or open a menu as a sub menu, the parent menu will open
automatically, once the child menu has been closed. This allows programmers to create a hierarchy of menus that always leads back to the main menu.
If this behaviour is not required, simply open the new menu with its open method. If the "previous" parameter is set to null, all parent menus are cleared
and no inventory will open when the menu is closed.

LayeredMenus instead don't close if players close their inventories. LayeredMenus can cover any slots of the player inventory (first row, every second
slot, etc...). If multiple LayeredMenus contain the same slot, the most recent added menu will be rendered and executed then clicked.
Hence, you always have to close LayeredMenus if you open them to restore the players original inventories and prevent item duping.
LayeredMenus only close automatically if the player disconnects.

Example for a TopInventoryMenu with multiple presets:
```Java
public class Example {

    public void openExampleMenu(Player player) {

        // Creates a chest menu with 2 rows and the title Lobby Selector
        ListMenu menu = MenuPresets.newListMenu(Component.text("Lobby Selector"), 3,
                    // LobbyHandler derives from ListMenuSupplier and can be displayed as list
                    ServerHandler.getInstance(),
                    // Action that triggers the call of the click handler
                    Action.LEFT,
                    // Will be called when a lobby icon is clicked. The targetContext contains the lobby as target
                    targetContext -> {
                        ServerHandler.getInstance().connect(clickContext.getPlayer(), targetContext.getTarget());
                    }  );

        // Open the GUI for the given player
        menu.open(player);
    }
}
```

### Pagination

When inserting items and clickhandlers into the menu (e.g. `Menu#setItem(int, ItemStack)`), you can input
every slot you like. Since every inventory has a limited amount of slots, only items on those slots are being added
to the menu inventory.
Buttons at higher or negative slots can be displayed by changing the current page.
The first index of each page can be calculated like so: `slots_per_page x current_page`
To change the rendered page, call `Menu#openPage(Player, int)`. This does not open the inventory, it only changes the
items in the menu inventory to the items for the provided page.

The MenuPresets class contains pagination presets that can be added with `Menu#addPreset(...)` and allow the menu users
to turn pages via buttons.

The maximum page is the page that contains the item or click handler with the highest slot,
the minimum page is the page that contains the lowest slot.

### Menu Presets

Some inventory buttons are required many times, but always do the same thing. They also don't need to be static buttons
that have to be removed manually if not wanted anymore.
Therefore, you can add MenuPresets to your menu. MenuPresets add items and click handlers are always added to the currently opened
page if the slot is not already taken by a static button.

The MenuPresets class contains presets for filling inventory backgrounds, adding rows, adding pagination items and back items.
You can create own MenuPresets by implementing the MenuPreset interface. Use the provided functions of the interface to add
items and click handlers dynamically. If you call `Menu#setItem(int, ItemStack)` in the MenuPreset, you would add a static
item, which is not what MenuPresets are expected to do.
Still, the interface method holds a reference to the menu, so that you can refresh the menu or change pages if necessary.

Example MenuPreset that dynamically fills the background of the menu with the provided ItemStack.
```Java
    public static MenuPreset<? extends TargetContext<?>> fill(ItemStack stack) {
        return (menu, addItem, addClickHandler) -> {
            Arrays.stream(menu.getSlots()).forEach(value -> addItem.accept(value, stack));
        };
    }
```

### Default ClickHandler

### Default Cancelling

### CloseHandler


### Animations

## Scoreboards

### Overview

CustomScoreboards provide simplified methods to display sidebars in Minecraft.
CustomScoreboards are structured in a stack, so hiding a scoreboard will reveal the CustomScoreboard that was visible before.
This only accounts for CustomScoreboard from this framework and if the scoreboard hasn't been hidden from the player before.

```Java
public class Example {
    public void showScoreboard(Player player) {
    // New Scoreboard with lobby as key, the name of the lobby as name and 5 lines. Store this instance to toggle and update the scoreboard lateron.
	CustomScoreboard scoreboard = new CustomScoreboard("lobby", ServerHandler.getInstance().getCurrentServer().getDisplayName(), 5);

    // Static entry in line 2 -> won't update
	scoreboard.setLine(2, Component.text("Jump'n'Run Score:"));
	// Dynamic entry in line 3 -> can be updated, e.g. in a JumpNRun Listener to refresh the high score
	scoreboard.setLine(3, () -> Component.text(JumpNRunHandler.getInstance().getFormattedHighScore(player)));

    // Show the scoreboard instance to the player
	scoreboard.show(player);
    }
}
```

## Chat Menus

### Overview

Chat menus provide a simple way to display many information in chat, scoreboards and item lores.
They are basically lists with indents. Therefore, they use a parent child structure, so that you can add multiple child menus to each chat menu object.
Every ChatMenu object can be rendered and will use all child menus to create the Component List to use in Chat.

Code:
```Java
        TextMenu online = new TextMenu("Spieler online:");
        Bukkit.getOnlinePlayers().forEach(p -> online.addSub(new ComponentMenu(p.displayName())));
        online.send(player);
```
Result:
```
        Spieler online:
         >  CubBossa
         >  Steve
         >  Alex
```

### Types

There are three predefined ChatMenu types: TextMenu, ComponentMenu and ItemMenu.
- TextMenu simply renders a string, that can contain legacy formatting.
- ComponentMenu can be instantiated with a Component like the players display name.
- ItemMenu uses the display name or translation key of an item stack and adds the item meta as lore.

To implement own ChatMenu types, simply derive from ChatMenu and use the class as generic type that is supposed to be
converted to a menu entry.

To change the way menus are rendered, you can change the static INDENT_COMPONENT component in the ChatMenu class.
It will be added before each indented sub menu.