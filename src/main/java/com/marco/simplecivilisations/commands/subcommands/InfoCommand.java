package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class InfoCommand extends SubCommand {
    public InfoCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("info", "i", "who");
    }

    @Override
    public String getDescription() {
        return "Get information about a civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv info [civilisation|player]";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    Civilisation civilisation = SQL.getCivilisationFromPlayerUUID(player.getUniqueId());
                    if (civilisation != null) sender.sendMessage(civilisationInfo(civilisation));
                    else sender.sendMessage(SimpleCivilisations.color + "You are not a member of a civilisation.");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Console must provide full usage: " + getUsage());
                }
                return;
            }

            @SuppressWarnings("deprecation") OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            Civilisation civilisation = player.hasPlayedBefore() ? SQL.getCivilisationFromPlayerUUID(player.getUniqueId()) : SQL.getCivilisation(args[0]);
            if (civilisation == null) {
                sender.sendMessage(SimpleCivilisations.color + "No civilisation found.");
                return;
            }

            sender.sendMessage(civilisationInfo(civilisation));
        });
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }

    private String civilisationInfo(Civilisation civilisation) {
        int online = 0;
        ArrayList<String> members = new ArrayList<>();
        for (UUID member : civilisation.getMembers()) {
            if (Bukkit.getPlayer(member) != null) online += 1;
            if (!Objects.equals(member.toString(), civilisation.getLeader().toString())) members.add(formatPlayerName(member));
        }

        return (
                ChatColor.GRAY + "-------------------------------\n"
                        + ChatColor.DARK_AQUA + civilisation.getName() + ChatColor.GRAY + " (" + online + "/" + civilisation.getMembers().size() + ")" + (civilisation.getWaypoint() != null ? " Waypoint: " + ChatColor.DARK_RED + civilisation.getWaypoint().getBlockX() + " " + civilisation.getWaypoint().getBlockZ() : "")
                        + "\n" + SimpleCivilisations.color + "Description: " + ChatColor.GRAY + civilisation.getDescription()
                        + "\n" + SimpleCivilisations.color +  "Leader: " + formatPlayerName(civilisation.getLeader())
                        + (members.size() > 0 ? "\n" + SimpleCivilisations.color + "Members: " + String.join(", ", members) : "")
                        // TODO: Click here to join if invite or open.
                        + ChatColor.GRAY + "\n-------------------------------"
        );
    }

    private String formatPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return ChatColor.GREEN + player.getName();
        return ChatColor.DARK_GRAY + Bukkit.getOfflinePlayer(uuid).getName();
    }
}
