package com.cst8411.finalproject.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.service.AuthService;
import com.cst8411.finalproject.service.InventoryService;
import com.cst8411.finalproject.service.MaintenanceService;
import com.cst8411.finalproject.service.OrderService;
import io.javalin.testtools.JavalinTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CustomerPortalApiTest {

    @Test
    void shouldAllowCustomerToLoginAndViewOwnedRecords() throws Exception {
        Path dbFile = Files.createTempFile("inventory-customer-api", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        OrderService orderService = new OrderService(database);
        MaintenanceService maintenanceService = new MaintenanceService(database);
        AuthService authService = new AuthService(database);

        long customerId = inventoryService.registerCustomer("Riley Stone", "613-555-0113", "riley@example.com").id();
        long vehicleId = inventoryService.registerVehicle(customerId, "KM8J3CA46HU100001", "Hyundai", "Tucson", 2020, "RIL313").id();
        long deviceId = inventoryService.registerDevice("Dash Cam", "CAM-77", "SER-777", "IN_STOCK").id();
        inventoryService.bindDeviceToVehicle(deviceId, vehicleId);
        orderService.recordPrepaidOrder(customerId, vehicleId, "Remote Starter", "RST-101", 349.99);
        maintenanceService.recordMaintenance(customerId, vehicleId, "Annual Inspection", "Completed inspection and fluid checks.", 129.99, "2026-04-03T12:00:00");
        authService.createCustomerAccount(customerId, "riley.stone", "secret123");

        JavalinTest.test(ApiFactory.create(database), (server, client) -> {
            var loginResponse = client.post("/api/customer-login", """
                    {"username":"riley.stone","password":"secret123"}
                    """);

            assertEquals(200, loginResponse.code());
            String body = loginResponse.body().string();
            assertTrue(body.contains("token"));

            String token = body.split("\"token\":\"")[1].split("\"")[0];

            var portalResponse = client.get("/api/customer-portal", request -> request.header("Authorization", "Bearer " + token));

            assertEquals(200, portalResponse.code());
            String portalBody = portalResponse.body().string();
            assertTrue(portalBody.contains("Riley Stone"));
            assertTrue(portalBody.contains("Remote Starter"));
            assertTrue(portalBody.contains("Annual Inspection"));
        });
    }

    @Test
    void shouldAllowPortalLoginWithoutRealCredentialValidation() throws Exception {
        Path dbFile = Files.createTempFile("inventory-customer-api-open", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        OrderService orderService = new OrderService(database);
        MaintenanceService maintenanceService = new MaintenanceService(database);

        long customerId = inventoryService.registerCustomer("Riley Stone", "613-555-0113", "riley@example.com").id();
        long vehicleId = inventoryService.registerVehicle(customerId, "KM8J3CA46HU100002", "Hyundai", "Tucson", 2020, "RIL314").id();
        inventoryService.registerDevice("Dash Cam", "CAM-78", "SER-778", "IN_STOCK");
        orderService.recordPrepaidOrder(customerId, vehicleId, "Remote Starter", "RST-102", 349.99);
        maintenanceService.recordMaintenance(customerId, vehicleId, "Oil Change", "Changed oil and filter.", 89.99, "2026-04-03T12:00:00");

        JavalinTest.test(ApiFactory.create(database), (server, client) -> {
            var loginResponse = client.post("/api/customer-login", """
                    {"username":"abb","password":"123"}
                    """);

            assertEquals(200, loginResponse.code());
            String body = loginResponse.body().string();
            assertTrue(body.contains("token"));

            String token = body.split("\"token\":\"")[1].split("\"")[0];

            var portalResponse = client.get("/api/customer-portal", request -> request.header("Authorization", "Bearer " + token));

            assertEquals(200, portalResponse.code());
            String portalBody = portalResponse.body().string();
            assertTrue(portalBody.contains("Riley Stone"));
            assertTrue(portalBody.contains("Oil Change"));
        });
    }
}
