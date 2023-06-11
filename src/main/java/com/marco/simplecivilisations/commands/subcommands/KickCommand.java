package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class KickCommand extends SubCommand {
    public KickCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("kick");
    }

    @Override
    public String getDescription() {
        return "Kick a member from your civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv kick <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                User user = SQL.getUser(player.getUniqueId());
                if (user == null) {
                    player.sendMessage(ChatColor.RED + "Something went wrong!");
                    return;
                }

                Civilisation civilisation = SQL.getCivilisation(user);
                if (civilisation == null) {
                    player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                    return;
                } else if (user.getRole() < 2) {
                    player.sendMessage(SimpleCivilisations.color + "You need a higher seniority level within your civilisation to run this command.");
                    return;
                } else if (args.length != 1) {
                    player.sendMessage(SimpleCivilisations.color + "Usage: " + getUsage());
                    return;
                }

                UUID uuid = SimpleCivilisations.uuidFromName(args[0]);
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                String targetName = targetPlayer == null ? args[0] : targetPlayer.getName();
                if (uuid == null) {
                    player.sendMessage(SimpleCivilisations.color + "Player not found.");
                    return;
                } else if (targetPlayer == player) {
                    player.sendMessage(SimpleCivilisations.color + "No.");
                    return;
                }

                User target = SQL.getUser(uuid);
                if (!civilisation.hasMember(target)) {
                    player.sendMessage(SimpleCivilisations.color + targetName + " is not a member of the civilisation.");
                    return;
                }

                civilisation.removeMember(target);
                civilisation.messageOnlineMembers(targetName + " has been kicked from the civilisation.");
                if (targetPlayer != null) {
                    targetPlayer.sendMessage(SimpleCivilisations.color + "You were kicked from the civilisation.");
                }
            });
            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p != sender) players.add(p.getName());
            });
            return players;
        }
        return Collections.emptyList();
    }
}
