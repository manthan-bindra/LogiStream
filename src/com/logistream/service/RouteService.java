package com.logistream.service;

import com.logistream.exception.LogiStreamException;

import com.logistream.model.Route;
import com.logistream.repository.MemoryRepository;
import com.logistream.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;

/** Creates routes and supports route lookup/completion. */
public class RouteService {
    private final MemoryRepository<Route> repo;
    private final ParcelService parcels;
    private final AuditLogger audit;

    public RouteService(MemoryRepository<Route> repo, ParcelService parcels, AuditLogger audit) {
        this.repo = repo;
        this.parcels = parcels;
        this.audit = audit;
    }

    public synchronized Route create(String driverId, List<String> parcelIds) {
        Route r = new Route(MemoryRepository.nextId("RTE", repo.allIds()), driverId, parcelIds);
        repo.save(r);
        audit.log("ROUTE created " + r.getId() + " for " + driverId + " with " + parcelIds.size() + " stops");
        return r;
    }

    public Route get(String id) {
        Route r = repo.get(id);
        if (r == null) throw new LogiStreamException("Route not found: " + id);
        return r;
    }

    public List<Route> listAll() {
        return repo.listAll();
    }

    public List<Route> listActive() {
        List<Route> out = new ArrayList<Route>();
        for (Route r : repo.listAll()) {
            if (r.isActive()) out.add(r);
        }
        return out;
    }

    public double totalWeight(Route r) {
        double sum = 0;
        for (String pid : r.getStops()) sum += parcels.get(pid).getWeightKg();
        return sum;
    }

    public synchronized void complete(String routeId) {
        Route r = get(routeId);
        r.setActive(false);
        repo.save(r);
        audit.log("ROUTE completed " + routeId);
    }

    public synchronized void delete(String routeId) {
        get(routeId);
        repo.delete(routeId);
        audit.log("DELETE route " + routeId);
    }
}
