package com.cst8411.finalproject.service;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import com.cst8411.finalproject.domain.Device;
import com.cst8411.finalproject.domain.DeviceOrder;
import com.cst8411.finalproject.domain.LookupResponse;
import com.cst8411.finalproject.domain.Vehicle;
import com.cst8411.finalproject.domain.VehicleDeviceBinding;
import com.cst8411.finalproject.repository.CustomerRepository;
import com.cst8411.finalproject.repository.DeviceOrderRepository;
import com.cst8411.finalproject.repository.DeviceRepository;
import com.cst8411.finalproject.repository.VehicleDeviceRepository;
import com.cst8411.finalproject.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final DeviceRepository deviceRepository;
    private final VehicleDeviceRepository vehicleDeviceRepository;
    private final DeviceOrderRepository deviceOrderRepository;

    public InventoryService(Database database) {
        this.customerRepository = new CustomerRepository(database);
        this.vehicleRepository = new VehicleRepository(database);
        this.deviceRepository = new DeviceRepository(database);
        this.vehicleDeviceRepository = new VehicleDeviceRepository(database);
        this.deviceOrderRepository = new DeviceOrderRepository(database);
    }

    public Customer registerCustomer(String fullName, String phone, String email) {
        return customerRepository.create(fullName, phone, email);
    }

    public Vehicle registerVehicle(long customerId, String vin, String make, String model, int year, String licensePlate) {
        ensureCustomerExists(customerId);
        return vehicleRepository.create(customerId, vin, make, model, year, licensePlate);
    }

    public Device registerDevice(String deviceName, String sku, String serialNumber, String status) {
        return deviceRepository.create(deviceName, sku, serialNumber, status);
    }

    public void bindDeviceToVehicle(long deviceId, long vehicleId) {
        Device device = requireDevice(deviceId);
        if (!"IN_STOCK".equals(device.status())) {
            throw new IllegalStateException("Only in-stock devices can be bound to a vehicle.");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }

        vehicleDeviceRepository.bind(deviceId, vehicleId);
        deviceRepository.updateStatus(deviceId, "BOUND");
    }

    public Device getDevice(long deviceId) {
        return requireDevice(deviceId);
    }

    public List<Device> listInventory() {
        return deviceRepository.listAll();
    }

    public List<Customer> listCustomers() {
        return customerRepository.listAll();
    }

    public List<Vehicle> listVehicles() {
        return vehicleRepository.listAll();
    }

    public LookupResponse lookupByVin(String vin) {
        Vehicle vehicle = vehicleRepository.findByVin(vin);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }

        Customer customer = customerRepository.findById(vehicle.customerId());
        List<Device> devices = deviceRepository.listByVehicleId(vehicle.id());
        List<DeviceOrder> orders = deviceOrderRepository.listByVehicleId(vehicle.id());

        return new LookupResponse(customer, vehicle, null, devices, orders);
    }

    public LookupResponse lookupByDeviceId(long deviceId) {
        Device device = requireDevice(deviceId);
        VehicleDeviceBinding binding = vehicleDeviceRepository.findActiveByDeviceId(deviceId);
        if (binding == null) {
            return new LookupResponse(null, null, device, List.of(device), List.of());
        }

        Vehicle vehicle = vehicleRepository.findById(binding.vehicleId());
        Customer customer = customerRepository.findById(vehicle.customerId());
        List<DeviceOrder> orders = deviceOrderRepository.listByVehicleId(vehicle.id());

        return new LookupResponse(customer, vehicle, device, List.of(device), orders);
    }

    public List<LookupResponse> lookupByCustomerName(String nameQuery) {
        List<LookupResponse> results = new ArrayList<>();
        for (Customer customer : customerRepository.findByName(nameQuery)) {
            List<Vehicle> vehicles = vehicleRepository.listByCustomerId(customer.id());
            if (vehicles.isEmpty()) {
                results.add(new LookupResponse(customer, null, null, List.of(), deviceOrderRepository.listByCustomerId(customer.id())));
                continue;
            }

            for (Vehicle vehicle : vehicles) {
                results.add(new LookupResponse(
                        customer,
                        vehicle,
                        null,
                        deviceRepository.listByVehicleId(vehicle.id()),
                        deviceOrderRepository.listByVehicleId(vehicle.id())
                ));
            }
        }
        return results;
    }

    private void ensureCustomerExists(long customerId) {
        if (customerRepository.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
    }

    private Device requireDevice(long deviceId) {
        Device device = deviceRepository.findById(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found.");
        }
        return device;
    }
}
