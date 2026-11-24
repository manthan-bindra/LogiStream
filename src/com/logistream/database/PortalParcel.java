package com.logistream.database;

/** Customer parcel row used by the MySQL customer and staff portals. */
public class PortalParcel {
    private final String id;
    private final String customerId;
    private final String customerName;
    private final String type;
    private final String destination;
    private final double weightKg;
    private final String status;
    private final String driverId;

    public PortalParcel(String id, String customerId, String customerName, String type,
                        String destination, double weightKg, String status, String driverId) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.type = type;
        this.destination = destination;
        this.weightKg = weightKg;
        this.status = status;
        this.driverId = driverId;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getType() { return type; }
    public String getDestination() { return destination; }

    @Override
    public String toString() {
        return String.format("%-9s customer=%-8s %-16s %-9s destination=%-12s %5.1fkg status=%-12s driver=%s",
                id, customerId, customerName, type, destination, weightKg, status, driverId == null ? "-" : driverId);
    }
}
