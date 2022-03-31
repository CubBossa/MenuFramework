package de.cubbossa.guiframework.test;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {

    /*
    Testcases
    1) Scoreboards
        - show
        - hide
        - stacked
        - animations
        - thread safety
    2) Chat Menus
        - pagination
        - itemstacks
        - string component mixed
    3) Top Inventory Menus
        - setting items and clickhandlers
        - default click handlers
        - pagination
        - presets
        -



     */






    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
