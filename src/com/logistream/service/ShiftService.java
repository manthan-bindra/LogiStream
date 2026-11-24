package com.logistream.service;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Driver;
import com.logistream.model.DriverStatus;
import com.logistream.model.Shift;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Tracks driver shifts (clock-in / clock-out) and keeps driver status in step. */
public class ShiftService {
    private final MemoryRepository<Shift> repo;
    private final DriverService drivers;
    private final AuditLogger audit;

    public ShiftService(MemoryRepository<Shift> repo, DriverService drivers, AuditLogger audit) {
        this.repo = repo;
        this.drivers = drivers;
        this.audit = audit;
    }

    public synchronized Shift start(String driverId) {
        Driver d = drivers.get(driverId);
        if (findOpen(driverId) != null)
            throw new LogiStreamException("Driver " + driverId + " already has an open shift.");
        Shift s = new Shift(MemoryRepository.nextId("SHF", repo.allIds()), d.getId());
        repo.save(s);
        drivers.setStatus(driverId, DriverStatus.AVAILABLE);
        audit.log("SHIFT start " + s.getId() + " driver " + driverId);
        return s;
    }

    public synchronized Shift end(String driverId) {
        Driver d = drivers.get(driverId);
        Shift s = findOpen(driverId);
        if (s == null) throw new LogiStreamException("Driver " + driverId + " has no open shift.");
        if (d.getStatus() == DriverStatus.ON_DELIVERY)
            throw new LogiStreamException("Driver is on delivery; finish the route first.");
        s.close();
        repo.save(s);
        drivers.setStatus(driverId, DriverStatus.OFF_DUTY);
        audit.log("SHIFT end " + s.getId() + " driver " + driverId);
        return s;
    }

    /** Returns the driver's open shift or null. */
    public Shift findOpen(String driverId) {
        for (Shift s : repo.listAll()) {
            if (s.getDriverId().equals(driverId) && s.isOpen()) return s;
        }
        return null;
    }

    public List<Shift> listAll() {
        return repo.listAll();
    }

    /** TreeMap (sorted by driver ID): total minutes worked per driver. */
    public Map<String, Double> minutesByDriver() {
        Map<String, Double> totals = new TreeMap<String, Double>();
        for (Shift s : repo.listAll()) {
            Double current = totals.get(s.getDriverId());               // may be null
            totals.put(s.getDriverId(), (current == null ? 0.0 : current) + s.minutesWorked());
        }
        return totals;
    }
}
