package de.cubbossa.guiframework.inventory;

import org.junit.Assert;
import org.junit.Test;

public class LayeredMenuTest {

    @Test
    public void getSlotsFromMask() {
        Assert.assertArrayEquals(new int[]{4, 5, 6, 7}, LayeredMenu.getSlotsFromMask(0xf0));
        Assert.assertArrayEquals(new int[]{0, 1, 4, 5, 8, 9}, LayeredMenu.getSlotsFromMask(0x333));
        Assert.assertArrayEquals(new int[0], LayeredMenu.getSlotsFromMask(-1));
        Assert.assertArrayEquals(new int[0], LayeredMenu.getSlotsFromMask(0));
        Assert.assertArrayEquals(new int[] {0}, LayeredMenu.getSlotsFromMask(1));
    }

    @Test
    public void getMaskFromSlots() {
        Assert.assertEquals(0x30, LayeredMenu.getMaskFromSlots(new int[]{4, 5}));
        Assert.assertEquals(0x333, LayeredMenu.getMaskFromSlots(new int[]{0, 1, 4, 5, 8, 9}));
        Assert.assertEquals(0, LayeredMenu.getMaskFromSlots(new int[0]));
        Assert.assertEquals(1, LayeredMenu.getMaskFromSlots(new int[]{0}));
    }
}