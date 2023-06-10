package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final SimpleCivilisations plugin;

    public PlayerQuitListener(SimpleCivilisations plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void listen(PlayerQuitEvent event) {
        plugin.getSQL().updateSession(event.getPlayer());
    }
}
