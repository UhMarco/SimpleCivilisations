package com.marco.simplecivilisations.sql;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class User {
    private final Connection connection;
    private final UUID uuid;
    private UUID civilisation;
    private int role; // 0 - Member, 1 - Junior, 2 - Senior, 3 - Leader
    private Location spawnPoint;
    private Timestamp lastSession;
    private Location lastLocation;
    private int lives;
    private Timestamp lastDeath;

    public User(SimpleCivilisations plugin, UUID uuid, UUID civilisation, int role, Location spawnLocation,Timestamp lastSession, Location lastLocation, int lives, Timestamp lastDeath) {
        this.connection = plugin.getSQL().getConnection();
        this.uuid = uuid;
        this.civilisation = civilisation;
        this.role = role;
        this.spawnPoint = spawnLocation;
        this.lastSession = lastSession;
        this.lastLocation = lastLocation;
        this.lives = lives;
        this.lastDeath = lastDeath;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public UUID getCivilisationId() {
        return civilisation;
    }

    public void setCivilisationId(UUID civilisation) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET civilisation=? WHERE uuid=?");
            ps.setString(1, civilisation != null ? civilisation.toString() : null);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.civilisation = civilisation;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET role=? WHERE uuid=?");
            ps.setInt(1, role);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.role = role;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCivilisation(Civilisation civilisation) {
        setCivilisationId(civilisation != null ? civilisation.getUniqueId() : null);
        setRole(0);
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public Timestamp getLastSession() {
        return lastSession;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastDeath(Timestamp lastDeath) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET lastDeath=? WHERE uuid=?");
            ps.setTimestamp(1, lastDeath);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.lastDeath = lastDeath;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Timestamp getLastDeath() {
        return lastDeath;
    }

}
