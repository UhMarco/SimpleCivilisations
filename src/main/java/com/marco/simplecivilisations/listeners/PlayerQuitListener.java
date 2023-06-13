package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends EventListener {

    public PlayerQuitListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(PlayerQuitEvent event) {
        plugin.getSQL().updateSession(event.getPlayer());
    }
}
