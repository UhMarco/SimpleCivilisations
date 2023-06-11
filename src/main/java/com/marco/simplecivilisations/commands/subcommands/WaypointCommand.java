package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WaypointCommand extends SubCommand {
    public WaypointCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("waypoint", "home", "hq", "capital");
    }

    @Override
    public String getDescription() {
        return "Find the waypoint of your civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv waypoint";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                User user = SQL.getUser(player.getUniqueId());
                Civilisation civilisation = SQL.getCivilisation(user);
                if (civilisation == null) {
                    player.sendMessage(SimpleCivilisations.color + "You are not a member of a civilisation.");
                    return;
                } else if (civilisation.getWaypoint() == null) {
                    player.sendMessage(SimpleCivilisations.color + "Your civilisation does not have a waypoint set.");
                    return;
                }

                Location wp = civilisation.getWaypoint();
                player.sendMessage(SimpleCivilisations.color + "Waypoint: " + ChatColor.GRAY + wp.getBlockX() + ", " + wp.getBlockY() + ", " + wp.getBlockZ() + SimpleCivilisations.color + ".");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
    }
}
