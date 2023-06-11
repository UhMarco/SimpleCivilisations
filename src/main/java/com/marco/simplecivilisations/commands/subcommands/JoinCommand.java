package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinCommand extends SubCommand {
    public JoinCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("join");
    }

    @Override
    public String getDescription() {
        return "Join a civilisation.";
    }

    @Override
    public String getUsage() {
        return "/cv join <civilisation|player>";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(SimpleCivilisations.color + getUsage());
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                User user = SQL.getUser(player.getUniqueId());

                if (user.getCivilisationId() != null) {
                    player.sendMessage(SimpleCivilisations.color + "You are already a member of a civilisation.");
                    return;
                }

                @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                Civilisation civilisation = offlinePlayer.hasPlayedBefore() ? SQL.getCivilisationFromPlayerUUID(offlinePlayer.getUniqueId()) : SQL.getCivilisation(args[0]);
                if (civilisation == null) {
                    player.sendMessage(SimpleCivilisations.color + "No civilisation found.");
                    return;
                }

                if (!civilisation.isOpen() && !civilisation.hasInvited(user)) {
                    player.sendMessage(SimpleCivilisations.color + "You have not been invited to join this civilisation.");
                    return;
                }

                civilisation.messageOnlineMembers(player.getName() + " has joined the civilisation.");
                civilisation.addMember(user);
                player.sendMessage(SimpleCivilisations.color + "You have joined " + civilisation.getName() + ".");
            });

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players can run this command.");
    }
}
