package de.epiceric.shopchest.sql;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database {

    public SQLite(ShopChest plugin) {
        super(plugin);
    }

    @Override
    public Connection getConnection() {
        Path folder = plugin.getDataFolder().toPath();
        Path dbFile = folder.resolve("shops.db");

        if (Files.notExists(dbFile)) {
            try {
                Files.createFile(dbFile);
            } catch (IOException ex) {
                plugin.getLogger().severe("Failed to create database file");
                plugin.debug("Failed to create database file");
                plugin.debug(ex);
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);

            return connection;
        } catch (ClassNotFoundException | SQLException ex) {
            plugin.getLogger().severe("Failed to get database connection");
            plugin.debug("Failed to get database connection");
            plugin.debug(ex);
        }

        return null;
    }

    /**
     * Vacuums the database to reduce file size
     * @param async Whether the call should be executed asynchronously
     */
    public void vacuum(boolean async) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Statement s = null;

                try {
                    s = connection.createStatement();
                    s.executeUpdate("VACUUM");

                    plugin.debug("Vacuumed SQLite database");
                } catch (final SQLException ex) {
                    plugin.getLogger().severe("Failed to access database");
                    plugin.debug("Failed to vacuum database");
                    plugin.debug(ex);
                } finally {
                    close(s, null);
                }
            }
        };

        if (async) {
            runnable.runTaskAsynchronously(plugin);
        } else {
            runnable.run();
        }
    }
}
