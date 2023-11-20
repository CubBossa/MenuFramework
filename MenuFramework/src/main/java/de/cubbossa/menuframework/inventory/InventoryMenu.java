package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.Panel;
import de.cubbossa.menuframework.SimplePanel;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class InventoryMenu extends SimplePanel {

  public InventoryMenu(InventoryType type) {
    super(IntStream.range(0, type.getDefaultSize()).toArray());
  }

  @Override
  public @Nullable Panel getParent() {
    return null;
  }

  @Override
  public void setParent(@Nullable Panel parent) {
  }
}
