package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DescriptionCommand extends SubCommand {
    public DescriptionCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("description", "setdescription");
    }

    @Override
    public String getDescription() {
        return "Set the description of your civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv description <description>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Civilisation civilisation = SQL.getCivilisationFromPlayerUUID(player.getUniqueId());

                if (civilisation == null) {
                    player.sendMessage(SimpleCivilisations.color + "You must be in a civilisation to run this command.");
                    return;
                } else if (!civilisation.getLeader().toString().equals(player.getUniqueId().toString())) {
                    player.sendMessage(SimpleCivilisations.color + "Only the leader may set the civilisation description.");
                    return;
                } else if (args.length == 0) {
                    player.sendMessage(SimpleCivilisations.color + getUsage());
                    return;
                } else if (String.join(" ", args).length() > 100) {
                    player.sendMessage(SimpleCivilisations.color + "Civilisation descriptions cannot exceed 100 characters.");
                    return;
                }

                civilisation.setDescription(String.join(" ", args));
                player.sendMessage(SimpleCivilisations.color + "Civilisation description updated.");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
    }
}
