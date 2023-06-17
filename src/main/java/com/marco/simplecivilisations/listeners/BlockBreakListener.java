package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.commands.SeenCommand;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class BlockBreakListener extends EventListener {
    public BlockBreakListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        for (Civilisation civilisation : plugin.civilisations.values()) {
            for (Pillar pillar : civilisation.getPillars()) {
                if (SimpleCivilisations.inRangeOfPillar(location, pillar)) {
                    if (!civilisation.hasMember(event.getPlayer().getUniqueId()) && pillar.isActive()) {
                        if (event.getPlayer().hasPermission("simplecivilisations.bypassterritory") && event.getPlayer().getGameMode() == GameMode.CREATIVE && location.distance(pillar.getLocation()) >= 5) return;
                        if (location.equals(pillar.getLocation())) {
                            // They have broken the important block.
                            event.setCancelled(true);
                            event.getBlock().setType(Material.RED_CONCRETE);
                            pillar.destroy();
                            World world = event.getBlock().getWorld();
                            double x = event.getBlock().getLocation().getX() + 0.5;
                            double y = event.getBlock().getLocation().getY() + 0.5;
                            double z = event.getBlock().getLocation().getZ() + 0.5;
                            Particle particle = Particle.EXPLOSION_NORMAL;
                            world.spawnParticle(particle, x, y, z, 100);
                            event.getPlayer().sendMessage(SimpleCivilisations.color + "Territory disabled!");
                            Bukkit.getOnlinePlayers().forEach(p -> {
                                if (civilisation.hasMember(p.getUniqueId())) {
                                    p.sendTitle("§cPillar disabled!", "§7One of your pillars has been disabled.", 1, 40, 1);
                                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
                                }
                            });
                        } else {
                            event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot build in " + ChatColor.YELLOW + civilisation.getName() + SimpleCivilisations.color + "'s territory.");
                            event.setCancelled(true);
                        }
                    } else if (location.distance(pillar.getLocation()) < 5) {
                        if (civilisation.hasMember(event.getPlayer().getUniqueId()) && !pillar.isActive() && event.getBlock().getType() == Material.RED_CONCRETE && location.equals(pillar.getLocation())) {
                            if (Duration.between(pillar.getDestroyed().toInstant(), Instant.now()).toMinutes() >= 60) {
                                event.setCancelled(true);
                                civilisation.getPillars().remove(pillar);
                                pillar.remove();
                                civilisation.gainPillar();
                                event.getPlayer().sendMessage(SimpleCivilisations.color + "Pillar removed.");
                            } else {
                                Timestamp timeWhenNotBanned = Timestamp.from(pillar.getDestroyed().toInstant().plusSeconds(TimeUnit.MINUTES.toSeconds(60)));
                                String remaining = SeenCommand.getTimestampDifference(Timestamp.from(Instant.now()), timeWhenNotBanned);
                                event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot remove this pillar for another " + remaining + ".");
                                event.setCancelled(true);
                            }
                            return;
                        }
                        event.getPlayer().sendMessage(SimpleCivilisations.color + "You cannot build this close to a pillar");
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }
}