package com.cst8411.finalproject.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    void shouldExposeExpectedDefaultSettings() {
        AppConfig config = AppConfig.defaultConfig();

        assertEquals(7070, config.port());
        assertEquals("data/car-service-inventory.db", config.databasePath());
    }
}
