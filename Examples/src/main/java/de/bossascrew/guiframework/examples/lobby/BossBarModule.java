package de.bossascrew.guiframework.examples.lobby;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.bossbar.CustomBossBar;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BossBarModule implements Listener {

    private final CustomBossBar customBossBar = CustomBossBar.Builder.builder("lobby")
            .withText("Welcome to Example Empire!")
            .withSegments(BarStyle.SOLID)
            .withAnimationIntervals(300)
            .withAnimationTicks(2)
            .withColorAnimation(integer -> switch (integer / 3) {
                case 0 -> BarColor.RED;
                case 1 -> BarColor.GREEN;
                default -> BarColor.BLUE;
            })
            .withProgressAnimation(integer -> Math.sin(Math.PI * 2 / 100 * integer) / 2 + .5)
            .build();


    public BossBarModule() {
        Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        customBossBar.show(event.getPlayer());
    }
}
