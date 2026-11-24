package com.logistream.concurrent;

import com.logistream.exception.LogiStreamException;
import com.logistream.model.DriverStatus;
import com.logistream.model.Parcel;
import com.logistream.model.ParcelStatus;
import com.logistream.model.Route;
import com.logistream.service.DriverService;
import com.logistream.service.ParcelService;
import com.logistream.service.RouteService;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MULTITHREADING: one DriverWorker (a Runnable) runs per route on its own thread.
 * It "drives" through the stops, marking each parcel IN_TRANSIT and then DELIVERED or FAILED,
 * and posts an event for the notifier thread. Shared services are synchronized, so concurrent
 * updates never corrupt the data files.
 */
public class DriverWorker implements Runnable {
    private final Route route;
    private final ParcelService parcels;
    private final DriverService drivers;
    private final RouteService routes;
    private final EventBuffer buffer;
    private final AtomicInteger delivered;
    private final AtomicInteger failed;
    private final Random random = new Random();

    public DriverWorker(Route route, ParcelService parcels, DriverService drivers, RouteService routes,
                        EventBuffer buffer, AtomicInteger delivered, AtomicInteger failed) {
        this.route = route;
        this.parcels = parcels;
        this.drivers = drivers;
        this.routes = routes;
        this.buffer = buffer;
        this.delivered = delivered;
        this.failed = failed;
    }

    @Override
    public void run() {
        String driverId = route.getDriverId();
        String me = Thread.currentThread().getName();
        System.out.printf("  [%s] starts route %s (%d stops)%n", me, route.getId(), route.size());
        try {
            for (String parcelId : route.getStops()) {
                Thread.sleep(150 + random.nextInt(200));                       // driving
                parcels.updateStatusInternal(parcelId, ParcelStatus.IN_TRANSIT);
                Thread.sleep(150 + random.nextInt(200));                       // at the door
                Parcel p = parcels.get(parcelId);
                if (random.nextInt(100) < 85) {                                // 15% chance of a failed attempt
                    parcels.updateStatusInternal(parcelId, ParcelStatus.DELIVERED);
                    drivers.recordDelivery(driverId);
                    delivered.incrementAndGet();
                    buffer.put("Dear " + p.getCustomerId() + ", parcel " + parcelId + " was delivered by " + driverId + ".");
                } else {
                    parcels.updateStatusInternal(parcelId, ParcelStatus.FAILED);
                    failed.incrementAndGet();
                    buffer.put("Dear " + p.getCustomerId() + ", delivery of " + parcelId + " failed; we will retry.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("  [" + me + "] interrupted");
        } catch (LogiStreamException e) {
            System.out.println("  [" + me + "] error: " + e.getMessage());
        } finally {
            try {
                routes.complete(route.getId());
                drivers.setStatus(driverId, DriverStatus.AVAILABLE);
            } catch (LogiStreamException ignored) {
                // driver or route was deleted meanwhile
            }
        }
    }
}
