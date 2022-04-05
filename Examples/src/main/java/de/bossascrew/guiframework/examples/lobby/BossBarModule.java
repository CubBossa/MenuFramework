package de.bossascrew.guiframework.examples.lobby;

import de.cubbossa.guiframework.Animations;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.bossbar.CustomBossBar;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BossBarModule implements Listener {

    private final CustomBossBar customBossBar = CustomBossBar.Builder.builder("lobby")
            .withText("Welcome to Example Empire!")
            .withSegments(BarStyle.SOLID)
            .withAnimationIntervals(300)
            .withAnimationTicks(1)
            .withColorAnimation(integer -> switch (integer / 100) {
                case 0 -> BarColor.RED;
                case 1 -> BarColor.GREEN;
                default -> BarColor.BLUE;
            })
            .withProgressAnimation(Animations.bounceProgress(150, 0, .9))
            .build();


    public BossBarModule() {
        Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
    }

    public void hideAll() {
        customBossBar.hideAll();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        customBossBar.show(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        customBossBar.hide(event.getPlayer());
    }
}
