package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class EntityExplodeListener extends EventListener {
    public EntityExplodeListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(EntityExplodeEvent event) {
        if (List.of(EntityType.PRIMED_TNT, EntityType.MINECART_TNT).contains(event.getEntityType())) {
            for (Civilisation civilisation : plugin.civilisations.values()) {
                for (Pillar pillar : civilisation.getPillars()) {
                    if (SimpleCivilisations.inRangeOfPillar(event.getLocation(), pillar)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
