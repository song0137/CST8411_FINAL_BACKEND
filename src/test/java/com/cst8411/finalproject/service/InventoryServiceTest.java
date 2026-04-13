package com.cst8411.finalproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.Device;
import com.cst8411.finalproject.domain.Vehicle;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    @Test
    void shouldBindInStockDeviceToVehicle() throws Exception {
        Path dbFile = Files.createTempFile("inventory-service", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService service = new InventoryService(database);
        Customer customer = service.registerCustomer("Jordan Lee", "613-555-0101", "jordan@example.com");
        Vehicle vehicle = service.registerVehicle(customer.id(), "1FTFW1ET1EFA00001", "Ford", "F-150", 2021, "TRK777");
        Device device = service.registerDevice("Dash Cam", "CAM-09", "SER-200", "IN_STOCK");

        service.bindDeviceToVehicle(device.id(), vehicle.id());

        assertEquals("BOUND", service.getDevice(device.id()).status());
        assertEquals(1, service.lookupByVin(vehicle.vin()).devices().size());
    }

    @Test
    void shouldRejectBindingDeviceThatIsNotInStock() throws Exception {
        Path dbFile = Files.createTempFile("inventory-service-rule", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService service = new InventoryService(database);
        Customer customer = service.registerCustomer("Jordan Lee", "613-555-0101", "jordan@example.com");
        Vehicle vehicle = service.registerVehicle(customer.id(), "1FTFW1ET1EFA00002", "Ford", "F-150", 2021, "TRK778");
        Device device = service.registerDevice("Dash Cam", "CAM-09", "SER-201", "OUT_OF_STOCK");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.bindDeviceToVehicle(device.id(), vehicle.id()));

        assertEquals("Only in-stock devices can be bound to a vehicle.", exception.getMessage());
    }
}
