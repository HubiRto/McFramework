package pl.pomoku.mcframework.database;


import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.pomoku.mcframework.annotation.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(DatabaseConfig annotation, YamlConfiguration dbConfig, Logger logger) {
        try {
            Class.forName(annotation.type().getDriverClassName());
            connection = DriverManager.getConnection(
                    "jdbc:%s://%s:%d/%s?autoReconnect=%b&ssl=".formatted(
                            annotation.type().name().toLowerCase(),
                            dbConfig.getString(annotation.hostPath()),
                            dbConfig.getInt(annotation.portPath()),
                            dbConfig.getString(annotation.dbPath()),
                            annotation.useSSL()

                    ),
                    dbConfig.getString(annotation.userPath()),
                    dbConfig.getString(annotation.passwordPath())
            );
            logger.log(Level.INFO, "Database connection success");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database connection failed");
        }
    }
}
