package com.cst8411.finalproject.config;

public record AppConfig(int port, String databasePath) {

    public static AppConfig defaultConfig() {
        return new AppConfig(7070, "data/car-service-inventory.db");
    }

    public static AppConfig fromEnvironment() {
        AppConfig defaults = defaultConfig();
        String portValue = System.getenv("APP_PORT");
        String databasePath = System.getenv("APP_DB_PATH");

        int port = portValue == null || portValue.isBlank()
                ? defaults.port()
                : Integer.parseInt(portValue);

        return new AppConfig(
                port,
                databasePath == null || databasePath.isBlank() ? defaults.databasePath() : databasePath
        );
    }
}
