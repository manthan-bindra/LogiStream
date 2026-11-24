package com.logistream.concurrent;

import com.logistream.exception.LogiStreamException;
import com.logistream.model.Route;
import com.logistream.service.DriverService;
import com.logistream.service.ParcelService;
import com.logistream.service.RouteService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Starts one thread per active route plus one notifier thread,
 * then join()s the drivers, tells the notifier to stop and join()s it as well.
 */
public class DeliverySimulator {

    public static class Summary {
        public final int routes;
        public final int delivered;
        public final int failed;

        Summary(int routes, int delivered, int failed) {
            this.routes = routes;
            this.delivered = delivered;
            this.failed = failed;
        }
    }

    private final RouteService routes;
    private final ParcelService parcels;
    private final DriverService drivers;

    public DeliverySimulator(RouteService routes, ParcelService parcels, DriverService drivers) {
        this.routes = routes; this.parcels = parcels; this.drivers = drivers;
    }

    public Summary runAll() {
        List<Route> active = routes.listActive();
        if (active.isEmpty()) throw new LogiStreamException("No active routes. Dispatch parcels first.");

        AtomicInteger delivered = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        EventBuffer buffer = new EventBuffer(5);

        NotificationWorker notifier = new NotificationWorker(buffer);
        notifier.start();

        List<Thread> driverThreads = new ArrayList<Thread>();
        for (Route r : active) {
            Thread t = new Thread(new DriverWorker(r, parcels, drivers, routes, buffer, delivered, failed),
                    "Driver-" + r.getDriverId());
            driverThreads.add(t);
            t.start();
        }

        try {
            for (Thread t : driverThreads) t.join();         // wait for every driver to finish
            buffer.put(NotificationWorker.STOP);             // then stop the consumer
            notifier.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LogiStreamException("Delivery run was interrupted.");
        }
        return new Summary(active.size(), delivered.get(), failed.get());
    }
}
