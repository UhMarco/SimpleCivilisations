package com.marco.simplecivilisations.commands.subcommands;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SubCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RemovePillarCommand extends SubCommand {
    public RemovePillarCommand(SimpleCivilisations plugin) {
        super(plugin);
    }

    @Override
    public List<String> getLabels() {
        return List.of("removepillar");
    }

    @Override
    public String getDescription() {
        return "Remove the pillar you are looking at.";
    }

    @Override
    public String getUsage() {
        return "/cv removepillar";
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
            }

            Block block = player.getTargetBlockExact(5);
            if (block == null) {
                player.sendMessage(SimpleCivilisations.color + "The block you are looking at is too far away.");
                return;
            }

            boolean found = false;
            for (Pillar pillar : civilisation.getPillars()) {
                if (pillar.getLocation().equals(block.getLocation())) {
                    found = true;
                    if (!pillar.isActive()) {
                        player.sendMessage(SimpleCivilisations.color + "This pillar cannot be removed via a command.");
                    } else {
                        civilisation.getPillars().remove(pillar);
                        pillar.remove();
                        civilisation.gainPillar();
                        player.sendMessage(SimpleCivilisations.color + "Pillar removed.");
                    }
                    break;
                }
            }
            if (!found) {
                player.sendMessage(SimpleCivilisations.color + "No pillar found at the block you are looking at.");
            }

            return;
        }
        sender.sendMessage(ChatColor.RED + "Only players may run this command.");
    }
}
