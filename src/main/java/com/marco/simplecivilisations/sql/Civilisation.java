package com.marco.simplecivilisations.sql;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Civilisation {
    private final Connection connection;
    private final UUID uuid;
    private String name;
    private String description;
    private UUID leader;
    private ArrayList<UUID> members;
    private boolean open;
    private ArrayList<Location> territory;
    private Location waypoint;

    public Civilisation(SimpleCivilisations plugin, UUID uuid, String name, String description, UUID leader, ArrayList<UUID> members, boolean open, ArrayList<Location> territory, Location waypoint) {
        this.connection = plugin.getSQL().getConnection();
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.leader = leader;
        this.members = members;
        this.open = open;
        this.territory = territory;
        this.waypoint = waypoint;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET description = ? WHERE uuid = ?");
            ps.setString(1, description);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.description = description;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        try {
            // Remove current leader
            if (this.leader != null && this.leader != leader) {
                PreparedStatement ps = connection.prepareStatement("UPDATE users SET role = ? WHERE uuid = ?");
                ps.setInt(1, 2);
                ps.setString(2, this.leader.toString());
                ps.executeUpdate();
            }
            // Set new leader
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET leader = ? WHERE uuid = ?");
            ps.setString(1, leader.toString());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.leader = leader;
            PreparedStatement ps2 = connection.prepareStatement("UPDATE users SET civilisation = ?, role = ? WHERE uuid = ?");
            ps2.setString(1, uuid.toString());
            ps2.setInt(2, 3);
            ps2.setString(3, leader.toString());
            ps2.executeUpdate();
            if (!members.contains(leader)) members.add(leader);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UUID> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<UUID> members) {
        this.members = members;
    }

    public void addMember(UUID member) {
        try {
            if (members.contains(member)) return;
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET civilisation = ? WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.setString(2, member.toString());
            ps.executeUpdate();
            members.add(member);
        } catch (SQLException e) {
            // Really hope this doesn't happen...
            e.printStackTrace();
        }
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isOpen() {
        return open;
    }

    public void invite(User user) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO invites (civilisation, user) VALUES (?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, user.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void uninvite(User user) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM invites WHERE civilisation=? AND user=?");
            Bukkit.getLogger().info(uuid.toString());
            Bukkit.getLogger().info(user.getUniqueId().toString());
            ps.setString(1, uuid.toString());
            ps.setString(2, user.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isInvited(User user) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS count FROM invites WHERE civilisation=? AND user=?");
            ps.setString(1, uuid.toString());
            ps.setString(2, user.getUniqueId().toString());
            ResultSet result = ps.executeQuery();
            if (result.next() && result.getInt("count") != 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public Location getWaypoint() {
        return waypoint;
    }

    public void disband() {
        try {
            PreparedStatement c = connection.prepareStatement("DELETE FROM civilisations WHERE uuid=?");
            c.setString(1, uuid.toString());
            c.executeUpdate();
            PreparedStatement u = connection.prepareStatement("UPDATE users SET civilisation=?, role=? WHERE civilisation=?");
            u.setString(1, null);
            u.setInt(2, 0);
            u.setString(3, uuid.toString());
            u.executeUpdate();
            PreparedStatement t = connection.prepareStatement("DELETE FROM territories WHERE civilisation=?");
            t.setString(1, uuid.toString());
            t.executeUpdate();
            PreparedStatement i = connection.prepareStatement("DELETE FROM invites WHERE civilisation=?");
            i.setString(1, uuid.toString());
            i.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
