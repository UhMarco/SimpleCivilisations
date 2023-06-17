package com.marco.simplecivilisations.sql;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class Pillar {
    private final SimpleCivilisations plugin;
    private final UUID civilisationId;
    private final Location location;
    private Timestamp destroyed;
    public Pillar(SimpleCivilisations plugin, UUID civilisationId, Location location, Timestamp destroyed) {
        this.plugin = plugin;
        this.civilisationId = civilisationId;
        this.location = location;
        this.destroyed = destroyed;
    }

    public UUID getCivilisationId() {
        return civilisationId;
    }

    public Location getLocation() {
        return location;
    }

    public Timestamp getDestroyed() {
        return destroyed;
    }

    public boolean isActive() {
        return destroyed == null;
    }

    public void destroy() {
        destroyed = Timestamp.from(Instant.now());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PreparedStatement ps = plugin.getSQL().getConnection().prepareStatement("UPDATE pillars SET destroyed=? WHERE civilisation=? AND location=?");
                ps.setTimestamp(1, destroyed);
                ps.setString(2, civilisationId.toString());
                ps.setString(3, MySQL.serialiseLocation(location));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void remove() {
        location.getBlock().setType(Material.AIR);
        World world = location.getWorld();
        assert world != null;
        world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX() + 1, location.getBlockY() - 1, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX() - 1, location.getBlockY() - 1, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ() + 1).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ() - 1).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 2, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 3, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX() + 1, location.getBlockY() + 3, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX() - 1, location.getBlockY() + 3, location.getBlockZ()).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 3, location.getBlockZ() + 1).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 3, location.getBlockZ() - 1).setType(Material.AIR);
        world.getBlockAt(location.getBlockX(), location.getBlockY() + 4, location.getBlockZ()).setType(Material.AIR);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           try {
               PreparedStatement ps = plugin.getSQL().getConnection().prepareStatement("DELETE FROM pillars WHERE civilisation=? AND location=?");
               ps.setString(1, civilisationId.toString());
               ps.setString(2, MySQL.serialiseLocation(location));
               ps.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
        });
    }
}
