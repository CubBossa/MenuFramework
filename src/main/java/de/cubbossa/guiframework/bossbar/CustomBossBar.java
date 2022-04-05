package de.cubbossa.guiframework.bossbar;

import de.cubbossa.guiframework.GUIHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class CustomBossBar {

    private KeyedBossBar bossBar;

    private CustomBossBar(String key, String title, double progress, BarColor color, BarStyle style) {
        this.bossBar = Bukkit.createBossBar(new NamespacedKey(GUIHandler.getInstance().getPlugin(), key), title, color, style);
        this.bossBar.setProgress(progress);
        playAnimation();
    }

    public void show(Player player) {
        if (this.bossBar != null) {
            this.bossBar.addPlayer(player);
        }
    }

    public void hide(Player player) {
        if (this.bossBar != null) {
            this.bossBar.removePlayer(player);
        }
    }

    public void hideAll() {
        if(this.bossBar != null) {
            for(Player player : bossBar.getPlayers()) {
                hide(player);
            }
         }
    }

    protected KeyedBossBar getBossBar() {
        return bossBar;
    }

    protected abstract void playAnimation();

    public static class Builder {

        public static Builder builder(String key) {
            return new Builder(key);
        }

        private final String key;
        private int intervals = 100;
        private int ticks = 20;

        private double progress = .5;
        private BarColor color = BarColor.RED;
        private BarStyle style = BarStyle.SOLID;
        private String text = "Unnamed";

        private Function<Integer, BarColor> colorAnimation;
        private Function<Integer, BarStyle> segmentAnimation;
        private Function<Integer, Double> progressAnimation;
        private Function<Integer, String> textAnimation;

        private Builder(String key) {
            this.key = key;
        }

        public Builder withAnimationIntervals(int intervals) {
            this.intervals = intervals;
            return this;
        }

        public Builder withAnimationTicks(int ticks) {
            this.ticks = ticks;
            return this;
        }

        public Builder withProgress(double progress) {
            this.progress = progress;
            return this;
        }

        public Builder withColor(BarColor color) {
            this.color = color;
            return this;
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Builder withText(Component text) {
            this.text = text.toString(); //TODO
            return this;
        }

        public Builder withSegments(BarStyle segments) {
            this.style = segments;
            return this;
        }

        public Builder withColorAnimation(Function<Integer, BarColor> colorAnimation) {
            this.colorAnimation = colorAnimation;
            return this;
        }

        public Builder withProgressAnimation(Function<Integer, Double> progressAnimation) {
            this.progressAnimation = progressAnimation;
            return this;
        }

        public Builder withTextAnimation(Function<Integer, String> textAnimation) {
            this.textAnimation = textAnimation;
            return this;
        }

        public Builder withComponentAnimation(Function<Integer, Component> textAnimation) {
            this.textAnimation = integer -> textAnimation.apply(integer).toString(); //TODO parse component
            return this;
        }

        public Builder withSegmentAnimation(Function<Integer, BarStyle> segmentAnimation) {
            this.segmentAnimation = segmentAnimation;
            return this;
        }


        public CustomBossBar build() {
            return new CustomBossBar(key, text, progress, color, style) {
                @Override
                protected void playAnimation() {
                    AtomicInteger currentTick = new AtomicInteger(0);
                    Bukkit.getScheduler().runTaskTimerAsynchronously(GUIHandler.getInstance().getPlugin(), () -> {
                        int tick = currentTick.get();
                        if(textAnimation != null) {
                            getBossBar().setTitle(textAnimation.apply(tick));
                        }
                        if(progressAnimation != null) {
                            getBossBar().setProgress(progressAnimation.apply(tick));
                        }
                        if(colorAnimation != null) {
                            getBossBar().setColor(colorAnimation.apply(tick));
                        }
                        if(segmentAnimation != null) {
                            getBossBar().setStyle(segmentAnimation.apply(tick));
                        }
                        currentTick.addAndGet(1);
                        if(currentTick.get() >= intervals) {
                            currentTick.set(0);
                        }
                    }, 0, ticks);
                }
            };
        }
    }
}
