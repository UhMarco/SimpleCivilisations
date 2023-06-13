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

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class SeenCommand implements TabExecutor {
    private final SimpleCivilisations plugin;
    private final MySQL SQL;

    public SeenCommand(SimpleCivilisations plugin) {
        this.plugin = plugin;
        this.SQL = plugin.getSQL();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // This command is unnecessary and isn't even that great, but it's a way of making sure the database is up-to-date.
        // I did not realise player.getLastPlayed() was a thing.
        if (!sender.hasPermission("simplecivilisations.seen")) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /seen <player>");
            return true;
        }

        Player p = Bukkit.getPlayer(args[0]);
        if (p != null) {
            sender.sendMessage(ChatColor.GRAY + p.getName() + " is currently online.");
            return true;
        }

        // We ball
        @SuppressWarnings("deprecation") OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.GRAY + "That player has never connected.");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = SQL.getUser(player.getUniqueId());
            if (user == null) sender.sendMessage(ChatColor.GRAY + "Something went wrong getting that data.");
            else sender.sendMessage(ChatColor.GRAY + args[0] + " has been offline for " + getTimestampDifference(user.getLastSession(), Timestamp.from(Instant.now())) + ".");
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    public static String getTimestampDifference(Timestamp timestamp1, Timestamp timestamp2) {
        Duration duration = Duration.between(timestamp1.toInstant(), timestamp2.toInstant());

        long years = duration.toDays() / 365;
        long months = (duration.toDays() % 365) / 30;
        long days = (duration.toDays() % 365) % 30;
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append(years > 1 ? " years " : " year ");
        }
        if (months > 0) {
            sb.append(months).append(months > 1 ? " months " : " month ");
        }
        if (days > 0) {
            sb.append(days).append(days > 1 ? " days " : " day ");
        }
        if (hours > 0) {
            sb.append(hours).append(hours > 1 ? " hours " : " hour ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(minutes > 1 ? " minutes " : " minute ");
        }
        if (seconds > 0) {
            sb.append(seconds).append(seconds > 1 ? " seconds " : " second ");
        }

        return sb.toString().trim();
    }
}
