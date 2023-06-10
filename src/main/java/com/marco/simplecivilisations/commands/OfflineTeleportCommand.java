package com.marco.simplecivilisations.commands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.MySQL;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OfflineTeleportCommand implements TabExecutor {
    private final SimpleCivilisations plugin;
    private final MySQL SQL;

    public OfflineTeleportCommand(SimpleCivilisations plugin) {
        this.plugin = plugin;
        this.SQL = plugin.getSQL();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("simplecivilisations.offlinetp")) {
                player.sendMessage(ChatColor.RED + "Insufficient permissions.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.GRAY + "Usage: /seen <player>");
                return true;
            }

            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                sender.sendMessage(ChatColor.GRAY + p.getName() + " is online.");
                return true;
            }

            // We ball
            @SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!player.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.GRAY + "That player has never connected.");
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                User user = SQL.getUser(target.getUniqueId());
                if (user == null || user.getLastLocation() == null) sender.sendMessage(ChatColor.GRAY + "Something went wrong getting that data.");
                else {
                    Bukkit.getScheduler().runTask(plugin, () -> player.teleport(user.getLastLocation()));
                }
            });

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
