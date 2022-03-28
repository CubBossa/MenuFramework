package de.cubbossa.guiframework.scoreboard;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.chat.ChatMenu;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;

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
	private final Map<Integer, Collection<Animation>> animations;

	public CustomScoreboard(String identifier, Component title, int lines) {
		this.identifier = identifier;
		this.title = title;
		this.lines = Integer.min(lines, 15);

		this.scoreboards = new HashMap<>();
		this.staticEntries = new TreeMap<>();
		this.dynamicEntries = new TreeMap<>();
		this.animations = new TreeMap<>();
	}

	public void show(Player player) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboard.registerNewObjective("GUI Framework", identifier, title);
		scoreboards.put(player, obj);

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		// Static lines
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

		// Run once to set all values
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
	 * @return the index of the next free line
	 */
	public int registerStaticEntry(int line, String text, int lineLength, TextColor color) {
		return registerStaticEntry(line, text, lineLength, color, 14);
	}

	/**
	 * @param limit defines, at which line the text will no longer be rendered and cut off with a "..."
	 * @return the index of the next free line
	 */
	public int registerStaticEntry(int line, String text, int lineLength, TextColor color, int limit) {
		List<String> lines = List.of(ChatUtils.wordWrap(text, "\n", lineLength).split("\n"));
		for (int i = 0; i < lines.size(); i++) {
			String string = lines.get(i);
			if (i >= line + limit - 1) {
				registerStaticEntry(line + i, Component.text(string.substring(0, string.length() - 3) + "...", color));
				return limit;
			}
			registerStaticEntry(line + i, Component.text(string, color));
		}
		return line + lines.size();
	}

	/**
	 * @return the index of the next free line
	 */
	public int registerStaticEntry(int line, ChatMenu<?> menu) {
		return registerStaticEntry(line, menu, 14);
	}

	/**
	 * @return the index of the next free line
	 */
	public int registerStaticEntry(int line, ChatMenu<?> menu, int limit) {
		int i = line;
		for (Component component : menu.toComponents()) {
			if (i >= limit) {
				return limit;
			}
			registerStaticEntry(i, component);
			i++;
		}
		return i;
	}

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


	public void playAnimation(int line, int milliseconds, Supplier<Component> lineUpdater) {
		playAnimation(line, -1, milliseconds, lineUpdater);
	}

	public void playAnimation(int line, int intervals, int milliseconds, Supplier<Component> lineUpdater) {
		Animation animation = new Animation(line, intervals, milliseconds, lineUpdater);

		Collection<Animation> animations = this.animations.get(null);
		if (animations == null) {
			animations = new HashSet<>();
		}
		animations.add(animation);
	}

	public void stopAnimation(int... lines) {
		for (int line : lines) {
			Collection<Animation> animations = this.animations.get(line);
			if (animations != null) {
				animations.forEach(Animation::stop);
			}
		}
	}

	public class Animation {

		private final int line;
		private int intervals = -1;
		private final int milliseconds;
		private final Supplier<Component> lineUpdater;

		private BukkitTask task;

		public Animation(int line, int milliseconds, Supplier<Component> lineUpdater) {
			this.line = line;
			this.milliseconds = milliseconds;
			this.lineUpdater = lineUpdater;
		}

		public Animation(int line, int intervals, int milliseconds, Supplier<Component> lineUpdater) {
			this.line = line;
			this.intervals = intervals;
			this.milliseconds = milliseconds;
			this.lineUpdater = lineUpdater;
		}

		public void play() {
			AtomicInteger interval = new AtomicInteger(0);
			task = Bukkit.getScheduler().runTaskTimer(GUIHandler.getInstance().getPlugin(), () -> {
				if (intervals == -1 || interval.get() < intervals) {
					try {
						registerStaticEntry(line, lineUpdater.get());
					} catch (Throwable t) {
						GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while playing animation in scoreboard", t);
					}
					interval.addAndGet(1);
				}
			}, 0, milliseconds);
		}

		public void stop() {
			if (task != null && !task.isCancelled()) {
				task.cancel();
			}
		}

		public boolean isRunning() {
			return !task.isCancelled();
		}
	}
}
