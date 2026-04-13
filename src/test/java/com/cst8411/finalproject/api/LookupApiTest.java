package com.cst8411.finalproject.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.service.InventoryService;
import io.javalin.testtools.JavalinTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LookupApiTest {

    @Test
    void shouldLookupVehicleByVin() throws Exception {
        Path dbFile = Files.createTempFile("inventory-api", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService service = new InventoryService(database);
        long customerId = service.registerCustomer("Sam Chen", "613-555-0103", "sam@example.com").id();
        service.registerVehicle(customerId, "2G1FB1E31F9100001", "Chevrolet", "Camaro", 2018, "FAST01");

        JavalinTest.test(ApiFactory.create(database), (server, client) -> {
            var response = client.get("/api/lookup?vin=2G1FB1E31F9100001");

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("Sam Chen"));
        });
    }
}
