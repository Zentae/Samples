package io.zentae.accounthandler.databasemanager;

public enum DbManager {
    PLAYFULL(new DbCredentials("127.0.0.1", "playfull", "*6n\"K5t7-kLv2kSE", "playfull", 5769));

    private final DbAcces dbAcces;

    DbManager(DbCredentials credentials) {
        this.dbAcces = new DbAcces(credentials);
    }

    public DbAcces getDbAcces() {
        return dbAcces;
    }

    public static void initAllDatabaseConnections() {
        for(DbManager dbManager : values()) dbManager.getDbAcces().initPool();
    }

    public static void closeAllDatabaseConnections() {
        for(DbManager dbManager : values()) dbManager.getDbAcces().closePool();
    }

}
