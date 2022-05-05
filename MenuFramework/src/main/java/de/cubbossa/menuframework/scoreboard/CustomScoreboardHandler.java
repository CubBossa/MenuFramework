package de.cubbossa.menuframework.scoreboard;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

public class CustomScoreboardHandler {

	@Getter
	private static CustomScoreboardHandler instance;

	private final Map<UUID, Stack<CustomScoreboard>> scoreboards;

	public CustomScoreboardHandler() {
		instance = this;
		scoreboards = new HashMap<>();
	}

	/**
	 * Closes all opened Scoreboards without opening the underlying scoreboard from stack. Use this before
	 * disabling the plugin to remove unexpected behaviour.
	 */
	public void closeAllScoreboards() {
		scoreboards.values().forEach(customScoreboards -> {
			if (!customScoreboards.isEmpty()) {
				customScoreboards.peek().hide(customScoreboards.peek().getViewers());
			}
		});
		scoreboards.clear();
	}

	protected void registerScoreboard(Player player, CustomScoreboard scoreboard) {
		Stack<CustomScoreboard> stack = scoreboards.computeIfAbsent(player.getUniqueId(), k -> new Stack<>());
		if (!stack.isEmpty() && stack.peek().equals(scoreboard)) {
			return;
		}
		stack.push(scoreboard);
	}

	protected void unregisterScoreboard(Player player, CustomScoreboard scoreboard) {
		Stack<CustomScoreboard> stack = scoreboards.get(player.getUniqueId());
		if (stack != null) {
			stack.remove(scoreboard);
			if (!stack.isEmpty()) {
				CustomScoreboard toShow = stack.peek();
				if (toShow != null) {
					toShow.show(player);
				}
			}
		}
	}
}
