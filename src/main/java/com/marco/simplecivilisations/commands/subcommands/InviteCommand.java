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

public class InviteCommand extends SubCommand {
    public InviteCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("invite", "inv");
    }

    @Override
    public String getDescription() {
        return "Invite a player to your civilisation";
    }

    @Override
    public String getUsage() {
    return "/cv invite <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                User user = SQL.getUser(player.getUniqueId());
                if (user == null) {
                    player.sendMessage(ChatColor.RED + "Something went wrong!");
                    return;
                } else if (user.getCivilisationId() == null) {
                    player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                    return;
                } else if (user.getRole() < 2) {
                    player.sendMessage(SimpleCivilisations.color + "You need a higher seniority level within your civilisation to run this command.");
                    return;
                } else if (args.length != 1) {
                    player.sendMessage(SimpleCivilisations.color + "Usage: " + getUsage());
                    return;
                }

                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    player.sendMessage(SimpleCivilisations.color + "Player not found.");
                    return;
                }

                User target = SQL.getUser(targetPlayer.getUniqueId());
                if (target.getCivilisationId() != null) {
                    player.sendMessage(SimpleCivilisations.color + targetPlayer.getName() + " is already a member of a civilisation.");
                    return;
                }

                Civilisation civilisation = SQL.getCivilisation(user);

                if (civilisation.isOpen()) {
                    player.sendMessage(SimpleCivilisations.color + "Your civilisation is open and anyone can join.");
                    return;
                } else if (civilisation.hasInvited(target)) {
                    player.sendMessage(SimpleCivilisations.color + targetPlayer.getName() + " has already been invited.");
                    return;
                }

                civilisation.invite(target);
                civilisation.messageOnlineMembers(targetPlayer.getName() + " has been invited.");
                targetPlayer.sendMessage(SimpleCivilisations.color + "You were invited to join " + civilisation.getName() + ".");
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
