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

    public User(SimpleCivilisations plugin, UUID uuid, UUID civilisation, int role, Location spawnLocation,Timestamp lastSession, Location lastLocation) {
        this.connection = plugin.getSQL().getConnection();
        this.uuid = uuid;
        this.civilisation = civilisation;
        this.role = role;
        this.spawnPoint = spawnLocation;
        this.lastSession = lastSession;
        this.lastLocation = lastLocation;
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
            ps.setString(1, civilisation.toString());
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

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Timestamp getLastSession() {
        return lastSession;
    }

    public void setLastSession(Timestamp lastSession) {
        this.lastSession = lastSession;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}
