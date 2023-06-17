package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SetwaypointCommand extends SubCommand {
    public SetwaypointCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("setwaypoint");
    }

    @Override
    public String getDescription() {
        return "Set the civilisation waypoint.";
    }

    @Override
    public String getUsage() {
        return "/cv setwaypoint [none]";
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
                player.sendMessage(SimpleCivilisations.color + "Only the leader may set the civilisation waypoint.");
                return;
            } else if (args.length > 1) {
                player.sendMessage(SimpleCivilisations.color + getUsage());
                return;
            }

            boolean clear = args.length == 1 && args[0].equalsIgnoreCase("none");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                civilisation.setWaypoint(clear ? null : player.getLocation());
                player.sendMessage(SimpleCivilisations.color + "Civilisation waypoint updated.");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("None");
        return Collections.emptyList();
    }
}
