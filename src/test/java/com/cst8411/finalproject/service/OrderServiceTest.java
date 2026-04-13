package com.cst8411.finalproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.Vehicle;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    @Test
    void shouldRecordPrepaidOutOfStockDeviceForCustomer() throws Exception {
        Path dbFile = Files.createTempFile("inventory-orders", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        OrderService orderService = new OrderService(database);

        Customer customer = inventoryService.registerCustomer("Taylor Brooks", "613-555-0102", "taylor@example.com");
        Vehicle vehicle = inventoryService.registerVehicle(customer.id(), "5NPE24AF5FH000001", "Hyundai", "Sonata", 2019, "SON999");

        long orderId = orderService.recordPrepaidOrder(customer.id(), vehicle.id(), "Remote Starter", "RST-44", 299.99);

        assertTrue(orderId > 0);
        assertEquals(1, orderService.listOrders().size());
        assertEquals("PURCHASED_NOT_IN_STOCK", orderService.listOrders().get(0).status());
    }
}
