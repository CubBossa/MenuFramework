package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.context.Context;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import lombok.experimental.UtilityClass;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class HotbarAction<C extends ClickContext> {

    public static HotbarAction<ClickContext> DROP = new HotbarAction<>();
    public static HotbarAction<ClickContext> SWAP_HANDS = new HotbarAction<>();
    public static HotbarAction<ClickContext> LEFT_CLICK_AIR = new HotbarAction<>();
    public static HotbarAction<ClickContext> RIGHT_CLICK_AIR = new HotbarAction<>();
    public static HotbarAction<TargetContext<Block>> LEFT_CLICK_BLOCK = new HotbarAction<>();
    public static HotbarAction<TargetContext<Block>> RIGHT_CLICK_BLOCK = new HotbarAction<>();
    public static HotbarAction<TargetContext<Entity>> LEFT_CLICK_ENTITY = new HotbarAction<>();
    public static HotbarAction<TargetContext<Entity>> RIGHT_CLICK_ENTITY = new HotbarAction<>();
    public static HotbarAction<TargetContext<Integer>> LEFT_CLICK_CLIENT_ENTITY = new HotbarAction<>();
    public static HotbarAction<TargetContext<Integer>> RIGHT_CLICK_CLIENT_ENTITY = new HotbarAction<>();

    private HotbarAction() {

    }
}
