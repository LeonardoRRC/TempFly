package me.theboykiss.ovh.tempfly;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private boolean sqlEnabled;
    private HikariDataSource dataSource;

    public DatabaseManager(File sqlFile) {
        YamlConfiguration sqlConfig = YamlConfiguration.loadConfiguration(sqlFile);
        sqlEnabled = sqlConfig.getBoolean("sql.enable");

        if (sqlEnabled) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + sqlConfig.getString("sql.host") + "/" + sqlConfig.getString("sql.database"));
            config.setUsername(sqlConfig.getString("sql.user"));
            config.setPassword(sqlConfig.getString("sql.password"));
            dataSource = new HikariDataSource(config);
        }
    }

    public boolean isSQLEnabled() {
        return sqlEnabled;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS fly_time (uuid VARCHAR(36), time INT, PRIMARY KEY (uuid))";
        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
