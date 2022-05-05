package de.cubbossa.menuframework.inventory;

import org.junit.Assert;
import org.junit.Test;

public class LayeredMenuTest {

    @Test
    public void getSlotsFromMask() {
        Assert.assertArrayEquals(new int[]{4, 5, 6, 7}, BottomMenu.getSlotsFromMask(0xf0));
        Assert.assertArrayEquals(new int[]{0, 1, 4, 5, 8, 9}, BottomMenu.getSlotsFromMask(0x333));
        Assert.assertArrayEquals(new int[0], BottomMenu.getSlotsFromMask(-1));
        Assert.assertArrayEquals(new int[0], BottomMenu.getSlotsFromMask(0));
        Assert.assertArrayEquals(new int[] {0}, BottomMenu.getSlotsFromMask(1));
    }

    @Test
    public void getMaskFromSlots() {
        Assert.assertEquals(0x30, BottomMenu.getMaskFromSlots(new int[]{4, 5}));
        Assert.assertEquals(0x333, BottomMenu.getMaskFromSlots(new int[]{0, 1, 4, 5, 8, 9}));
        Assert.assertEquals(0, BottomMenu.getMaskFromSlots(new int[0]));
        Assert.assertEquals(1, BottomMenu.getMaskFromSlots(new int[]{0}));
    }
}