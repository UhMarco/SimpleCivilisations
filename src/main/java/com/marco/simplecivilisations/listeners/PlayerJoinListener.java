package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final SimpleCivilisations plugin;

    public PlayerJoinListener(SimpleCivilisations plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listen(PlayerJoinEvent event) {
        plugin.getSQL().createUser(event.getPlayer());
    }
}
