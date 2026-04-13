package com.cst8411.finalproject;

import com.cst8411.finalproject.api.ApiFactory;
import com.cst8411.finalproject.config.AppConfig;
import com.cst8411.finalproject.db.Database;
import io.javalin.Javalin;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Database database = new Database(config.databasePath());
        database.initialize();

        Javalin app = ApiFactory.create(database);
        app.start(config.port());
    }
}
