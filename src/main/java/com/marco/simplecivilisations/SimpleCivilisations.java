package com.marco.simplecivilisations;

import com.marco.simplecivilisations.commands.CivilisationsCommand;
import com.marco.simplecivilisations.commands.OfflineTeleportCommand;
import com.marco.simplecivilisations.commands.ReviveCommand;
import com.marco.simplecivilisations.commands.SeenCommand;
import com.marco.simplecivilisations.listeners.PlayerRespawnEvent;
import com.marco.simplecivilisations.listeners.PlayerJoinListener;
import com.marco.simplecivilisations.listeners.PlayerKickListener;
import com.marco.simplecivilisations.listeners.PlayerQuitListener;
import com.marco.simplecivilisations.sql.MySQL;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

// Yes, this is the British spelling.
public final class SimpleCivilisations extends JavaPlugin {
    private MySQL SQL;

    public static final ChatColor color = ChatColor.GOLD;
    // I'll stick with color but only because it's forced on me.

    public List<Location> spawns = new ArrayList<>();


    @Override
    public void onEnable() {
        // Get these from config file at some point.
        this.SQL = new MySQL(
                this,
                "localhost",
                "3306",
                "test",
                "root",
                ""
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


        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerKickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnEvent(this), this);

        getLogger().info("Calculating spawns (faster on preloaded worlds)...");
        generateSpawns(20);
        getLogger().info("Spawns calculated.");
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
        // TODO: Get these from config
        World world = Bukkit.getWorld("world");
        assert world != null;
        int maxX = 4000;
        int minX = 0;
        int maxZ = 4000;
        int minZ = 0;
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
        //noinspection RedundantIfStatement
        if (List.of(Material.CACTUS, Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW).contains(block.getType())) return false;
        return true;
    }
}
