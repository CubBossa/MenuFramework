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

	@Getter
	private final String identifier;
	@Getter
	private Component title;
	private final int lines;

	private final Map<Player, Objective> scoreboards;
	private final Map<Integer, Component> staticEntries;
	private final Map<Integer, ScoreboardEntry> dynamicEntries;
	private final Map<Integer, Collection<Animation>> animations;

	/**
	 * @param identifier a unique identifier for this scoreboard
	 * @param title      the component title of this scoreboard
	 * @param lines      the amount of lines for this scoreboard with a maximum of 15
	 */
	public CustomScoreboard(String identifier, Component title, int lines) {
		this.identifier = identifier;
		this.title = title;
		this.lines = Integer.min(lines, 15);

		this.scoreboards = new HashMap<>();
		this.staticEntries = new TreeMap<>();
		this.dynamicEntries = new TreeMap<>();
		this.animations = new TreeMap<>();
	}

	/**
	 * Makes this scoreboard visible for a certain player. If another {@link CustomScoreboard} was opened before, the player
	 * will open this scoreboard on top. Once this scoreboard is being removed, the previous scoreboard will reappear.
	 *
	 * @param player the player to show this scoreboard to
	 */
	public void show(Player player) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = scoreboard.registerNewObjective("GUI Framework " + GUIHandler.getInstance().getPlugin().getName(), identifier, title);
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

	/**
	 * Makes this scoreboard visible for multiple players
	 *
	 * @param players the players to show the scoreboard to
	 */
	public void show(Collection<Player> players) {
		for (Player player : players) {
			show(player);
		}
	}

	/**
	 * @return all players that are supposed to see this scoreboard at the moment. Other plugins can override the current scoreboard but the player still counts as viewing this scoreboard.
	 */
	public Collection<Player> getViewers() {
		return scoreboards.keySet();
	}

	/**
	 * Updates all dynamic lines of this scoreboard for the given player
	 *
	 * @param player the player to update this scoreboard for
	 */
	public void update(Player player) {
		Objective obj = scoreboards.get(player);
		if (obj == null || obj.getScoreboard() == null) {
			return;
		}
		for (int i = 0; i < lines; i++) {
			updateLine(obj, i);
		}
	}

	/**
	 * Updates all dynamic lines of this scoreboard for the given players
	 *
	 * @param players the players to update this scoreboard for
	 */
	public void update(Collection<Player> players) {
		for (Player player : players) {
			update(player);
		}
	}

	/**
	 * Updates a certain dynamic line of this scoreboard for a given player
	 *
	 * @param player the player to update this scoreboard for
	 * @param index  the line index
	 */
	public void updateLine(Player player, int index) {
		Objective objective = scoreboards.get(player);
		if (objective == null) {
			return;
		}
		updateLine(objective, index);
	}

	/**
	 * Updates a certain dynamic line of this scoreboard for the given players
	 *
	 * @param players the players to update this scoreboard for
	 * @param index   the line index
	 */
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

	/**
	 * Hides this scoreboard from the given player
	 *
	 * @param player the player to hide this scoreboard from
	 */
	public void hide(Player player) {
		Objective obj = scoreboards.get(player);
		if (obj != null && obj.getScoreboard() != null) {
			obj.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		}
		CustomScoreboardHandler.getInstance().unregisterScoreboard(player, this);
	}

	/**
	 * Hides this scoreboard from the given players
	 *
	 * @param players the players to hide this scoreboard from
	 */
	public void hide(Collection<Player> players) {
		for (Player player : players) {
			hide(player);
		}
	}

	/**
	 * Registers a static line that cannot be changed later on.
	 * If you want dynamic text that can be changed, use {@link #registerDynamicEntry(int, Supplier)} instead.
	 *
	 * @param line  the line to insert this entry to
	 * @param entry the component to display
	 */
	public void registerStaticEntry(int line, Component entry) {
		this.staticEntries.put(line, entry);
	}

	/**
	 * Registers a static line that cannot be changed later on.
	 * If you want dynamic text that can be changed, use {@link #registerDynamicEntry(int, Supplier)} instead.
	 * <br>
	 * Displays a text with automatic word wrapping in a given color.
	 *
	 * @param line  the line to insert this entry to
	 * @param text the text to display
	 * @param lineLength the amount of characters that are allowed in one line
	 * @param color the color to start each line with
	 *
	 * @return the index of the next free line after inserting the word wrapped text
	 */
	public int registerStaticEntry(int line, String text, int lineLength, TextColor color) {
		return registerStaticEntry(line, text, lineLength, color, 14);
	}

	/**
	 * Registers a static line that cannot be changed later on.
	 * If you want dynamic text that can be changed, use {@link #registerDynamicEntry(int, Supplier)} instead.
	 * <br>
	 * Displays a text with automatic word wrapping in a given color. Once the given line limit was reached, it
	 * cuts off the text and ends it with "..."
	 *
	 * @param line  the line to insert this entry to
	 * @param text the text to display
	 * @param lineLength the amount of characters that are allowed in one line
	 * @param color the color to start each line with
	 * @param limit the line limit. Once the text is longer than the given limit, the text gets truncated
	 *
	 * @return the index of the next free line after inserting the word wrapped text
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
	 * Registers a static line that cannot be changed later on.
	 * If you want dynamic text that can be changed, use {@link #registerDynamicEntry(int, Supplier)} instead.
	 * <br>
	 * Places the {@link ChatMenu} as multiple lines until it reaches either the end of the scoreboard or the end of the menu.
	 *
	 * @return the index of the next free line
	 */
	public int registerStaticEntry(int line, ChatMenu<?> menu) {
		return registerStaticEntry(line, menu, 14);
	}

	/**
	 * Registers a static line that cannot be changed later on.
	 * If you want dynamic text that can be changed, use {@link #registerDynamicEntry(int, Supplier)} instead.
	 * <br>
	 * Places the {@link ChatMenu} as multiple lines until it reaches either the given limit or the end of the menu.
	 *
	 * @param line the line to start placing the menu at
	 * @param menu the {@link ChatMenu} to place on the scoreboard
	 * @param limit the first line after the start line that is not filled with menu content
	 *
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

	/**
	 * Registers a dynamic entry that can be updated by calling {@link #updateLine(Player, int)}
	 *
	 * @param line  the line to place the entry on
	 * @param entry the supplier that will be called once the line is updated
	 */
	public void registerDynamicEntry(int line, Supplier<Component> entry) {
		this.dynamicEntries.put(line, new ScoreboardEntry(identifier + line, entry));
	}

	/**
	 * Sets the title of the scoreboard
	 *
	 * @param component the title component
	 */
	public void setTitle(Component component) {
		this.title = component;
		for (Objective objective : scoreboards.values()) {
			objective.displayName(component);
		}
	}

	/**
	 * Sets the title of this scoreboard for a specific player
	 *
	 * @param component the title component
	 * @param player    the player to set the scoreboard title for
	 */
	public void setTitle(Component component, Player player) {
		Objective objective = scoreboards.get(player);
		objective.displayName(component);
	}

	/**
	 * Play an animation on a scoreboard line.
	 *
	 * @param line        the line to play the animation on
	 * @param ticks       the time in ticks to wait before updating the animation
	 * @param lineUpdater the supplier that returns the component for the animation. To achieve a component that
	 *                    is changing over time, you may want to use the system millis or the current Bukkit tick.
	 * @return the Animation instance
	 */
	public Animation playAnimation(int line, int ticks, Supplier<Component> lineUpdater) {
		return playAnimation(line, -1, ticks, lineUpdater);
	}

	/**
	 * Play an animation on a scoreboard line.
	 *
	 * @param line        the line to play the animation on
	 * @param intervals   the amount of intervals to run this animation for. The animation will stop automatically after the given amount of intervals.
	 * @param ticks       the time in ticks to wait before updating the animation
	 * @param lineUpdater the supplier that returns the component for the animation. To achieve a component that
	 *                    is changing over time, you may want to use the system millis or the current Bukkit tick.
	 * @return the Animation instance
	 */
	public Animation playAnimation(int line, int intervals, int ticks, Supplier<Component> lineUpdater) {
		Animation animation = new Animation(line, intervals, ticks, lineUpdater);

		Collection<Animation> animations = this.animations.get(line);
		if (animations == null) {
			animations = new HashSet<>();
		}
		animations.add(animation);
		animation.play();

		return animation;
	}

	/**
	 * Stops and removes all current animations on the given lines.
	 *
	 * @param lines the lines to clear from any animations.
	 */
	public void stopAnimation(int... lines) {
		for (int line : lines) {
			Collection<Animation> animations = this.animations.get(line);
			if (animations != null) {
				animations.forEach(Animation::stop);
			}
			this.animations.remove(line);
		}
	}

	public class Animation {

		private final int line;
		private int intervals = -1;
		private final int ticks;
		private final Supplier<Component> lineUpdater;

		private BukkitTask task;

		public Animation(int line, int ticks, Supplier<Component> lineUpdater) {
			this.line = line;
			this.ticks = ticks;
			this.lineUpdater = lineUpdater;
		}

		public Animation(int line, int intervals, int ticks, Supplier<Component> lineUpdater) {
			this.line = line;
			this.intervals = intervals;
			this.ticks = ticks;
			this.lineUpdater = lineUpdater;
		}

		/**
		 * Starts the animation
		 */
		public void play() {
			AtomicInteger interval = new AtomicInteger(0);
			registerDynamicEntry(line, lineUpdater);
			task = Bukkit.getScheduler().runTaskTimer(GUIHandler.getInstance().getPlugin(), () -> {
				if (intervals == -1 || interval.get() < intervals) {
					try {
						updateLine(scoreboards.keySet(), line);
					} catch (Throwable t) {
						GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error occured while playing animation in scoreboard", t);
					}
					interval.addAndGet(1);
				} else {
					stop();
				}
			}, 0, ticks);
		}

		/**
		 * Stops the animation
		 */
		public void stop() {
			if (task != null && !task.isCancelled()) {
				task.cancel();
			}
		}

		/**
		 * Checks if the animation is running
		 * @return true if the animation is running
		 */
		public boolean isRunning() {
			return !task.isCancelled();
		}
	}
}
