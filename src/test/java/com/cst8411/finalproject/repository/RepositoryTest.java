package com.cst8411.finalproject.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.CustomerAccount;
import com.cst8411.finalproject.domain.Device;
import com.cst8411.finalproject.domain.MaintenanceRecord;
import com.cst8411.finalproject.domain.Vehicle;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RepositoryTest {

    @Test
    void shouldPersistCustomersVehiclesAndDevices() throws Exception {
        Path dbFile = Files.createTempFile("inventory-repository", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        CustomerRepository customerRepository = new CustomerRepository(database);
        VehicleRepository vehicleRepository = new VehicleRepository(database);
        DeviceRepository deviceRepository = new DeviceRepository(database);

        Customer customer = customerRepository.create("Alex Mason", "613-555-0100", "alex@example.com");
        Vehicle vehicle = vehicleRepository.create(customer.id(), "1HGCM82633A004352", "Honda", "Civic", 2020, "ABC123");
        Device device = deviceRepository.create("GPS Tracker", "GPS-01", "SER-100", "IN_STOCK");

        assertNotNull(customerRepository.findById(customer.id()));
        assertEquals("1HGCM82633A004352", vehicleRepository.findByVin(vehicle.vin()).vin());
        assertEquals("SER-100", deviceRepository.findById(device.id()).serialNumber());
    }

    @Test
    void shouldPersistCustomerAccountsAndMaintenanceRecords() throws Exception {
        Path dbFile = Files.createTempFile("inventory-repository-auth", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        CustomerRepository customerRepository = new CustomerRepository(database);
        VehicleRepository vehicleRepository = new VehicleRepository(database);
        CustomerAccountRepository customerAccountRepository = new CustomerAccountRepository(database);
        MaintenanceRecordRepository maintenanceRecordRepository = new MaintenanceRecordRepository(database);

        Customer customer = customerRepository.create("Morgan Hale", "613-555-0110", "morgan@example.com");
        Vehicle vehicle = vehicleRepository.create(customer.id(), "3FA6P0H72HR100001", "Ford", "Fusion", 2017, "MOR777");
        CustomerAccount account = customerAccountRepository.create(customer.id(), "morgan.hale", "hashed-password");
        MaintenanceRecord record = maintenanceRecordRepository.create(
                customer.id(),
                vehicle.id(),
                "Oil Change",
                "Changed oil and filter.",
                89.99,
                "2026-04-03T10:00:00"
        );

        assertEquals("morgan.hale", customerAccountRepository.findByUsername("morgan.hale").username());
        assertEquals("Oil Change", maintenanceRecordRepository.listByCustomerId(customer.id()).get(0).serviceType());
        assertEquals(vehicle.id(), record.vehicleId());
        assertEquals(account.customerId(), customer.id());
    }
}
