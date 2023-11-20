package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.Panel;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SingleItemPanel implements Panel {

  private @Nullable Panel parent;
  private final int[] cells = new int[]{ 0 };

  private final Supplier<ItemStack> stackSupplier;
  private final Map<Class<? extends Event>, Collection<Consumer<? extends Event>>> listeners = new HashMap<>();

  private long paintingDueTimeStamp = Long.MAX_VALUE;

  public SingleItemPanel(Supplier<ItemStack> stackSupplier) {
    this.stackSupplier = stackSupplier;
  }

  @Override
  public @Nullable Panel getParent() {
    return parent;
  }

  @Override
  public void setParent(@Nullable Panel panel) {
    this.parent = panel;
  }

  @Override
  public Collection<Panel> getContent() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasCell(int cell) {
    return cell == 0;
  }

  @Override
  public void add(int offset, Panel panel) {
    throw new IllegalStateException("Single Item Panel cannot contain sub panels.");
  }

  @Override
  public void remove(Panel panel) {
    throw new IllegalStateException("Single Item Panel cannot contain sub panels.");
  }

  @Override
  public int[] getCells() {
    return cells;
  }

  @Override
  public int getOffset() {
    return 0;
  }

  @Override
  public void setOffset(int offset) {
    throw new IllegalStateException("Single Item Panel cannot have an offset");
  }

  @Override
  public void paint(Inventory inventory) {
    inventory.setItem(0, stackSupplier.get());
  }

  @Override
  public ItemStack paint(int slot) {
    if (slot != 0) {
      return null;
    }
    return stackSupplier.get();
  }

  @Override
  public void repaint(int inTicks) {
    paintingDueTimeStamp = System.currentTimeMillis() + inTicks * 20L;
  }

  @Override
  public boolean requiresPaint() {
    return System.currentTimeMillis() > paintingDueTimeStamp;
  }

  @Override
  public <E extends Event> void addListener(Class<E> type, Consumer<E> listener) {
    listeners.computeIfAbsent(type, aClass -> new LinkedList<>()).add(listener);
  }

  @Override
  public <E extends Event> boolean handle(int cell, E event) {
    listeners.forEach((type, consumers) -> {
      if (event.getClass().isInstance(type)) {
        consumers.forEach(consumer -> ((Consumer<E>) consumer).accept(event));
      }
    });
    return true;
  }
}
