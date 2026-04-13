package com.cst8411.finalproject.api;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.service.AuthService;
import com.cst8411.finalproject.service.CustomerPortalService;
import com.cst8411.finalproject.service.InventoryService;
import com.cst8411.finalproject.service.MaintenanceService;
import com.cst8411.finalproject.service.OrderService;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

public final class ApiFactory {

    private ApiFactory() {
    }

    public static Javalin create(Database database) {
        InventoryService inventoryService = new InventoryService(database);
        OrderService orderService = new OrderService(database);
        MaintenanceService maintenanceService = new MaintenanceService(database);
        AuthService authService = new AuthService(database);
        CustomerPortalService customerPortalService = new CustomerPortalService(database);

        Javalin app = Javalin.create();

        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        });

        app.options("/*", ctx -> ctx.status(HttpStatus.NO_CONTENT));

        app.exception(IllegalArgumentException.class, (exception, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(exception.getMessage()));
        });

        app.exception(IllegalStateException.class, (exception, ctx) -> {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(new ErrorResponse(exception.getMessage()));
        });

        app.exception(SecurityException.class, (exception, ctx) -> {
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(new ErrorResponse(exception.getMessage()));
        });

        app.get("/health", ctx -> ctx.json(new HealthResponse("ok")));
        app.get("/api/customers", ctx -> ctx.json(inventoryService.listCustomers()));
        app.get("/api/vehicles", ctx -> ctx.json(inventoryService.listVehicles()));
        app.get("/api/devices", ctx -> ctx.json(inventoryService.listInventory()));
        app.get("/api/inventory", ctx -> ctx.json(inventoryService.listInventory()));
        app.get("/api/orders", ctx -> ctx.json(orderService.listOrders()));
        app.get("/api/maintenance-records", ctx -> ctx.json(maintenanceService.listRecords()));

        app.post("/api/customers", ctx -> {
            CustomerRequest request = ctx.bodyAsClass(CustomerRequest.class);
            ctx.status(HttpStatus.CREATED);
            ctx.json(inventoryService.registerCustomer(request.fullName(), request.phone(), request.email()));
        });

        app.post("/api/vehicles", ctx -> {
            VehicleRequest request = ctx.bodyAsClass(VehicleRequest.class);
            ctx.status(HttpStatus.CREATED);
            ctx.json(inventoryService.registerVehicle(
                    request.customerId(),
                    request.vin(),
                    request.make(),
                    request.model(),
                    request.year(),
                    request.licensePlate()
            ));
        });

        app.post("/api/devices", ctx -> {
            DeviceRequest request = ctx.bodyAsClass(DeviceRequest.class);
            ctx.status(HttpStatus.CREATED);
            ctx.json(inventoryService.registerDevice(
                    request.deviceName(),
                    request.sku(),
                    request.serialNumber(),
                    request.status()
            ));
        });

        app.post("/api/customer-accounts", ctx -> {
            CustomerAccountRequest request = ctx.bodyAsClass(CustomerAccountRequest.class);
            ctx.status(HttpStatus.CREATED);
            ctx.json(authService.createCustomerAccount(
                    request.customerId(),
                    request.username(),
                    request.password()
            ));
        });

        app.post("/api/customer-login", ctx -> {
            CustomerLoginRequest request = ctx.bodyAsClass(CustomerLoginRequest.class);
            ctx.json(authService.login(request.username(), request.password()));
        });

        app.post("/api/vehicle-devices/bind", ctx -> {
            BindRequest request = ctx.bodyAsClass(BindRequest.class);
            inventoryService.bindDeviceToVehicle(request.deviceId(), request.vehicleId());
            ctx.json(new SuccessResponse("Device bound to vehicle."));
        });

        app.post("/api/orders/preorder", ctx -> {
            PrepaidOrderRequest request = ctx.bodyAsClass(PrepaidOrderRequest.class);
            long orderId = orderService.recordPrepaidOrder(
                    request.customerId(),
                    request.vehicleId(),
                    request.deviceName(),
                    request.sku(),
                    request.amountPaid()
            );
            ctx.status(HttpStatus.CREATED);
            ctx.json(new OrderCreatedResponse(orderId));
        });

        app.post("/api/maintenance-records", ctx -> {
            MaintenanceRequest request = ctx.bodyAsClass(MaintenanceRequest.class);
            ctx.status(HttpStatus.CREATED);
            ctx.json(maintenanceService.recordMaintenance(
                    request.customerId(),
                    request.vehicleId(),
                    request.serviceType(),
                    request.description(),
                    request.cost(),
                    request.servicedAt()
            ));
        });

        app.get("/api/customer-portal", ctx -> {
            String token = extractBearerToken(ctx.header("Authorization"));
            ctx.json(customerPortalService.getPortal(token));
        });

        app.get("/api/lookup", ctx -> {
            String vin = ctx.queryParam("vin");
            String deviceId = ctx.queryParam("deviceId");
            String customerName = ctx.queryParam("customerName");

            if (vin != null && !vin.isBlank()) {
                ctx.json(inventoryService.lookupByVin(vin));
                return;
            }

            if (deviceId != null && !deviceId.isBlank()) {
                ctx.json(inventoryService.lookupByDeviceId(Long.parseLong(deviceId)));
                return;
            }

            if (customerName != null && !customerName.isBlank()) {
                ctx.json(inventoryService.lookupByCustomerName(customerName));
                return;
            }

            throw new IllegalArgumentException("Provide vin, deviceId, or customerName.");
        });

        return app;
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Customer login is required.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private record HealthResponse(String status) {
    }

    private record ErrorResponse(String message) {
    }

    private record SuccessResponse(String message) {
    }

    private record OrderCreatedResponse(long orderId) {
    }

    private record CustomerRequest(String fullName, String phone, String email) {
    }

    private record CustomerAccountRequest(long customerId, String username, String password) {
    }

    private record CustomerLoginRequest(String username, String password) {
    }

    private record VehicleRequest(long customerId, String vin, String make, String model, int year, String licensePlate) {
    }

    private record DeviceRequest(String deviceName, String sku, String serialNumber, String status) {
    }

    private record BindRequest(long deviceId, long vehicleId) {
    }

    private record PrepaidOrderRequest(long customerId, Long vehicleId, String deviceName, String sku, double amountPaid) {
    }

    private record MaintenanceRequest(
            long customerId,
            long vehicleId,
            String serviceType,
            String description,
            double cost,
            String servicedAt
    ) {
    }
}
