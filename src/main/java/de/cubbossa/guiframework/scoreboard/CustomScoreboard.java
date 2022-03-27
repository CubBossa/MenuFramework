package de.cubbossa.guiframework.scoreboard;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.Supplier;

public class CustomScoreboard {

	public record ScoreboardEntry(String key, Supplier<Component> componentSupplier) {
	}

	private final String identifier;
	@Getter
	private Component title;
	private final int lines;

	private final Map<Player, Objective> scoreboards;
	private final Map<Integer, Component> staticEntries;
	private final Map<Integer, ScoreboardEntry> dynamicEntries;

	public CustomScoreboard(String identifier, Component title, int lines) {
		this.identifier = identifier;
		this.title = title;
		this.lines = Integer.min(lines, 15);

		this.scoreboards = new HashMap<>();
		this.staticEntries = new HashMap<>();
		this.dynamicEntries = new HashMap<>();
	}

	public void show(Player player) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboard.registerNewObjective("BossasCrew", identifier, title);
		scoreboards.put(player, obj);

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		//Statische Zeilen setzen
		for (int i = 0; i < lines; i++) {
			int line = lines - i;
			String lineHex = Integer.toHexString(line);
			String scoreString = "§" + lineHex + ChatColor.WHITE;
			Component staticEntry = staticEntries.get(i);

			obj.getScore(scoreString).setScore(line);

			if (staticEntry == null) {
				continue;
			}
			Team team = scoreboard.getTeam(identifier + i);
			if (team == null) {
				team = scoreboard.registerNewTeam(identifier + i);
			}
			team.addEntry(scoreString);
			team.prefix(staticEntry);
		}

		//Erstes Update ausführen um alle Werte zu setzen
		if (!dynamicEntries.isEmpty()) {
			update(player);
		}
		player.setScoreboard(scoreboard);
		CustomScoreboardHandler.getInstance().registerScoreboard(player, this);
	}

	public void show(Collection<Player> players) {
		for (Player player : players) {
			show(player);
		}
	}

	public void update(Player player) {
		Objective obj = scoreboards.get(player);
		if (obj == null || obj.getScoreboard() == null) {
			return;
		}
		for (int i = 0; i < lines; i++) {
			updateLine(obj, i);
		}
	}

	public void update(Collection<Player> players) {
		for (Player player : players) {
			update(player);
		}
	}

	public void updateLine(Player player, int index) {
		Objective objective = scoreboards.get(player);
		if (objective == null) {
			return;
		}
		updateLine(objective, index);
	}

	public void updateLine(Collection<Player> players, int index) {
		for (Player player : players) {
			updateLine(player, index);
		}
	}

	private void updateLine(Objective objective, int index) {
		if (objective.getScoreboard() == null) {
			return;
		}
		int line = lines - index;
		String lineHex = Integer.toHexString(line);
		ScoreboardEntry dynamicEntry = dynamicEntries.get(index);
		if (dynamicEntry == null) {
			return;
		}
		Team team = objective.getScoreboard().getTeam(dynamicEntry.key);
		if (team == null) {
			team = objective.getScoreboard().registerNewTeam(dynamicEntry.key);
		}
		Component toSet = dynamicEntry.componentSupplier.get();
		if (Objects.equals(toSet.toString(), team.prefix().toString())) {
			return;
		}
		team.addEntry("§" + lineHex + ChatColor.WHITE + "");
		team.prefix(toSet);
		objective.getScore("§" + lineHex + ChatColor.WHITE + "").setScore(line);
	}

	public void hide(Player player) {
		Objective obj = scoreboards.get(player);
		if (obj != null && obj.getScoreboard() != null) {
			obj.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		}
		CustomScoreboardHandler.getInstance().unregisterScoreboard(player, this);
	}

	public void hide(Collection<Player> players) {
		for (Player player : players) {
			hide(player);
		}
	}

	public void registerStaticEntry(int line, Component entry) {
		this.staticEntries.put(line, entry);
	}

	/**
	 * @return den Index der nächsten freien Zeile
	 */
	/*public int registerStaticEntry(int line, String text, int lineLength, TextColor color) {
		return registerStaticEntry(line, text, lineLength, color, 14);
	}*/

	/**
	 * @param limit Gibt an, welche Zeile nicht überschritten werden darf. Es wird automatisch ein "..." angehängt, falls nicht der ganze Text auf das Scoreboard passt.
	 * @return den Index der nächsten freien Zeile
	 */
	/*public int registerStaticEntry(int line, String text, int lineLength, TextColor color, int limit) {
		List<String> lines = List.of(CommandUtils.wordWrap(text, "\n", 36).split("\n"));
		for (int i = 0; i < lines.size(); i++) {
			String string = lines.get(i);
			if (i >= line + limit - 1) {
				registerStaticEntry(line + i, Component.text(string.substring(0, string.length() - 3) + "...", color));
				return limit;
			}
			registerStaticEntry(line + i, Component.text(string, color));
		}
		return line + lines.size();
	}*/

	/**
	 * @return den Index der nächsten freien Zeile
	 */
	/*public int registerStaticEntry(int line, RecursiveChatElement<?> menu) {
		return registerStaticEntry(line, menu, 14);
	}*/

	/**
	 * @return den Index der nächsten freien Zeile
	 */
	/*public int registerStaticEntry(int line, RecursiveChatElement<?> menu, int limit) {
		int i = line;
		for (Component component : menu.toComponents()) {
			if (i >= limit) {
				return limit;
			}
			registerStaticEntry(i, component);
			i++;
		}
		return i;
	}*/

	public void registerDynamicEntry(int line, Supplier<Component> entry) {
		this.dynamicEntries.put(line, new ScoreboardEntry(identifier + line, entry));
	}

	public void setTitle(Component component) {
		this.title = component;
		for (Objective objective : scoreboards.values()) {
			objective.displayName(component);
		}
	}

	public void setTitle(Component component, Player player) {
		Objective objective = scoreboards.get(player);
		objective.displayName(component);
	}
}
