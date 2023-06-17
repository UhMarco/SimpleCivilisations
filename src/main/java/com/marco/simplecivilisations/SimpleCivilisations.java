package com.marco.simplecivilisations;

import com.marco.simplecivilisations.commands.*;
import com.marco.simplecivilisations.listeners.*;
import com.marco.simplecivilisations.sql.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

// Yes, this is the British spelling.
public final class SimpleCivilisations extends JavaPlugin {
    private MySQL SQL;

    public static final ChatColor color = ChatColor.GOLD;
    // I'll stick with color but only because it's forced on me.

    public List<Location> spawns = new ArrayList<>();

    public Map<UUID, User> users = new HashMap<>();
    public Map<UUID, Civilisation> civilisations = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.SQL = new MySQL(
                this,
                getConfig().getString("host"),
                getConfig().getString("port"),
                getConfig().getString("database"),
                getConfig().getString("username"),
                getConfig().getString("password")
        );

        try {
            SQL.connect();
            SQL.createTables();
        } catch (ClassNotFoundException | SQLException e) {
            getLogger().severe("Required database not connected. This plugin will not work!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (SQL.isConnected()) getLogger().info("Database connected.");

        Objects.requireNonNull(getCommand("civilisations")).setExecutor(new CivilisationsCommand(this));
        Objects.requireNonNull(getCommand("revive")).setExecutor(new ReviveCommand(this));
        Objects.requireNonNull(getCommand("seen")).setExecutor(new SeenCommand(this));
        Objects.requireNonNull(getCommand("offlinetp")).setExecutor(new OfflineTeleportCommand(this));
        Objects.requireNonNull(getCommand("screload")).setExecutor(new ReloadCommand(this));


        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerKickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityExplodeListener(this), this);

        getLogger().info("Calculating spawns (faster on preloaded worlds)...");
        generateSpawns(20);
        getLogger().info("Spawns calculated.");

        getLogger().info("Fetching users...");
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM users");
            ResultSet results = ps.executeQuery();
            while (results.next()) {
                try {
                    String potentialUuid = results.getString("civilisation");
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    users.put(uuid, new User(
                            this,
                            uuid,
                            potentialUuid != null ? UUID.fromString(potentialUuid) : null,
                            results.getInt("role"),
                            MySQL.deserialiseLocation(results.getString("spawnPoint")),
                            results.getTimestamp("lastSession"),
                            MySQL.deserialiseLocation(results.getString("lastLocation")),
                            results.getInt("lives"),
                            results.getTimestamp("lastDeath")
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info(users.size() + " user(s) found.");

        getLogger().info("Fetching civilisations...");
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM civilisations");
            ResultSet results = ps.executeQuery();
            while (results.next()) {
                // Literally just copied this code from another class, didn't even read it properly.
                UUID uuid = UUID.fromString(results.getString("uuid"));
                ArrayList<UUID> members = new ArrayList<>();
                PreparedStatement psm = SQL.getConnection().prepareStatement("SELECT * FROM users WHERE civilisation = ?");
                psm.setString(1, uuid.toString());
                ResultSet membersResult = psm.executeQuery();
                while (membersResult.next()) {
                    String mUUID = membersResult.getString("uuid");
                    members.add(UUID.fromString(mUUID));
                }

                ArrayList<Pillar> pillars = new ArrayList<>();
                PreparedStatement psp = SQL.getConnection().prepareStatement("SELECT * FROM pillars WHERE civilisation = ?");
                psp.setString(1, uuid.toString());
                ResultSet pillarsResult = psp.executeQuery();
                while (pillarsResult.next()) {
                    pillars.add(new Pillar(
                            this,
                            uuid,
                            MySQL.deserialiseLocation(pillarsResult.getString("location")),
                            pillarsResult.getTimestamp("destroyed")
                    ));
                }

                String waypoint = results.getString("waypoint");

                try {
                    civilisations.put(uuid, new Civilisation(
                            this,
                            uuid,
                            results.getString("name"),
                            results.getString("description"),
                            UUID.fromString(results.getString("leader")),
                            members,
                            results.getBoolean("open"),
                            pillars,
                            results.getInt("pillarsAvailable"),
                            waypoint != null ? MySQL.deserialiseLocation(waypoint) : null
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // IT WORKED!
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info(civilisations.size() + " civilisation(s) found.");

        getLogger().info("Ready");
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> SQL.updateSession(p));
        SQL.disconnect();
    }

    public MySQL getSQL() {
        return SQL;
    }

    public static UUID uuidFromName(String name) {
        Player p = Bukkit.getPlayer(name);
        if (p != null) {
            return p.getUniqueId();
        }
        @SuppressWarnings("deprecation") OfflinePlayer o = Bukkit.getOfflinePlayer(name);
        if (o.hasPlayedBefore()) {
            return o.getUniqueId();
        }
        return null;
    }

    public void generateSpawns(int t) {
        Random r = new Random();
        World world = Bukkit.getWorld("world");
        assert world != null;
        int maxX = getConfig().getInt("max-x");
        int minX = getConfig().getInt("min-x");
        int maxZ = getConfig().getInt("max-z");
        int minZ = getConfig().getInt("min-z");
        for (int i = 0; i < t; i++) {
            int x = r.nextInt(minX, maxX);
            int z = r.nextInt(minZ, maxZ);
            Block block = world.getHighestBlockAt(x, z);
            Location l = new Location(world, x + 0.5, block.getY() + 1, z + 0.5);
            if (isValid(block)) spawns.add(l);
            else i -= 1;
        }
    }

    private boolean isValid(Block block) {
        if (block.isEmpty() || block.isLiquid()) return false;
        if (block.getType().toString().endsWith("LEAVES")) return false;
        if (List.of(Material.CACTUS, Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW, Material.SNOW).contains(block.getType())) return false;
        return true;
    }

    public static boolean inRangeOfPillar(Location location, Pillar pillar) {
        double x = location.getChunk().getX() * 16 + 7.5;
        double z = location.getChunk().getZ() * 16 + 7.5;
        double halfLength = (4 * 16 + 7.5); // This means 4 chunks in each direction.


        double cx = pillar.getLocation().getChunk().getX() * 16 + 7.5;
        double cz = pillar.getLocation().getChunk().getZ() * 16 + 7.5;

        double ax = cx - halfLength;
        double az = cz - halfLength;
        double bx = cx + halfLength;
        double bz = cz + halfLength;

        return x > ax && x < bx && z > az && z < bz;
    }

    public void update(User user) {
        users.put(user.getUniqueId(), user);
    }

    public void update(Civilisation civilisation) {
        civilisations.put(civilisation.getUniqueId(), civilisation);
    }
}
