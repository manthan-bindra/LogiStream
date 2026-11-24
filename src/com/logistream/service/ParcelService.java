package com.logistream.service;

import com.logistream.util.LogiStreamUtil;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Customer;
import com.logistream.model.Parcel;
import com.logistream.model.ParcelFactory;
import com.logistream.model.ParcelStatus;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParcelService {
    private final MemoryRepository<Parcel> repo;
    private final MemoryRepository<Customer> customers;
    private final AuditLogger audit;

    public ParcelService(MemoryRepository<Parcel> repo, MemoryRepository<Customer> customers, AuditLogger audit) {
        this.repo = repo;
        this.customers = customers;
        this.audit = audit;
    }

    /** Overloaded: parcels without an insured value. */
    public Parcel register(String type, String customerId, String destination, double weightKg) {
        return register(type, customerId, destination, weightKg, 0);
    }

    public synchronized Parcel register(String type, String customerId, String destination,
                                        double weightKg, double insuredValue) {
        if (!customers.exists(customerId)) throw new LogiStreamException("Customer not found: " + customerId);
        LogiStreamUtil.requireNonBlank(destination, "Destination");
        LogiStreamUtil.requirePositive(weightKg, "Weight");
        String id = MemoryRepository.nextId("PCL", repo.allIds());
        Parcel p = ParcelFactory.create(type, id, customerId, LogiStreamUtil.clean(destination), weightKg, insuredValue);
        repo.save(p);
        audit.log("REGISTER parcel " + id + " " + p.getType() + " for " + customerId);
        return p;
    }

    public Parcel get(String id) {
        Parcel p = repo.get(id);
        if (p == null) throw new LogiStreamException("Parcel not found: " + id);
        return p;
    }

    /** All parcels, most urgent first (Collections.sort uses Parcel's Comparable). */
    public List<Parcel> listAll() {
        List<Parcel> list = repo.listAll();
        Collections.sort(list);
        return list;
    }

    public List<Parcel> pending() {
        return search(ParcelStatus.REGISTERED);
    }

    /** Overloaded search #1: by keyword. */
    public List<Parcel> search(String keyword) {
        String k = keyword.toLowerCase();
        List<Parcel> out = new ArrayList<Parcel>();
        for (Parcel p : repo.listAll()) {
            if (p.getId().toLowerCase().contains(k) || p.getCustomerId().toLowerCase().contains(k)
                    || p.getDestination().toLowerCase().contains(k) || p.getType().toLowerCase().contains(k)) out.add(p);
        }
        return out;
    }

    /** Overloaded search #2: by status. */
    public List<Parcel> search(ParcelStatus status) {
        List<Parcel> out = new ArrayList<Parcel>();
        for (Parcel p : repo.listAll()) {
            if (p.getStatus() == status) out.add(p);
        }
        return out;
    }

    public synchronized void updateDestination(String id, String destination) {
        LogiStreamUtil.requireNonBlank(destination, "Destination");
        Parcel p = get(id);
        p.setDestination(LogiStreamUtil.clean(destination));
        repo.save(p);
        audit.log("UPDATE parcel " + id + " destination=" + p.getDestination());
    }

    public void updateStatus(String id, ParcelStatus status) {
        updateStatusInternal(id, status);
    }

    public synchronized void updateStatusInternal(String id, ParcelStatus status) {
        Parcel p = get(id);
        p.setStatus(status);
        repo.save(p);
        audit.log("STATUS parcel " + id + " -> " + status);
    }

    /** Used by dispatch: attach a driver and mark the parcel DISPATCHED. */
    public synchronized void dispatchTo(String id, String driverId) {
        Parcel p = get(id);
        p.setDriverId(driverId);
        p.setStatus(ParcelStatus.DISPATCHED);
        repo.save(p);
        audit.log("DISPATCH parcel " + id + " to " + driverId);
    }

    /** Failed parcel goes back to the pending pool for another attempt. */
    public synchronized void resetForRetry(String id) {
        Parcel p = get(id);
        p.setStatus(ParcelStatus.REGISTERED);
        p.setDriverId("-");
        repo.save(p);
        audit.log("RETRY parcel " + id);
    }

    public synchronized void delete(String id) {
        get(id);
        repo.delete(id);
        audit.log("DELETE parcel " + id);
    }

}
