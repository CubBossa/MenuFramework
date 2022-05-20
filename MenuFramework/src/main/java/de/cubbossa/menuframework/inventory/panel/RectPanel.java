package de.cubbossa.menuframework.inventory.panel;

import java.util.stream.IntStream;

public class RectPanel extends SimplePanel {

    public RectPanel(int length, int height) {
        super(IntStream.range(0, length * height).toArray());
    }
}
