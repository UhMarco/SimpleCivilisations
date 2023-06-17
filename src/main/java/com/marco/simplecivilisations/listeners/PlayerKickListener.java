package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickListener extends EventListener {

    public PlayerKickListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(PlayerKickEvent event) {
        if (event.getReason().startsWith("You are deathbanned for another")) {
            event.setLeaveMessage("");
        } else {
            plugin.getSQL().updateSession(event.getPlayer());
        }
    }
}
