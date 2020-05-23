package io.zentae.accounthandler.databasemanager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DbAcces {
    private final Logger log = Logger.getLogger("Minecraft");

    private final DbCredentials credentials;
    private HikariDataSource hikariDataSource;

    public DbAcces(DbCredentials credentials) {
        this.credentials = credentials;
    }

    private void setupHikariCP() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setJdbcUrl(credentials.toURI());
        hikariConfig.setUsername(credentials.getUser());
        hikariConfig.setPassword(credentials.getPass());
        hikariConfig.setMaxLifetime(60000L);
        hikariConfig.setIdleTimeout(30000L);
        hikariConfig.setLeakDetectionThreshold(300000L);
        hikariConfig.setConnectionTimeout(10000L);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void initPool() {
        setupHikariCP();
    }

    public void closePool() {
        this.hikariDataSource.close();
    }

    public Connection getConnection() throws SQLException {
        if(this.hikariDataSource == null) {
            log.warning("HikariCP not connected, seting up HikariCP.");
            setupHikariCP();
        }

        return this.hikariDataSource.getConnection();
    }

}
