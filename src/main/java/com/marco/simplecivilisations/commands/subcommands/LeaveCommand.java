package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaveCommand extends SubCommand {
    public LeaveCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("leave");
    }

    @Override
    public String getDescription() {
        return "Leave your civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv leave";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Get user and civilisation
                User user = SQL.getUser(player.getUniqueId());
                Civilisation civilisation = SQL.getCivilisation(user);
                // Is there any reason they can't leave?
                if (civilisation == null) {
                    player.sendMessage(SimpleCivilisations.color + "You are not a member of a civilisation.");
                    return;
                } else if (user.getRole() == 3) {
                    player.sendMessage(SimpleCivilisations.color + "Leaders cannot leave their civilisations. You must promote another player or disband.");
                    return;
                }

                // Leave
                civilisation.messageOnlineMembers(player.getName() + " has left the civilisation.");
                civilisation.removeMember(player.getUniqueId());
                player.sendMessage(SimpleCivilisations.color + "You have left " + civilisation.getName() + ".");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
    }
}
