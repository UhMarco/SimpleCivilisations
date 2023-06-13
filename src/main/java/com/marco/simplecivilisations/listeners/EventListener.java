package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.event.Listener;

public abstract class EventListener implements Listener {
    protected final SimpleCivilisations plugin;

    public EventListener(SimpleCivilisations plugin) {
        this.plugin = plugin;
    }
}
