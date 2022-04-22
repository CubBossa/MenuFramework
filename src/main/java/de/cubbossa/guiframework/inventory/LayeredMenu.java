package de.cubbossa.guiframework.inventory;

import java.util.ArrayList;
import java.util.List;

public interface LayeredMenu {

	long getSlotMask();

	void restoreSlots(long mask);

	static int[] getSlotsFromMask(long mask) {
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
