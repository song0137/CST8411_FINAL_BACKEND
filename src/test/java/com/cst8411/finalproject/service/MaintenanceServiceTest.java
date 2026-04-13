package com.cst8411.finalproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.Vehicle;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MaintenanceServiceTest {

    @Test
    void shouldRecordMaintenanceForCustomerVehicle() throws Exception {
        Path dbFile = Files.createTempFile("inventory-maintenance", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        MaintenanceService maintenanceService = new MaintenanceService(database);

        Customer customer = inventoryService.registerCustomer("Taylor Shaw", "613-555-0112", "taylor@example.com");
        Vehicle vehicle = inventoryService.registerVehicle(customer.id(), "1N4AL3AP6GC100001", "Nissan", "Altima", 2018, "ALT888");

        maintenanceService.recordMaintenance(customer.id(), vehicle.id(), "Brake Service", "Replaced front brake pads.", 249.99, "2026-04-03T11:00:00");

        assertEquals(1, maintenanceService.listRecords().size());
        assertEquals("Brake Service", maintenanceService.listRecords().get(0).serviceType());
        assertEquals(1, maintenanceService.listByCustomer(customer.id()).size());
    }
}
