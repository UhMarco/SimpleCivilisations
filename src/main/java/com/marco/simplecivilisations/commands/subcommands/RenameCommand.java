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

public class RenameCommand extends SubCommand {
    public RenameCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("rename");
    }

    @Override
    public String getDescription() {
        return "Rename your civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv rename <name>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            User user = plugin.users.get(player.getUniqueId());
            Civilisation civilisation = plugin.civilisations.get(user.getCivilisationId());

            if (civilisation == null) {
                player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                return;
            } else if (!civilisation.getLeader().toString().equals(player.getUniqueId().toString())) {
                player.sendMessage(SimpleCivilisations.color + "Only the leader may rename the civilisation.");
                return;
            } else if (args.length == 0) {
                player.sendMessage(SimpleCivilisations.color + getUsage());
                return;
            } else if (args.length > 1) {
                player.sendMessage(SimpleCivilisations.color + "Civilisation names cannot contain spaces.");
                return;
            } else if (args[0].length() > 20) {
                player.sendMessage(SimpleCivilisations.color + "Civilisation names cannot exceed 20 characters.");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                civilisation.setName(args[0]);
                plugin.update(civilisation);
                player.sendMessage(SimpleCivilisations.color + "Civilisation name updated.");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
    }
}
