package com.marco.simplecivilisations.sql;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class Civilisation {
    private final SimpleCivilisations plugin;
    private final MySQL SQL;
    private final Connection connection;
    private final UUID uuid;
    private String name;
    private String description;
    private UUID leader;
    private List<UUID> members;
    private boolean open;
    private List<Pillar> pillars;
    private int pillarsAvailable;
    private Location waypoint;

    public Civilisation(SimpleCivilisations plugin, UUID uuid, String name, String description, UUID leader, List<UUID> members, boolean open, List<Pillar> pillars, int pillarsAvailable, Location waypoint) {
        this.plugin = plugin;
        this.SQL = plugin.getSQL();
        this.connection = plugin.getSQL().getConnection();
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.leader = leader;
        this.members = members;
        this.open = open;
        this.pillars = pillars;
        this.pillarsAvailable = pillarsAvailable;
        this.waypoint = waypoint;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET name = ? WHERE uuid = ?");
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.name = name;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(User member) {
        try {
            if (members.contains(member.getUniqueId())) return;
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET civilisation = ?, role = ? WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.setInt(2, 0);
            ps.setString(3, member.toString());
            ps.executeUpdate();
            uninvite(member);
            members.add(member.getUniqueId());
            member.setCivilisation(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMember(User member) {
        try {
            if (!members.contains(member.getUniqueId()) || member.getUniqueId() == leader) return;
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET civilisation = ?, role = ? WHERE uuid = ?");
            ps.setString(1, null);
            ps.setInt(2, 0);
            ps.setString(3, member.toString());
            ps.executeUpdate();
            uninvite(member);
            members.remove(member.getUniqueId());
            member.setCivilisation(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            ps.setString(1, uuid.toString());
            ps.setString(2, user.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasInvited(User user) {
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

    public void setWaypoint(Location waypoint) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET waypoint=? WHERE uuid=?");
            ps.setString(1, waypoint != null ? SQL.serialiseLocation(waypoint) : null);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            this.waypoint = waypoint;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            PreparedStatement t = connection.prepareStatement("DELETE FROM pillars WHERE civilisation=?");
            t.setString(1, uuid.toString());
            t.executeUpdate();
            PreparedStatement i = connection.prepareStatement("DELETE FROM invites WHERE civilisation=?");
            i.setString(1, uuid.toString());
            i.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void messageOnlineMembers(String message) {
        getMembers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(SimpleCivilisations.color + message);
        });
    }

    public boolean hasMember(User user) {
        return hasMember(user.getUniqueId());
    }

    public boolean hasMember(UUID u) {
        return members.contains(u);
    }

    public int getPillarsAvailable() {
        return pillarsAvailable;
    }

    public void usePillar() {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET pillarsAvailable=? WHERE uuid=?");
            ps.setInt(1, pillarsAvailable - 1);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            pillarsAvailable--;
        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }

    public void gainPillar() {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE civilisations SET pillarsAvailable=? WHERE uuid=?");
            ps.setInt(1, pillarsAvailable + 1);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            pillarsAvailable ++;
        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }

    public void addPillar(Location location) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO pillars (civilisation, location, active) VALUES (?, ?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, MySQL.serialiseLocation(location));
            ps.setBoolean(3, true);
            ps.executeUpdate();
            pillars.add(new Pillar(
                    plugin,
                    uuid,
                    location,
                    null
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Pillar> getPillars() {
        return pillars;
    }
}
