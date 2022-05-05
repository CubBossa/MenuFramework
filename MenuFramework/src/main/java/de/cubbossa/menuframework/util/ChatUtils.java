package de.cubbossa.menuframework.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class ChatUtils {

	private static final GsonComponentSerializer GSON_SERIALZIER = GsonComponentSerializer.builder().build();

	private static final LegacyComponentSerializer LEGACY_SERIALIZER_AMPERSAND = LegacyComponentSerializer.builder()
			.character('&')
			.hexColors()
			.hexCharacter('#')
			.build();

	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
			.character('§')
			.hexColors()
			.hexCharacter('x')
			.useUnusualXRepeatedCharacterHexFormat()
			.build();

	private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.builder().build();

	public static final String DURATION_FORMAT = new DurationParser(true).format(0);

	public static final String DATE_TIME_FORMAT_SHORT = "dd.MM.yy HH:mm";
	public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
	public static final DateTimeFormatter DATE_TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_SHORT);
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	/**
	 * Format: §x§1§2§3§4§5§6
	 */
	public Component fromLegacy(String legacy) {
		return LEGACY_SERIALIZER.deserialize(legacy);
	}

	/**
	 * Format: &#123456
	 */
	public Component fromChatLegacy(String legacy) {
		return LEGACY_SERIALIZER_AMPERSAND.deserialize(legacy);
	}

	public String toLegacy(ComponentLike component) {
		return LEGACY_SERIALIZER.serialize(component.asComponent());
	}

	public String toPlain(ComponentLike component) {
		return PLAIN_SERIALIZER.serialize(component.asComponent());
	}

	public String toGson(ComponentLike component) {
		return GSON_SERIALZIER.serialize(component.asComponent());
	}

	public String formatDuration(Duration duration) {
		return new DurationParser().format(duration);
	}

	public Duration parseDuration(String input) {
		return new DurationParser().parse(input);
	}

	public String formatLocalDateTime(@Nullable LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return "-";
		}
		return localDateTime.format(DATE_TIME_FORMATTER_SHORT);
	}

	public @Nullable LocalDateTime parseLocalDateTime(String string) {
		try {
			return LocalDateTime.parse(string, DATE_TIME_FORMATTER_SHORT);
		} catch (DateTimeParseException e) {
			try {
				return LocalDateTime.parse(string, DATE_TIME_FORMATTER);
			} catch (DateTimeParseException eInner) {
				eInner.printStackTrace();
				return null;
			}
		}
	}

	public String wordWrap(String toWrap, String newLine, int length) {
		return WordUtils.wrap(toWrap, length, newLine, false);
	}
}
