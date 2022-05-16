package de.cubbossa.menuframework.util;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DurationParser {

	private static final DurationUnit[] units = {
			new DurationUnit(ChronoUnit.YEARS, 31_536_000_000L, "y ", "y "),
			new DurationUnit(ChronoUnit.WEEKS, 604_800_000, "w ", "w "),
			new DurationUnit(ChronoUnit.DAYS, 86_400_000, "d ", "d "),
			new DurationUnit(ChronoUnit.HOURS, 3_600_000, "h ", "h "),
			new DurationUnit(ChronoUnit.MINUTES, 60_000, "min ", "min "),
			new DurationUnit(ChronoUnit.SECONDS, 1000, "s ", "s ")
	};

	private final boolean displayEmptyUnits;
	private final List<ChronoUnit> displayedUnits;

	public DurationParser(ChronoUnit... displayedUnits) {
		this(false, displayedUnits);
	}

	public DurationParser(boolean displayEmptyUnits, ChronoUnit... displayedUnits) {
		this.displayEmptyUnits = displayEmptyUnits;
		this.displayedUnits = new ArrayList<>(Lists.newArrayList(displayedUnits));
	}

	public String format(Duration duration) {
		return format(duration.toMillis());
	}

	public String format(long millis) {
		StringBuilder result = new StringBuilder();
		for (DurationUnit unit : units) {
			if (!displayedUnits.isEmpty() && !displayedUnits.contains(unit.unit)) {
				continue;
			}
			int counter = 0;
			while (millis >= unit.milliseconds) {
				millis -= unit.milliseconds;
				counter++;
			}
			if (counter == 0) {
				if (this.displayEmptyUnits) {
					result.append(0).append(unit.plural);
				}
			} else if (counter == 1) {
				result.append(1).append(unit.singular);
			} else {
				result.append(counter).append(unit.plural);
			}
		}
		return result.toString();
	}

	public Duration parse(String input) {
		input = input.toLowerCase().replace(" ", "");
		Duration duration = Duration.ZERO;
		for (DurationUnit durationUnit : units) {

			int index = input.indexOf(durationUnit.plural.toLowerCase().replace(" ", ""));
			if (index == -1) {
				index = input.indexOf(durationUnit.singular.toLowerCase().replace(" ", ""));
			}
			if (index == -1) {
				continue;
			}
			int endIndex = index--;
			while (index >= 0 && Character.isDigit(input.charAt(index))) {
				index--;
			}
			index++;
			try {
				int result = Integer.parseInt(input.substring(index, endIndex));
				duration = duration.plus(result * durationUnit.milliseconds, ChronoUnit.MILLIS);
			} catch (NumberFormatException ignored) {
			}
		}
		return duration;
	}

	@AllArgsConstructor
	private static class DurationUnit {
		ChronoUnit unit;
		long milliseconds;
		String singular;
		String plural;
	}
}

