package com.marco.simplecivilisations.commands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;

public class CivilisationsCommand implements TabExecutor {

    private final ArrayList<SubCommand> subcommands = new ArrayList<>();

    public CivilisationsCommand(SimpleCivilisations plugin) {
        subcommands.add(new InfoCommand(plugin));
        subcommands.add(new CreateCommand(plugin));
        subcommands.add(new DescriptionCommand(plugin));
        subcommands.add(new DisbandCommand(plugin));
        subcommands.add(new InviteCommand(plugin));
        subcommands.add(new UninviteCommand(plugin));
        subcommands.add(new JoinCommand(plugin));
        subcommands.add(new LeaveCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args.length == 1 && (args[0].equals("help") || args[0].equals("h"))) {
                ArrayList<String> commands = new ArrayList<>();
                subcommands.forEach(sb -> {
                    if (sb.showInHelp) commands.add(sb.getUsage());
                });
                sender.sendMessage(SimpleCivilisations.color + "---------- Civilisations ----------\n"
                        + "Commands:\n"
                        + String.join("\n", commands)
                        + "\n-------------------------------"
                );
              return true;
            }
            for (SubCommand sb : subcommands) {
                if (sb.getLabels().contains(args[0])) {
                    sb.perform(sender, removeFirstElement(args));
                    return true;
                } else if (args.length == 2 && (args[0].equals("help") || args[0].equals("h")) && sb.getLabels().contains(args[1])) {
                    sender.sendMessage(SimpleCivilisations.color + "Usage: " + ChatColor.GRAY + sb.getUsage() + SimpleCivilisations.color + "\nDescription: " + ChatColor.GRAY + sb.getDescription() + SimpleCivilisations.color + "\nAliases: " + ChatColor.GRAY + String.join(", ", sb.getLabels()));
                    return true;
                }
            }
        }

        // Info by default.
        subcommands.get(0).perform(sender, removeFirstElement(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 || (args.length == 2 && (args[0].equals("help") || args[0].equals("h")))) {
            List<String> labels = new ArrayList<>();
            subcommands.forEach(sb -> {
                if (sb.getLabels() != null) labels.addAll(sb.getLabels());
            });
            labels.add("help");
            return labels;
        } else if (args.length > 1) {
            for (SubCommand sb : subcommands) {
                List<String> labels = sb.getLabels();
                if (labels != null && labels.contains(args[0])) {
                    return sb.getTabCompletions(sender, removeFirstElement(args));
                }
            }
        }
        return Collections.emptyList();
    }

    private String[] removeFirstElement(String[] array) {
        return array == null || array.length <= 1 ? new String[0] : Arrays.copyOfRange(array, 1, array.length);
    }
}
