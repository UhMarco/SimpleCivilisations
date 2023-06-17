package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class PlayerMoveListener extends EventListener {
    public PlayerMoveListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;
        List<Civilisation> civilisations = plugin.civilisations.values().stream().toList();

        Pillar to = null, from = null;

        for (Civilisation civilisation : civilisations) {
            if (to != null && from != null) break;
            for (Pillar pillar : civilisation.getPillars()) {
                if (to != null && from != null) break;
                if (SimpleCivilisations.inRangeOfPillar(event.getTo(), pillar)) {
                    to = pillar;
                }
                if (SimpleCivilisations.inRangeOfPillar(event.getFrom(), pillar)) {
                    from = pillar;
                }
            }
        }

        if (to == null && from == null) {
            return;
        } else if (to != null && from != null) {
            if (to.getCivilisationId().toString().equals(from.getCivilisationId().toString())) {
                return;
            } else {
                Civilisation civilisation = plugin.civilisations.get(to.getCivilisationId());
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Entering §e" + civilisation.getName() + "§6's territory."));
            }
        } else if (to == null) {
            Civilisation civilisation = plugin.civilisations.get(from.getCivilisationId());
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Leaving §e" + civilisation.getName() + "§6's territory."));
        } else if (from == null) {
            Civilisation civilisation = plugin.civilisations.get(to.getCivilisationId());
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Entering §e" + civilisation.getName() + "§6's territory."));
        }
    }
}
