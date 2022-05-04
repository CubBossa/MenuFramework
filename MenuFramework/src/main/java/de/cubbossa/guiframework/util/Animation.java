package de.cubbossa.guiframework.util;

import de.cubbossa.guiframework.GUIHandler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class Animation {

    private final int[] indices;
    private final int intervals;
    private final int ticks;
    private final Consumer<int[]> updateTask;

    private BukkitTask task;

    public Animation(int[] indices, int ticks, Consumer<int[]> updateTask) {
        this.indices = indices;
        this.intervals = -1;
        this.ticks = ticks;
        this.updateTask = updateTask;
    }

    public Animation(int[] indices, int intervals, int ticks, Consumer<int[]> updateTask) {
        this.indices = indices;
        this.intervals = intervals;
        this.ticks = ticks;
        this.updateTask = updateTask;
    }

    /**
     * Starts the animation
     */
    public void play() {
        AtomicInteger interval = new AtomicInteger(0);
        task = Bukkit.getScheduler().runTaskTimer(GUIHandler.getInstance().getPlugin(), () -> {
            if (intervals == -1 || interval.get() < intervals) {
                try {
                    updateTask.accept(indices);
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
     *
     * @return true if the animation is running
     */
    public boolean isRunning() {
        return !task.isCancelled();
    }
}
