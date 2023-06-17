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

public class SetleaderCommand extends SubCommand {
    public SetleaderCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("setleader", "leader");
    }

    @Override
    public String getDescription() {
        return "Give leader to another member of the civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv setleader <player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            User user = plugin.users.get(player.getUniqueId());
            if (user == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong!");
                return;
            } else if (user.getCivilisationId() == null) {
                player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                return;
            } else if (user.getRole() < 3) {
                player.sendMessage(SimpleCivilisations.color + "Only the leader of your civilisation can run this command.");
                return;
            }

            UUID uuid = SimpleCivilisations.uuidFromName(args[0]);
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            String targetName = targetPlayer == null ? args[0] : targetPlayer.getName();
            if (uuid == null) {
                player.sendMessage(SimpleCivilisations.color + "Player not found.");
                return;
            } else if (targetPlayer == player) {
                player.sendMessage(SimpleCivilisations.color + "You are already the leader of this civilisation.");
                return;
            }

            Civilisation civilisation = plugin.civilisations.get(user.getCivilisationId());
            if (!civilisation.hasMember(uuid)) {
                player.sendMessage(SimpleCivilisations.color + targetName + " is not a member of your civilisation.");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                civilisation.setLeader(uuid);
                user.setRole(0);
                plugin.users.get(uuid).setRole(3);
                plugin.update(user);
                plugin.update(civilisation);
                civilisation.messageOnlineMembers(targetName + " has been promoted to the leader of the civilisation.");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
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
