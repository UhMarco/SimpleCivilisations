package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            User user = plugin.users.get(player.getUniqueId());
            Civilisation civilisation = plugin.civilisations.get(user.getCivilisationId());
            if (civilisation == null) {
                player.sendMessage(SimpleCivilisations.color + "You are not a member of a civilisation.");
                return;
            } else if (user.getRole() == 3) {
                player.sendMessage(SimpleCivilisations.color + "Leaders cannot leave their civilisations. You must promote another player or disband.");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                player.sendMessage(SimpleCivilisations.color + "You have left " + civilisation.getName() + ".");
                civilisation.removeMember(user);
                civilisation.messageOnlineMembers(player.getName() + " has left the civilisation.");
                plugin.update(user);
                plugin.update(civilisation);
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
    }
}
