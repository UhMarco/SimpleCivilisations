package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener extends EventListener {
    public BlockPlaceListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        for (Civilisation civilisation : plugin.civilisations.values()) {
            for (Pillar pillar : civilisation.getPillars()) {
                if (SimpleCivilisations.inRangeOfPillar(location, pillar)) {
                    if (!civilisation.hasMember(event.getPlayer().getUniqueId()) && pillar.isActive()) {
                        if (event.getPlayer().hasPermission("simplecivilisations.bypassterritory") && event.getPlayer().getGameMode() == GameMode.CREATIVE && location.distance(pillar.getLocation()) >= 5) return;
                        event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot build in " + ChatColor.YELLOW + civilisation.getName() + SimpleCivilisations.color + "'s territory.");
                        event.setCancelled(true);
                    } else if (location.distance(pillar.getLocation()) < 5) {
                        event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot build this close to a pillar");
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }
}
