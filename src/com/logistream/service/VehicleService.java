package com.logistream.service;

import com.logistream.util.LogiStreamUtil;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Bike;
import com.logistream.model.Van;
import com.logistream.model.Vehicle;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;

public class VehicleService {
    private final MemoryRepository<Vehicle> repo;
    private final AuditLogger audit;

    public VehicleService(MemoryRepository<Vehicle> repo, AuditLogger audit) {
        this.repo = repo;
        this.audit = audit;
    }

    /** Overloaded: uses the default capacity of the vehicle type. */
    public Vehicle add(String type, String regNo) {
        double cap = type.equalsIgnoreCase("BIKE") ? Bike.DEFAULT_CAPACITY : Van.DEFAULT_CAPACITY;
        return add(type, regNo, cap);
    }

    public synchronized Vehicle add(String type, String regNo, double capacityKg) {
        LogiStreamUtil.requireNonBlank(regNo, "Registration number");
        LogiStreamUtil.requirePositive(capacityKg, "Capacity");
        for (Vehicle existing : repo.listAll()) {
            if (existing.getRegNo().equalsIgnoreCase(regNo))
                throw new LogiStreamException("A vehicle with registration " + regNo + " already exists.");
        }
        String id = MemoryRepository.nextId("VEH", repo.allIds());
        Vehicle v;
        if (type.equalsIgnoreCase("BIKE")) v = new Bike(id, regNo, capacityKg);
        else if (type.equalsIgnoreCase("VAN")) v = new Van(id, regNo, capacityKg);
        else throw new LogiStreamException("Vehicle type must be BIKE or VAN.");
        repo.save(v);
        audit.log("ADD vehicle " + id + " " + regNo);
        return v;
    }

    public Vehicle get(String id) {
        Vehicle v = repo.get(id);
        if (v == null) throw new LogiStreamException("Vehicle not found: " + id);
        return v;
    }

    public List<Vehicle> listAll() {
        return repo.listAll();
    }

    public List<Vehicle> search(String keyword) {
        String k = keyword.toLowerCase();
        List<Vehicle> out = new ArrayList<Vehicle>();
        for (Vehicle v : repo.listAll()) {
            if (v.getId().toLowerCase().contains(k) || v.getRegNo().toLowerCase().contains(k)
                    || v.getType().toLowerCase().contains(k)) out.add(v);
        }
        return out;
    }

    public synchronized void delete(String id) {
        get(id);
        repo.delete(id);
        audit.log("DELETE vehicle " + id);
    }

    public boolean exists(String id) {
        return repo.exists(id);
    }
}
