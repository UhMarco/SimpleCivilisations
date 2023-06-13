package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SeenCommand;
import com.marco.simplecivilisations.sql.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PlayerJoinListener extends EventListener {
    public PlayerJoinListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(PlayerJoinEvent event) {
        User user = plugin.getSQL().createUser(event.getPlayer());
        if (user.getLastDeath() == null) return;
        // TODO: get deathban duration from config.
        int minutes = 15;
        Duration duration = Duration.between(user.getLastDeath().toInstant(), Instant.now());
        if (duration.toMinutes() > minutes) {
            user.setLastDeath(null);
        } else {
            Timestamp timeWhenNotBanned = Timestamp.from(user.getLastDeath().toInstant().plusSeconds(TimeUnit.MINUTES.toSeconds(minutes)));
            event.getPlayer().kickPlayer("You are deathbanned for another " + SeenCommand.getTimestampDifference(Timestamp.from(Instant.now()), timeWhenNotBanned) + ".");
        }
    }
}
