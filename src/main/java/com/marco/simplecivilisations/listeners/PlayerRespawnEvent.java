package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.sql.Timestamp;
import java.time.Instant;

public class PlayerRespawnEvent extends EventListener {
    public PlayerRespawnEvent(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void listen(org.bukkit.event.player.PlayerRespawnEvent event) {
        User user = plugin.users.get(event.getPlayer().getUniqueId());
        user.setLastDeath(Timestamp.from(Instant.now()));
        Location bed = event.getPlayer().getBedSpawnLocation();
        event.setRespawnLocation(bed != null ? bed : user.getSpawnPoint());
        Bukkit.getScheduler().runTask(plugin, () -> {
            event.getPlayer().kickPlayer("You died.");
        });
    }
}
