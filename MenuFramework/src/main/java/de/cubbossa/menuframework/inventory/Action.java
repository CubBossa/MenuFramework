package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.inventory.context.ClickContext;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.ClickType;

public class Action<C extends TargetContext<?>> {

    public static final Action<ClickContext> LEFT = new Action<>();
    public static final Action<ClickContext> SHIFT_INSERT = new Action<>();
    public static final Action<ClickContext> SHIFT_LEFT = new Action<>();
    public static final Action<ClickContext> RIGHT = new Action<>();
    public static final Action<ClickContext> SHIFT_RIGHT = new Action<>();
    public static final Action<ClickContext> WINDOW_BORDER_LEFT = new Action<>();
    public static final Action<ClickContext> WINDOW_BORDER_RIGHT = new Action<>();
    public static final Action<ClickContext> MIDDLE = new Action<>();
    public static final Action<ClickContext> NUMBER_KEY = new Action<>();
    public static final Action<ClickContext> INVENTORY_DROP = new Action<>();
    public static final Action<ClickContext> CONTROL_DROP = new Action<>();
    public static final Action<ClickContext> CREATIVE = new Action<>();
    public static final Action<ClickContext> SWAP_OFFHAND = new Action<>();
    public static final Action<ClickContext> UNKNOWN = new Action<>();

    public static Action<ClickContext> fromClickType(ClickType clickType) {
        switch (clickType) {
            case LEFT:
            case CREATIVE:
                return Action.LEFT;
            case SHIFT_LEFT:
                return Action.SHIFT_LEFT;
            case RIGHT:
                return Action.RIGHT;
            case SHIFT_RIGHT:
                return Action.SHIFT_RIGHT;
            case WINDOW_BORDER_LEFT:
                return Action.WINDOW_BORDER_LEFT;
            case WINDOW_BORDER_RIGHT:
                return Action.WINDOW_BORDER_RIGHT;
            case MIDDLE:
                return Action.MIDDLE;
            case NUMBER_KEY:
                return Action.NUMBER_KEY;
            case DROP:
                return Action.INVENTORY_DROP;
            case CONTROL_DROP:
                return Action.CONTROL_DROP;
            case SWAP_OFFHAND:
                return Action.SWAP_OFFHAND;
            default:
                return Action.UNKNOWN;
        }
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

    public static Action<?>[] inventoryValues = new Action<?>[]{
            LEFT, SHIFT_INSERT, SHIFT_LEFT, RIGHT, SHIFT_RIGHT, WINDOW_BORDER_LEFT, WINDOW_BORDER_RIGHT,
            MIDDLE, NUMBER_KEY, INVENTORY_DROP, CONTROL_DROP, CREATIVE, SWAP_OFFHAND, UNKNOWN
    };
}
