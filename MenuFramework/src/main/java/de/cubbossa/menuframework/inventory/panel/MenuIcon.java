package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public abstract class MenuIcon implements Panel {

    private final Panel parentPanel;
    private final int slot;
    private final Supplier<ItemStack> item;
    private final Consumer<Player> soundPlayer;
    private final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler;

    public boolean isPanelSlot(int slot) {
        return slot == this.slot;
    }

    public int getPageSize() {
        return 1;
    }

    public int[] getSlots() {
        return new int[]{slot};
    }

    public void setOffset(int offset) {
    }

    public int getOffset() {
        return 0;
    }

    public void setParentPanel(Panel parentPanel) {
    }

    public List<Panel> getSubPanels() {
        return null;
    }

    public void setSubPanel(Panel subPanel) {
    }
}
