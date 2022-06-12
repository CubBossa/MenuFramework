package de.cubbossa.menuframework.inventory.panel;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.exception.ItemPlaceException;
import de.cubbossa.menuframework.inventory.exception.MenuHandlerException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class MenuIcon implements Panel {

    @Setter
    private Panel parentPanel;
    private final Supplier<ItemStack> item;
    private final Consumer<Player> soundPlayer;
    private final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler;
    private final int priority;

    public MenuIcon(Supplier<ItemStack> item, Consumer<Player> soundPlayer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        this(item, soundPlayer, clickHandler, 1);
    }
    public MenuIcon(Supplier<ItemStack> item, Consumer<Player> soundPlayer, Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler, int priority) {
        this.priority = priority;
        this.item = item;
        this.soundPlayer = soundPlayer;
        this.clickHandler = clickHandler;
    }

    public boolean isPanelSlot(int slot) {
        return slot == 0;
    }

    public int getPageSize() {
        return 1;
    }

    public int[] getSlots() {
        return new int[]{0};
    }

    public void setOffset(int offset) {
    }

    public int getOffset() {
        return 0;
    }

    public List<Panel> getSubPanels() {
        return null;
    }

    public void setButton(int slot, Button button) {
    }

    public void addSubPanel(int position, Panel subPanel) {
    }

    public void clearSubPanels() {
    }

    public void render(int slot) throws ItemPlaceException {
    }

    public <T> boolean perform(int slot, TargetContext<T> context) throws MenuHandlerException {
        ContextConsumer<TargetContext<T>> clickHandler = (ContextConsumer<TargetContext<T>>) getClickHandler().get(context.getAction());
        if (clickHandler == null) {
            return true;
        }
        try {
            clickHandler.accept(context);
        } catch (Throwable t) {
            throw new MenuHandlerException(context, t);
        }
        return context.isCancelled();
    }

    @Override
    public int getMinPage() {
        return 0;
    }

    @Override
    public int getMaxPage() {
        return 0;
    }
}
