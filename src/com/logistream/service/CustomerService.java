package com.logistream.service;

import com.logistream.util.LogiStreamUtil;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Customer;
import com.logistream.model.Parcel;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    private final MemoryRepository<Customer> repo;
    private final MemoryRepository<Parcel> parcels;
    private final AuditLogger audit;

    public CustomerService(MemoryRepository<Customer> repo, MemoryRepository<Parcel> parcels, AuditLogger audit) {
        this.repo = repo;
        this.parcels = parcels;
        this.audit = audit;
    }

    public synchronized Customer add(String name, String phone, String address, String zone) {
        LogiStreamUtil.requireNonBlank(name, "Name");
        LogiStreamUtil.requirePhone(phone);
        LogiStreamUtil.requireNonBlank(zone, "Zone");
        String id = MemoryRepository.nextId("CUS", repo.allIds());
        Customer c = new Customer(id, name, phone, address, zone);
        repo.save(c);
        audit.log("ADD customer " + id + " " + name);
        return c;
    }

    public Customer get(String id) {
        Customer c = repo.get(id);
        if (c == null) throw new LogiStreamException("Customer not found: " + id);
        return c;
    }

    public List<Customer> listAll() {
        return repo.listAll();
    }

    public List<Customer> search(String keyword) {
        String k = keyword.toLowerCase();
        List<Customer> out = new ArrayList<Customer>();
        for (Customer c : repo.listAll()) {
            if (c.getId().toLowerCase().contains(k) || c.getName().toLowerCase().contains(k)
                    || c.getZone().toLowerCase().contains(k) || c.getPhone().contains(k)) out.add(c);
        }
        return out;
    }

    /** Blank arguments mean "keep the old value". */
    public synchronized Customer update(String id, String name, String phone, String address, String zone) {
        Customer c = get(id);
        if (!name.isEmpty()) c.setName(name);
        if (!phone.isEmpty()) {
            LogiStreamUtil.requirePhone(phone);
            c.setPhone(phone);
        }
        if (!address.isEmpty()) c.setAddress(address);
        if (!zone.isEmpty()) c.setZone(zone);
        repo.save(c);
        audit.log("UPDATE customer " + id);
        return c;
    }

    public synchronized void delete(String id) {
        get(id);
        for (Parcel p : parcels.listAll()) {
            if (p.getCustomerId().equals(id))
                throw new LogiStreamException("Customer " + id + " still has parcels; delete those first.");
        }
        repo.delete(id);
        audit.log("DELETE customer " + id);
    }
}
