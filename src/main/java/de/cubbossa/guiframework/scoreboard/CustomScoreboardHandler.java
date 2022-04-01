package de.cubbossa.guiframework.scoreboard;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public class CustomScoreboardHandler {

	@Getter
	private static CustomScoreboardHandler instance;

	private final Map<UUID, Stack<CustomScoreboard>> scoreboards;

	public CustomScoreboardHandler() {
		instance = this;
		scoreboards = new HashMap<>();
	}

	public void registerScoreboard(Player player, CustomScoreboard scoreboard) {
		Stack<CustomScoreboard> stack = scoreboards.computeIfAbsent(player.getUniqueId(), k -> new Stack<>());
		if (!stack.isEmpty() && stack.peek().equals(scoreboard)) {
			return;
		}
		stack.push(scoreboard);
	}

	public void unregisterScoreboard(Player player, CustomScoreboard scoreboard) {
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
