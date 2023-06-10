package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand extends SubCommand {
    public CreateCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("create");
    }

    @Override
    public String getDescription() {
        return "Create a new civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv create <name>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (plugin.getSQL().isInCivilisation(player)) {
                    player.sendMessage(SimpleCivilisations.color + "You are already in a civilisation.");
                    return;
                } else if (args.length == 0) {
                    player.sendMessage(SimpleCivilisations.color + "Usage: " + getUsage());
                    return;
                } else if (args.length > 1) {
                    player.sendMessage(SimpleCivilisations.color + "Civilisation names cannot contain spaces.");
                    return;
                } else if (args[0].length() > 20) {
                    player.sendMessage(SimpleCivilisations.color + "Civilisation names cannot exceed 20 characters.");
                    return;
                }
                SQL.createCivilisation(args[0], player);
            });
        } else {
            sender.sendMessage(ChatColor.RED + "Only players may run this command.");
        }
    }
}