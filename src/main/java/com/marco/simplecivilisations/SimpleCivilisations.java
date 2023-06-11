package com.marco.simplecivilisations;

import com.marco.simplecivilisations.commands.CivilisationsCommand;
import com.marco.simplecivilisations.commands.OfflineTeleportCommand;
import com.marco.simplecivilisations.commands.SeenCommand;
import com.marco.simplecivilisations.listeners.PlayerJoinListener;
import com.marco.simplecivilisations.listeners.PlayerQuitListener;
import com.marco.simplecivilisations.sql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.UUID;

// Yes, this is the British spelling.
public final class SimpleCivilisations extends JavaPlugin {
    private MySQL SQL;

    public static ChatColor color = ChatColor.GOLD;
    // I'll stick with color but only because it's forced on me.

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
            getLogger().info("Required database not connected. This plugin will not work!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (SQL.isConnected()) getLogger().info("Database connected.");

        getCommand("civilisations").setExecutor(new CivilisationsCommand(this));
        getCommand("seen").setExecutor(new SeenCommand(this));
        getCommand("offlinetp").setExecutor(new OfflineTeleportCommand(this));


        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
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
}
