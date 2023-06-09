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
        if (args.length == 0) {
            if (sender instanceof Player player) {
                Civilisation civilisation = plugin.civilisations.get(plugin.users.get(player.getUniqueId()).getCivilisationId());
                if (civilisation != null) sender.sendMessage(civilisationInfo(civilisation, sender));
                else sender.sendMessage(SimpleCivilisations.color + "You are not a member of a civilisation.");
            } else {
                sender.sendMessage(ChatColor.GRAY + "Console must provide full usage: " + getUsage());
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation") OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            Civilisation civilisation = player.hasPlayedBefore() ? SQL.getCivilisationFromPlayerUUID(player.getUniqueId()) : SQL.getCivilisation(args[0]);
            if (civilisation == null) {
                sender.sendMessage(SimpleCivilisations.color + "No civilisation found.");
                return;
            }
            sender.sendMessage(civilisationInfo(civilisation, sender));
        });
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }

    private String civilisationInfo(Civilisation civilisation, CommandSender sender) {
        // I am reassigning a local variable.
        int online = 0;
        ArrayList<String> members = new ArrayList<>();
        for (UUID member : civilisation.getMembers()) {
            if (Bukkit.getPlayer(member) != null) online += 1;
            if (!Objects.equals(member.toString(), civilisation.getLeader().toString())) members.add(formatPlayerName(member));
        }

        boolean showPillarsAvailable = sender instanceof Player player && civilisation.getLeader().equals(player.getUniqueId());
        boolean showWaypoint = sender instanceof Player player && civilisation.hasMember((player.getUniqueId()));

        return (
                ChatColor.GRAY + "-------------------------------\n"
                        + ChatColor.DARK_AQUA + civilisation.getName() + ChatColor.GRAY + " (" + online + "/" + civilisation.getMembers().size() + ")"
                        + "\n" + SimpleCivilisations.color + "Description: " + ChatColor.GRAY + civilisation.getDescription()
                        + (showPillarsAvailable ? "\n" + SimpleCivilisations.color + "Pillars available: " + ChatColor.GRAY + civilisation.getPillarsAvailable() : "")
                        + (showWaypoint ? "\n" + SimpleCivilisations.color + "Waypoint: " + ChatColor.GRAY + (civilisation.getWaypoint() != null ? civilisation.getWaypoint().getBlockX() + " " + civilisation.getWaypoint().getBlockZ() : "None") : "")
                        + "\n" + SimpleCivilisations.color +  "Leader: " + formatPlayerName(civilisation.getLeader())
                        + (members.size() > 0 ? "\n" + SimpleCivilisations.color + "Members: " + String.join(ChatColor.GRAY + ", ", members) : "")
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
