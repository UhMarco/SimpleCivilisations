package com.marco.simplecivilisations.commands;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements TabExecutor {
    private final SimpleCivilisations plugin;
    public ReloadCommand(SimpleCivilisations plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        sender.sendMessage("SimpleCivilisations config reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
