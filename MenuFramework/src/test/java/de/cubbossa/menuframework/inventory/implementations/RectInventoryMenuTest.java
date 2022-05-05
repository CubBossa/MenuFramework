package de.cubbossa.menuframework.inventory.implementations;

import org.junit.Assert;
import org.junit.Test;

public class RectInventoryMenuTest {

    @Test
    public void applyOffsetTopRight() {
        Assert.assertEquals(7, RectInventoryMenu.OFFSET_TOP_RIGHT.applyOffset(3, 4, 3));
    }

    @Test
    public void applyOffsetTopDown() {
        Assert.assertEquals(9, RectInventoryMenu.OFFSET_TOP_DOWN.applyOffset(0, 1, 3));
        Assert.assertEquals(-1, RectInventoryMenu.OFFSET_TOP_DOWN.applyOffset(18, 1, 3));
    }


    @Test
    public void applyOffsetTopLeft() {
        //Assert.assertEquals(2, RectInventoryMenu.OFFSET_TOP_LEFT.applyOffset(5, 3, 3));
        //Assert.assertEquals(13, RectInventoryMenu.OFFSET_TOP_LEFT.applyOffset(2, 7, 3));
    }




}