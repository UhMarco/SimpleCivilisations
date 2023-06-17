package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener extends EventListener {
    public PlayerInteractListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getType().toString().endsWith("DOOR") && !event.getClickedBlock().getType().toString().endsWith("GATE")) return;
        Location location = event.getClickedBlock().getLocation();

        for (Civilisation civilisation : plugin.civilisations.values()) {
            for (Pillar pillar : civilisation.getPillars()) {
                if (SimpleCivilisations.inRangeOfPillar(location, pillar)) {
                    if (!civilisation.hasMember(event.getPlayer().getUniqueId()) && pillar.isActive()) {
                        if (event.getPlayer().hasPermission("simplecivilisations.bypassterritory") && event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                        event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot do this in " + ChatColor.YELLOW + civilisation.getName() + SimpleCivilisations.color + "'s territory.");
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }
}
