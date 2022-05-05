package de.cubbossa.menuframework.inventory;

import java.util.ArrayList;
import java.util.List;

public interface LayeredMenu extends Menu {

	/**
	 * sum of (slot²) for each menu slot
	 * You may want to use {@link #getMaskFromSlots(int[])}
	 *
	 * @return A binary mask where every bit means that the corresponding slot is part of this layered menu.
	 */
	long getSlotMask();

	/**
	 * Sets all menu items back to the corresponding slots.
	 * You may want to use {@link #refresh(int...)} on {@link #getSlotsFromMask(long)}
	 *
	 * @param mask The mask that says which slots to refresh.
	 */
	void restoreSlots(long mask);

	static int[] getSlotsFromMask(long mask) {
		if(mask < 0) return new int[0];
		List<Integer> slots = new ArrayList<>();
		for (int i = 0; i < 9 * 4; i++) {
			if ((mask >> i & 1) == 1) {
				slots.add(i);
			}
		}
		return slots.stream().mapToInt(Integer::intValue).toArray();
	}

	static long getMaskFromSlots(int[] slots) {
		long mask = 0;
		for (int slot : slots) {
			mask += Math.pow(2, slot);
		}
		return mask;
	}
}
