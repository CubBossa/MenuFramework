package de.cubbossa.guiframework.scoreboard;

import de.cubbossa.guiframework.chat.ChatMenu;
import de.cubbossa.guiframework.util.Animation;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
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

    public record ScoreboardEntry(String key, Supplier<ComponentLike> componentSupplier) {
    }

    @Getter
    private final String identifier;
    @Getter
    private ComponentLike title;
    private final int lines;

    private final Map<Player, Objective> scoreboards;
    private final Map<Integer, ComponentLike> staticEntries;
    private final Map<Integer, ScoreboardEntry> dynamicEntries;
    private final Map<Integer, Collection<Animation>> animations;

    /**
     * @param identifier a unique identifier for this scoreboard
     * @param title      the component title of this scoreboard
     * @param lines      the amount of lines for this scoreboard with a maximum of 15
     */
    public CustomScoreboard(String identifier, ComponentLike title, int lines) {
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
        Objective obj = scoreboard.registerNewObjective("GUI Framework", identifier, ChatUtils.toLegacy(title));
        scoreboards.put(player, obj);

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Static lines
        for (int i = 0; i < lines; i++) {
            int line = lines - i;
            String lineHex = Integer.toHexString(line);
            String scoreString = "§" + lineHex + ChatColor.WHITE;
            ComponentLike staticEntry = staticEntries.get(i);

            obj.getScore(scoreString).setScore(line);

            if (staticEntry == null) {
                continue;
            }
            Team team = scoreboard.getTeam(identifier + i);
            if (team == null) {
                team = scoreboard.registerNewTeam(identifier + i);
            }
            team.addEntry(scoreString);
            team.setPrefix(ChatUtils.toLegacy(staticEntry));
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
        ComponentLike toSet = dynamicEntry.componentSupplier.get();
        team.addEntry("§" + lineHex + ChatColor.WHITE + "");
        team.setPrefix(ChatUtils.toLegacy(toSet));
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
     * If you want dynamic text that can be changed, use {@link #setLine(int, Supplier)} instead.
     *
     * @param line  the line to insert this entry to
     * @param entry the component to display
     */
    public void setLine(int line, ComponentLike entry) {
        this.staticEntries.put(line, entry);
    }

    /**
     * Registers a static line that cannot be changed later on.
     * If you want dynamic text that can be changed, use {@link #setLine(int, Supplier)} instead.
     * <br>
     * Displays a text with automatic word wrapping in a given color.
     *
     * @param line       the line to insert this entry to
     * @param text       the text to display
     * @param lineLength the amount of characters that are allowed in one line
     * @param color      the color to start each line with
     * @return the index of the next free line after inserting the word wrapped text
     */
    public int setLine(int line, String text, int lineLength, TextColor color) {
        return setLine(line, text, lineLength, color, 14);
    }

    /**
     * Registers a static line that cannot be changed later on.
     * If you want dynamic text that can be changed, use {@link #setLine(int, Supplier)} instead.
     * <br>
     * Displays a text with automatic word wrapping in a given color. Once the given line limit was reached, it
     * cuts off the text and ends it with "..."
     *
     * @param line       the line to insert this entry to
     * @param text       the text to display
     * @param lineLength the amount of characters that are allowed in one line
     * @param color      the color to start each line with
     * @param limit      the line limit. Once the text is longer than the given limit, the text gets truncated
     * @return the index of the next free line after inserting the word wrapped text
     */
    public int setLine(int line, String text, int lineLength, TextColor color, int limit) {
        List<String> lines = List.of(ChatUtils.wordWrap(text, "\n", lineLength).split("\n"));
        for (int i = 0; i < lines.size(); i++) {
            String string = lines.get(i);
            if (i >= line + limit - 1) {
                setLine(line + i, Component.text(string.substring(0, string.length() - 3) + "...", color));
                return limit;
            }
            setLine(line + i, Component.text(string, color));
        }
        return line + lines.size();
    }

    /**
     * Registers a static line that cannot be changed later on.
     * If you want dynamic text that can be changed, use {@link #setLine(int, Supplier)} instead.
     * <br>
     * Places a list of components as multiple lines until it reaches either the end of the scoreboard or the end of the list.
     *
     * @return the index of the next free line
     */
    public int setLine(int line, List<ComponentLike> componentLikes) {
        return setLine(line, componentLikes, 14);
    }

    /**
     * Registers a static line that cannot be changed later on.
     * If you want dynamic text that can be changed, use {@link #setLine(int, Supplier)} instead.
     * <br>
     * Places the {@link ChatMenu} as multiple lines until it reaches either the given limit or the end of the menu.
     *
     * @param line           the line to start placing the menu at
     * @param componentLikes the component lines to place on the scoreboard
     * @param limit          the first line after the start line that is not filled with menu content
     * @return the index of the next free line
     */
    public int setLine(int line, List<ComponentLike> componentLikes, int limit) {
        int i = line;
        for (ComponentLike component : componentLikes) {
            if (i >= limit) {
                return limit;
            }
            setLine(i, component);
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
    public void setLine(int line, Supplier<ComponentLike> entry) {
        this.dynamicEntries.put(line, new ScoreboardEntry(identifier + line, entry));
    }

    /**
     * Sets the title of the scoreboard
     *
     * @param component the title component
     */
    public void setTitle(ComponentLike component) {
        this.title = component;
        for (Objective objective : scoreboards.values()) {
            objective.setDisplayName(ChatUtils.toLegacy(component));
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
        objective.setDisplayName(ChatUtils.toLegacy(component));
    }

    /**
     * Play an animation on a scoreboard line. Use {@link #setLine(int, Supplier)} first.
     *
     * @param line        the line to play the animation on
     * @param ticks       the time in ticks to wait before updating the animation
     * @return the Animation instance
     */
    public Animation playAnimation(int line, int ticks) {
        return playAnimation(line, -1, ticks);
    }

    /**
     * Play an animation on a scoreboard line. Use {@link #setLine(int, Supplier)} first.
     *
     * @param line        the line to play the animation on
     * @param intervals   the amount of intervals to run this animation for. The animation will stop automatically after the given amount of intervals.
     * @param ticks       the time in ticks to wait before updating the animation

     * @return the Animation instance
     */
    public Animation playAnimation(int line, int intervals, int ticks) {
        Animation animation = new Animation(new int[] {line}, intervals, ticks, integer -> Arrays.stream(integer).forEach(t -> updateLine(getViewers(), t)));

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
}
