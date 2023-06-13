package com.marco.simplecivilisations.commands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReviveCommand implements TabExecutor {
    private final SimpleCivilisations plugin;
    public ReviveCommand(SimpleCivilisations plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Missing argument.");
            return true;
        }

        UUID uuid = SimpleCivilisations.uuidFromName(args[0]);
        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "No player found.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = plugin.getSQL().getUser(uuid);
            if (user == null) {
                sender.sendMessage(ChatColor.RED + "No user found.");
                return;
            }
            user.setLastDeath(null);
            sender.sendMessage("Done.");
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
