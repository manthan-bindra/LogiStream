package com.logistream.service;

import com.logistream.exception.LogiStreamException;
import com.logistream.model.Driver;
import com.logistream.model.DriverStatus;
import com.logistream.model.Parcel;
import com.logistream.model.ParcelStatus;
import com.logistream.util.ZoneMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The heart of the system: last-mile parcel dispatching.
 * - PriorityQueue: urgent parcels are assigned first.
 * - Queue (LinkedList): parcels nobody could carry wait here; failed parcels are re-queued.
 * - HashMap: running load per driver so vehicle capacity is never exceeded.
 * - ZoneMap (2-D array): the nearest suitable driver gets the parcel.
 */
public class DispatchService {

    /** Simple result holder shown to the user. */
    public static class Result {
        public int assigned;
        public int unassigned;
        public int routes;
        public final List<String> notes = new ArrayList<String>();
    }

    private final ParcelService parcels;
    private final DriverService drivers;
    private final RouteService routes;
    private final AuditLogger audit;

    public DispatchService(ParcelService parcels, DriverService drivers, RouteService routes, AuditLogger audit) {
        this.parcels = parcels;
        this.drivers = drivers;
        this.routes = routes;
        this.audit = audit;
    }

    public synchronized Result dispatchPending() {
        List<Parcel> pending = parcels.pending();
        if (pending.isEmpty()) throw new LogiStreamException("No pending (REGISTERED) parcels to dispatch.");
        List<Driver> available = drivers.available();
        if (available.isEmpty())
            throw new LogiStreamException("No available drivers. Start a shift for at least one driver first.");

        PriorityQueue<Parcel> queue = new PriorityQueue<Parcel>(pending);        // urgent first
        Queue<Parcel> unassigned = new LinkedList<Parcel>();
        Map<String, Double> load = new HashMap<String, Double>();                // driverId -> kg
        Map<String, List<String>> plan = new LinkedHashMap<String, List<String>>(); // driverId -> parcelIds

        while (!queue.isEmpty()) {
            Parcel p = queue.poll();
            Driver chosen = pickDriver(p, available, load);
            if (chosen == null) {
                unassigned.add(p);
                continue;
            }
            double current = load.containsKey(chosen.getId()) ? load.get(chosen.getId()) : 0.0;
            load.put(chosen.getId(), current + p.getWeightKg());                 // autoboxing
            if (!plan.containsKey(chosen.getId())) plan.put(chosen.getId(), new ArrayList<String>());
            plan.get(chosen.getId()).add(p.getId());
        }

        Result result = new Result();
        for (Map.Entry<String, List<String>> entry : plan.entrySet()) {
            String driverId = entry.getKey();
            List<String> ids = entry.getValue();
            routes.create(driverId, ids);
            for (String pid : ids) parcels.dispatchTo(pid, driverId);
            drivers.setStatus(driverId, DriverStatus.ON_DELIVERY);
            result.assigned += ids.size();
            result.routes++;
            result.notes.add(String.format("%s -> %d parcel(s), %.1f kg", driverId, ids.size(), load.get(driverId)));
        }
        while (!unassigned.isEmpty()) {
            Parcel p = unassigned.poll();
            result.unassigned++;
            result.notes.add("Could not assign " + p.getId() + " (" + p.getWeightKg() + " kg to " + p.getDestination()
                    + ") - no driver with enough spare capacity; it stays REGISTERED.");
        }
        audit.log("DISPATCH run: assigned=" + result.assigned + " unassigned=" + result.unassigned);
        return result;
    }

    /** Among drivers who can still carry the parcel: nearest zone first, then the lightest load. */
    private Driver pickDriver(Parcel p, List<Driver> candidates, Map<String, Double> load) {
        Driver best = null;
        int bestDistance = 0;
        double bestLoad = 0;
        for (Driver d : candidates) {
            double current = load.containsKey(d.getId()) ? load.get(d.getId()) : 0.0;
            if (current + p.getWeightKg() > drivers.capacityOf(d)) continue;     // would overload the vehicle
            int distance = ZoneMap.distance(d.getZone(), p.getDestination());
            boolean better = best == null || distance < bestDistance
                    || (distance == bestDistance && current < bestLoad);
            if (better) {
                best = d;
                bestDistance = distance;
                bestLoad = current;
            }
        }
        return best;
    }

    /** Re-queues every FAILED parcel so the next dispatch run can try again. */
    public synchronized int requeueFailed() {
        Queue<Parcel> retry = new LinkedList<Parcel>(parcels.search(ParcelStatus.FAILED));
        int count = 0;
        while (!retry.isEmpty()) {
            parcels.resetForRetry(retry.poll().getId());
            count++;
        }
        return count;
    }
}
