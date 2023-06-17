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

public class DisbandCommand extends SubCommand {
    public DisbandCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("disband");
    }

    @Override
    public String getDescription() {
        return "Disbands the civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv disband";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            User user = plugin.users.get(player.getUniqueId());
            if (user.getCivilisationId() == null) {
                player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                return;
            } else if (user.getRole() < 3) {
                player.sendMessage(SimpleCivilisations.color + "Only the leader of your civilisation can run this command.");
                return;
            }

            Civilisation civilisation = plugin.civilisations.get(user.getCivilisationId());
            civilisation.messageOnlineMembers(civilisation.getName() + " has been disbanded.");
            plugin.civilisations.remove(civilisation.getUniqueId());

            Bukkit.getScheduler().runTaskAsynchronously(plugin, civilisation::disband);
            return;
        }
        sender.sendMessage(ChatColor.RED + "Only player can run this command.");
    }
}
