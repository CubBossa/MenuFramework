package de.cubbossa.menuframework;

import com.google.common.collect.Lists;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimplePanel implements Panel{

  private record PositionedPanel(int offset, Panel panel) {}

  private @Nullable Panel parent = null;
  private final List<PositionedPanel> children = new LinkedList<>();
  private final Map<Integer, PositionedPanel> childrenCellCache = new HashMap<>();
  private final SortedSet<Integer> cells;
  private int offset;
  private final Map<Class<? extends Event>, Collection<Consumer<? extends Event>>> listeners = new HashMap<>();

  private long paintingDueTimeStamp = Long.MAX_VALUE;

  public SimplePanel(int[] cells) {
    this.cells = new TreeSet<>();
    for (int cell : cells) {
      this.cells.add(cell);
    }
    this.offset = 0;
  }

  @Override
  public @Nullable Panel getParent() {
    return parent;
  }

  @Override
  public void setParent(@Nullable Panel parent) {
    this.parent = parent;
  }

  @Override
  public Collection<Panel> getContent() {
    return children.stream()
        .map(PositionedPanel::panel)
        .collect(Collectors.toList());
  }

  @Override
  public void add(int offset, Panel panel) {
    panel.setParent(this);
    var positionedPanel = new PositionedPanel(offset, panel);
    children.add(positionedPanel);
    for (int slot : panel.getCells()) {
      childrenCellCache.put(offset + slot, positionedPanel);
    }
  }

  @Override
  public void remove(Panel panel) {
    AtomicReference<PositionedPanel> match = new AtomicReference<>(null);
    children.removeIf(positionedPanel -> {
      boolean matches = positionedPanel.panel.equals(panel);
      if (matches) {
        match.set(positionedPanel);
      }
      return matches;
    });
    var matchedPanel = match.get();
    if (matchedPanel != null) {
      for (int slot : matchedPanel.panel().getCells()) {
        recalculateCache(offset + slot);
      }
    }
    panel.setParent(null);
  }

  @Override
  public boolean hasCell(int cell) {
    return cells.contains(cell);
  }

  private void recalculateCache(int slot) {
    List<PositionedPanel> reversed = Lists.reverse(children);
    for (PositionedPanel positionedPanel : reversed) {
      if (positionedPanel.offset > slot) {
        continue;
      }
      if (positionedPanel.panel.hasCell(slot - positionedPanel.offset)) {
        childrenCellCache.put(slot, positionedPanel);
        return;
      }
    }
  }

  @Override
  public int[] getCells() {
    return cells.stream().mapToInt(Integer::intValue).toArray();
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public void setOffset(int offset) {
    this.offset = offset;
  }

  public void paint(Inventory inventory) {

  }

  @Override
  public @Nullable ItemStack paint(int cell) {
    cell += offset;
    var panel = childrenCellCache.get(cell);
    if (panel != null) {
      return panel.panel.paint(cell - panel.offset);
    }
    return null;
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

    // Call all own listeners
    listeners.forEach((type, consumers) -> {
      if (event.getClass().isInstance(type)) {
        consumers.forEach(consumer -> ((Consumer<E>) consumer).accept(event));
      }
    });

    // Call all matching sub listeners
    var match = childrenCellCache.get(cell);
    if (match == null) {
      return false;
    }
    return match.panel.handle(cell - match.offset, event);
  }
}
