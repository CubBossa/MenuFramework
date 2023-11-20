package de.cubbossa.menuframework;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public interface Panel {

  @Nullable Panel getParent();

  @ApiStatus.Internal
  void setParent(Panel panel);

  Collection<Panel> getContent();

  boolean hasCell(int cell);

  void add(int offset, Panel panel);

  void remove(Panel panel);

  int[] getCells();

  int getOffset();

  void setOffset(int offset);

  void paint(Inventory inventory);

  ItemStack paint(int slot);

  default void repaint() {
    repaint(0);
  }

  void repaint(int inTicks);

  boolean requiresPaint();

  <E extends Event> void addListener(Class<E> type, Consumer<E> listener);

  <E extends Event> boolean handle(int cell, E event);
}
