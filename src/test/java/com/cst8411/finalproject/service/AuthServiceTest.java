package com.cst8411.finalproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.CustomerAccount;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void shouldCreateCustomerAccountAndLogin() throws Exception {
        Path dbFile = Files.createTempFile("inventory-auth", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        AuthService authService = new AuthService(database);

        Customer customer = inventoryService.registerCustomer("Jamie Fox", "613-555-0111", "jamie@example.com");
        CustomerAccount account = authService.createCustomerAccount(customer.id(), "jamie.fox", "secret123");
        String token = authService.login("jamie.fox", "secret123").token();

        assertEquals(customer.id(), account.customerId());
        assertNotNull(token);
        assertEquals(customer.id(), authService.authenticate(token).customer().id());
    }

    @Test
    void shouldAllowLoginWithAnyPasswordForExistingUsername() throws Exception {
        Path dbFile = Files.createTempFile("inventory-auth-fail", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        AuthService authService = new AuthService(database);

        Customer customer = inventoryService.registerCustomer("Jamie Fox", "613-555-0111", "jamie@example.com");
        authService.createCustomerAccount(customer.id(), "jamie.fox", "secret123");

        String token = authService.login("jamie.fox", "wrong-password").token();

        assertNotNull(token);
        assertEquals(customer.id(), authService.authenticate(token).customer().id());
    }

    @Test
    void shouldAllowLoginForUnknownUsernameByUsingFirstCustomer() throws Exception {
        Path dbFile = Files.createTempFile("inventory-auth-open", ".db");
        Database database = new Database(dbFile.toString());
        database.initialize();

        InventoryService inventoryService = new InventoryService(database);
        AuthService authService = new AuthService(database);

        Customer customer = inventoryService.registerCustomer("Riley Stone", "613-555-0113", "riley@example.com");

        String token = authService.login("abb", "anything").token();

        assertNotNull(token);
        assertEquals(customer.id(), authService.authenticate(token).customer().id());
    }
}
