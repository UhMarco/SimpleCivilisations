package com.marco.simplecivilisations.sql;

import com.marco.simplecivilisations.SimpleCivilisations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class MySQL {
    private final SimpleCivilisations plugin;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public MySQL(SimpleCivilisations plugin, String host, String port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (isConnected()) return;
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
    }

    public void disconnect() {
        if (!isConnected()) return;
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTables() {
        Bukkit.getLogger().info("[SimpleCivilisations] Completing setup...");
        try {
            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (uuid TEXT, civilisation TEXT, role INT, spawnPoint TEXT, lastSession TIMESTAMP, lastLocation TEXT, lives INT, lastDeath TIMESTAMP NULL, PRIMARY KEY (uuid(255)))");
            PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS civilisations (uuid TEXT, name TEXT, description TEXT, leader TEXT, open BOOLEAN, pillarsAvailable INT, waypoint TEXT, PRIMARY KEY (uuid(255)))");
            PreparedStatement ps3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS pillars (civilisation TEXT, location TEXT, destroyed TIMESTAMP NULL, PRIMARY KEY (civilisation(255), location(255)))");
            PreparedStatement ps4 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS invites (civilisation TEXT, user TEXT, PRIMARY KEY (civilisation(255), user(255)))");
            ps1.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();
            ps4.executeUpdate();
            Bukkit.getLogger().info("[SimpleCivilisations] Complete with no known issues.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean exists(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User createUser(Player player) {
        try {
            UUID uuid = player.getUniqueId();
            if (exists(uuid)) return getUser(player.getUniqueId());

            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (uuid, civilisation, role, spawnPoint, lastSession, lastLocation, lives, lastDeath) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, null);
            ps.setInt(3, 0);


            Location location = plugin.spawns.remove(0);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.generateSpawns(1));
            ps.setObject(4, serialiseLocation(location));

            Bukkit.getScheduler().runTask(plugin, () -> player.teleport(location));

            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setString(6, serialiseLocation(player.getLocation()));
            ps.setInt(7, 0);
            ps.setTimestamp(8, null);

            ps.executeUpdate();
            User user = new User(
                    plugin,
                    uuid,
                    null,
                    0,
                    location,
                    Timestamp.from(Instant.now()),
                    player.getLocation(),
                    0,
                    null
            );
            plugin.users.put(uuid, user);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSession(Player player) {
        // MASSIVE WARNING TO MYSELF
        // I'm not updating this in memory because I don't have to rn.
        // If I ever try to fetch this information from memory, and it is stale, I'm reserving the right to tell myself "I told you so".
        try {
            UUID uuid = player.getUniqueId();
            if (!exists(uuid)) {
                createUser(player);
            } else {
                PreparedStatement ps = connection.prepareStatement("UPDATE users SET lastSession = ?, lastLocation = ? WHERE UUID = ?");
                ps.setTimestamp(1, Timestamp.from(Instant.now()));
                ps.setString(2, serialiseLocation(player.getLocation()));
                ps.setString(3, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                try {
                    String potentialUuid = results.getString("civilisation");
                    return new User(
                            plugin,
                            uuid,
                            potentialUuid != null ? UUID.fromString(potentialUuid) : null,
                            results.getInt("role"),
                            deserialiseLocation(results.getString("spawnPoint")),
                            results.getTimestamp("lastSession"),
                            deserialiseLocation(results.getString("lastLocation")),
                            results.getInt("lives"),
                            results.getTimestamp("lastDeath")
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Civilisation createCivilisation(String name, Player player) {
        try {
            // Check if the name is taken.
            PreparedStatement nameCheck = connection.prepareStatement("SELECT * FROM civilisations where name = ?");
            nameCheck.setString(1, name);
            if (nameCheck.executeQuery().next()) {
                player.sendMessage(SimpleCivilisations.color + "That civilisation name is unavailable.");
                return null;
            }

            UUID uuid = randomCivilistionUUID();
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Something went wrong!");
                return null;
            }

            PreparedStatement ps = connection.prepareStatement("INSERT INTO civilisations (uuid, name, description, leader, open, pillarsAvailable, waypoint) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, "No description.");
            ps.setString(4, player.getUniqueId().toString());
            ps.setBoolean(5, false);
            ps.setInt(6, 5); // TODO: Determine how many pillars a civilisation should have access to.
            ps.setString(7, null);
            ps.executeUpdate();

            player.sendMessage(SimpleCivilisations.color + "Civilisation created.");
            Civilisation civilisation = new Civilisation(
                    this.plugin,
                    uuid,
                    name,
                    "No description.",
                    player.getUniqueId(),
                    new ArrayList<>(),
                    false,
                    new ArrayList<>(),
                    1,
                    null
            );
            civilisation.setLeader(player.getUniqueId());
            return civilisation;
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Something went wrong!");
        }
        return null;
    }

    private UUID randomCivilistionUUID() {
        while (true) {
            UUID uuid = UUID.randomUUID();
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS count FROM civilisations WHERE uuid=?");
                ps.setString(1, uuid.toString());
                ResultSet result = ps.executeQuery();
                if (result.next() && result.getInt("count") == 0) return uuid;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public Civilisation getCivilisationFromPlayerUUID(UUID playerUUID) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users where uuid = ?");
            ps.setString(1, playerUUID.toString());
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                String cUUID = results.getString("civilisation");
                if (cUUID != null) {
                    return getCivilisation(UUID.fromString(cUUID));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Civilisation getCivilisation(String name) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM civilisations WHERE name = ?");
            ps.setString(1, name);
            ResultSet results = ps.executeQuery();
            return getCivilisation(results);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Civilisation getCivilisation(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM civilisations WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            return getCivilisation(results);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Civilisation getCivilisation(ResultSet results) {
        // Not my proudest work.
        try {
            if (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                ArrayList<UUID> members = new ArrayList<>();
                PreparedStatement psm = connection.prepareStatement("SELECT * FROM users WHERE civilisation = ?");
                psm.setString(1, uuid.toString());
                ResultSet membersResult = psm.executeQuery();
                while (membersResult.next()) {
                    String mUUID = membersResult.getString("uuid");
                    members.add(UUID.fromString(mUUID));
                }

                ArrayList<Pillar> pillars = new ArrayList<>();
                PreparedStatement psp = connection.prepareStatement("SELECT * FROM pillars WHERE civilisation = ?");
                psp.setString(1, uuid.toString());
                ResultSet pillarsResult = psp.executeQuery();
                while (pillarsResult.next()) {
                    pillars.add(new Pillar(
                            plugin,
                            uuid,
                            deserialiseLocation(pillarsResult.getString("location")),
                            pillarsResult.getTimestamp("destroyed")
                    ));
                }

                String waypoint = results.getString("waypoint");

                try {
                    return new Civilisation(
                            plugin,
                            uuid,
                            results.getString("name"),
                            results.getString("description"),
                            UUID.fromString(results.getString("leader")),
                            members,
                            results.getBoolean("open"),
                            pillars,
                            results.getInt("pillarsAvailable"),
                            waypoint != null ? deserialiseLocation(waypoint) : null
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String serialiseLocation(Location location) {
        // I'm sure there are better methods of doing this, but I've done this now, so we ball.
        return Objects.requireNonNull(location.getWorld()).getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    public static Location deserialiseLocation(String locationString) {
        String[] parts = locationString.split(",");
        if (parts.length >= 6) {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return new Location(world, x, y, z, yaw, pitch);
            }
        }

        return null;
    }
}
