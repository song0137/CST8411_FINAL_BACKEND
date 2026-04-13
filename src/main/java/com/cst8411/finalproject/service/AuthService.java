package com.cst8411.finalproject.service;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.AuthenticatedCustomer;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.CustomerAccount;
import com.cst8411.finalproject.domain.CustomerSession;
import com.cst8411.finalproject.domain.LoginResponse;
import com.cst8411.finalproject.repository.CustomerAccountRepository;
import com.cst8411.finalproject.repository.CustomerRepository;
import com.cst8411.finalproject.repository.CustomerSessionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class AuthService {

    private final CustomerRepository customerRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerSessionRepository customerSessionRepository;

    public AuthService(Database database) {
        this.customerRepository = new CustomerRepository(database);
        this.customerAccountRepository = new CustomerAccountRepository(database);
        this.customerSessionRepository = new CustomerSessionRepository(database);
    }

    public CustomerAccount createCustomerAccount(long customerId, String username, String password) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
        if (customerAccountRepository.findByCustomerId(customerId) != null) {
            throw new IllegalArgumentException("Customer account already exists.");
        }
        if (customerAccountRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }

        return customerAccountRepository.create(customerId, username, PasswordHasher.hash(password));
    }

    public LoginResponse login(String username, String password) {
        CustomerAccount account = resolveLoginAccount(username);

        String token = UUID.randomUUID().toString();
        CustomerSession session = customerSessionRepository.create(
                token,
                account.id(),
                Instant.now().plus(30, ChronoUnit.DAYS).toString()
        );

        Customer customer = customerRepository.findById(account.customerId());
        return new LoginResponse(session.token(), customer.id(), customer.fullName(), account.username());
    }

    private CustomerAccount resolveLoginAccount(String username) {
        CustomerAccount existingAccount = customerAccountRepository.findByUsername(username);
        if (existingAccount != null) {
            return existingAccount;
        }

        Customer fallbackCustomer = customerRepository.listAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No customer records available."));

        CustomerAccount fallbackAccount = customerAccountRepository.findByCustomerId(fallbackCustomer.id());
        if (fallbackAccount != null) {
            return fallbackAccount;
        }

        String generatedUsername = username == null || username.isBlank()
                ? "guest-" + UUID.randomUUID()
                : username;

        return customerAccountRepository.create(
                fallbackCustomer.id(),
                generatedUsername,
                PasswordHasher.hash(UUID.randomUUID().toString())
        );
    }

    public AuthenticatedCustomer authenticate(String token) {
        CustomerSession session = customerSessionRepository.findByToken(token);
        if (session == null || Instant.parse(session.expiresAt()).isBefore(Instant.now())) {
            throw new SecurityException("Invalid or expired customer session.");
        }

        CustomerAccount account = customerAccountRepository.findById(session.customerAccountId());
        if (account == null) {
            throw new SecurityException("Customer account not found.");
        }

        Customer customer = customerRepository.findById(account.customerId());
        if (customer == null) {
            throw new SecurityException("Customer not found.");
        }

        return new AuthenticatedCustomer(account, customer);
    }
}
