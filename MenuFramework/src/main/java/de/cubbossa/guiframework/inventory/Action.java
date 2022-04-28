package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.ClickType;

public class Action<C extends TargetContext<?>> {

    public static final Action<ClickContext> LEFT = new Action<>();
    public static final Action<ClickContext> SHIFT_LEFT = new Action<>();
    public static final Action<ClickContext> RIGHT = new Action<>();
    public static final Action<ClickContext> SHIFT_RIGHT = new Action<>();
    public static final Action<ClickContext> WINDOW_BORDER_LEFT = new Action<>();
    public static final Action<ClickContext> WINDOW_BORDER_RIGHT = new Action<>();
    public static final Action<ClickContext> MIDDLE = new Action<>();
    public static final Action<ClickContext> NUMBER_KEY = new Action<>();
    public static final Action<ClickContext> DOUBLE_CLICK = new Action<>();
    public static final Action<ClickContext> INVENTORY_DROP = new Action<>();
    public static final Action<ClickContext> CONTROL_DROP = new Action<>();
    public static final Action<ClickContext> CREATIVE = new Action<>();
    public static final Action<ClickContext> SWAP_OFFHAND = new Action<>();
    public static final Action<ClickContext> UNKNOWN = new Action<>();

    public static Action<ClickContext> fromClickType(ClickType clickType) {
        return switch (clickType) {
            case LEFT -> Action.LEFT;
            case SHIFT_LEFT -> Action.SHIFT_LEFT;
            case RIGHT -> Action.RIGHT;
            case SHIFT_RIGHT -> Action.SHIFT_RIGHT;
            case WINDOW_BORDER_LEFT -> Action.WINDOW_BORDER_LEFT;
            case WINDOW_BORDER_RIGHT -> Action.WINDOW_BORDER_RIGHT;
            case MIDDLE -> Action.MIDDLE;
            case NUMBER_KEY -> Action.NUMBER_KEY;
            case DOUBLE_CLICK -> Action.DOUBLE_CLICK;
            case DROP -> Action.INVENTORY_DROP;
            case CONTROL_DROP -> Action.CONTROL_DROP;
            case CREATIVE -> Action.CREATIVE;
            case SWAP_OFFHAND -> Action.SWAP_OFFHAND;
            case UNKNOWN -> Action.UNKNOWN;
        };
    }

    public static Action<ClickContext> HOTBAR_DROP = new Action<>();
    public static Action<ClickContext> SWAP_HANDS = new Action<>();
    public static Action<ClickContext> LEFT_CLICK_AIR = new Action<>();
    public static Action<ClickContext> RIGHT_CLICK_AIR = new Action<>();
    public static Action<TargetContext<Block>> LEFT_CLICK_BLOCK = new Action<>();
    public static Action<TargetContext<Block>> RIGHT_CLICK_BLOCK = new Action<>();
    public static Action<TargetContext<Entity>> LEFT_CLICK_ENTITY = new Action<>();
    public static Action<TargetContext<Entity>> RIGHT_CLICK_ENTITY = new Action<>();
    public static Action<TargetContext<Integer>> LEFT_CLICK_CLIENT_ENTITY = new Action<>();
    public static Action<TargetContext<Integer>> RIGHT_CLICK_CLIENT_ENTITY = new Action<>();

    public Action() {

    }
}
