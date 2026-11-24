package com.logistream.service;

import com.logistream.util.LogiStreamUtil;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Driver;
import com.logistream.model.DriverStatus;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;

public class DriverService {
    private final MemoryRepository<Driver> repo;
    private final VehicleService vehicles;
    private final AuditLogger audit;

    public DriverService(MemoryRepository<Driver> repo, VehicleService vehicles, AuditLogger audit) {
        this.repo = repo;
        this.vehicles = vehicles;
        this.audit = audit;
    }

    public synchronized Driver add(String name, String phone, String zone) {
        LogiStreamUtil.requireNonBlank(name, "Name");
        LogiStreamUtil.requirePhone(phone);
        LogiStreamUtil.requireNonBlank(zone, "Zone");
        String id = MemoryRepository.nextId("DRV", repo.allIds());
        Driver d = new Driver(id, name, phone, zone);
        repo.save(d);
        audit.log("ADD driver " + id + " " + name);
        return d;
    }

    public Driver get(String id) {
        Driver d = repo.get(id);
        if (d == null) throw new LogiStreamException("Driver not found: " + id);
        return d;
    }

    public List<Driver> listAll() {
        return repo.listAll();
    }

    public List<Driver> available() {
        List<Driver> out = new ArrayList<Driver>();
        for (Driver d : repo.listAll()) {
            if (d.getStatus() == DriverStatus.AVAILABLE) out.add(d);
        }
        return out;
    }

    public List<Driver> search(String keyword) {
        String k = keyword.toLowerCase();
        List<Driver> out = new ArrayList<Driver>();
        for (Driver d : repo.listAll()) {
            if (d.getId().toLowerCase().contains(k) || d.getName().toLowerCase().contains(k)
                    || d.getZone().toLowerCase().contains(k)) out.add(d);
        }
        return out;
    }

    public synchronized Driver update(String id, String name, String phone, String zone) {
        Driver d = get(id);
        if (!name.isEmpty()) d.setName(name);
        if (!phone.isEmpty()) {
            LogiStreamUtil.requirePhone(phone);
            d.setPhone(phone);
        }
        if (!zone.isEmpty()) d.setZone(zone);
        repo.save(d);
        audit.log("UPDATE driver " + id);
        return d;
    }

    public synchronized void delete(String id) {
        Driver d = get(id);
        if (d.getStatus() == DriverStatus.ON_DELIVERY)
            throw new LogiStreamException("Driver is on delivery and cannot be deleted.");
        repo.delete(id);
        audit.log("DELETE driver " + id);
    }

    public synchronized void assignVehicle(String driverId, String vehicleId) {
        Driver d = get(driverId);
        vehicles.get(vehicleId); // throws if the vehicle does not exist
        for (Driver other : repo.listAll()) {
            if (other.getVehicleId().equals(vehicleId) && !other.getId().equals(driverId))
                throw new LogiStreamException("Vehicle " + vehicleId + " is already assigned to " + other.getId() + ".");
        }
        d.setVehicleId(vehicleId);
        repo.save(d);
        audit.log("ASSIGN vehicle " + vehicleId + " to driver " + driverId);
    }

    /** Deleting a vehicle is allowed only when no driver uses it. */
    public synchronized void deleteVehicle(String vehicleId) {
        for (Driver d : repo.listAll()) {
            if (d.getVehicleId().equals(vehicleId))
                throw new LogiStreamException("Vehicle " + vehicleId + " is assigned to " + d.getId() + ".");
        }
        vehicles.delete(vehicleId);
    }

    /** Weight the driver's vehicle can carry (0 when the driver has no vehicle). */
    public double capacityOf(Driver d) {
        if (d.getVehicleId().equals("-") || !vehicles.exists(d.getVehicleId())) return 0;
        return vehicles.get(d.getVehicleId()).getCapacityKg();
    }

    public synchronized void setStatus(String id, DriverStatus status) {
        Driver d = get(id);
        d.setStatus(status);
        repo.save(d);
    }

    public synchronized void recordDelivery(String id) {
        Driver d = get(id);
        d.incrementDelivered();
        repo.save(d);
    }
}
