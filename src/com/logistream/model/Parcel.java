package com.logistream.model;

import com.logistream.util.LogiStreamUtil;


/**
 * INHERITANCE + ABSTRACTION: base of all parcel types.
 * Comparable so a PriorityQueue / Collections.sort can order parcels (urgent first, then oldest).
 */
public abstract class Parcel extends Entity implements Comparable<Parcel> {
    private final String customerId;
    private String destination;
    private final double weightKg;
    private ParcelStatus status;
    private final Priority priority;
    private String driverId;
    private final String createdOn;

    protected Parcel(String id, String customerId, String destination, double weightKg, Priority priority) {
        this(id, customerId, destination, weightKg, priority, ParcelStatus.REGISTERED, "-", LogiStreamUtil.today());
    }

    protected Parcel(String id, String customerId, String destination, double weightKg, Priority priority,
                     ParcelStatus status, String driverId, String createdOn) {
        super(id);
        this.customerId = customerId;
        this.destination = destination;
        this.weightKg = weightKg;
        this.priority = priority;
        this.status = status;
        this.driverId = driverId;
        this.createdOn = createdOn;
    }

    /** POLYMORPHISM: every parcel type calculates its own delivery charge. */
    public abstract double calculateCharge();

    /** METHOD OVERLOADING: same name, extra parameter. */
    public double calculateCharge(double discountPercent) {
        return calculateCharge() * (1 - discountPercent / 100.0);
    }

    public abstract String getType();

    /** Extra field stored in the record (only some subclasses use it). */
    protected String extraField() {
        return "-";
    }

    public String getCustomerId() { return customerId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public double getWeightKg() { return weightKg; }
    public ParcelStatus getStatus() { return status; }
    public void setStatus(ParcelStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getCreatedOn() { return createdOn; }

    @Override
    public String toRecord() {
        return String.join("|", getType(), getId(), customerId, destination, String.valueOf(weightKg),
                status.name(), driverId, createdOn, extraField());
    }

    @Override
    public int compareTo(Parcel other) {
        int byPriority = Integer.compare(priority.getLevel(), other.priority.getLevel());
        if (byPriority != 0) return byPriority;
        int byDate = createdOn.compareTo(other.createdOn);
        return byDate != 0 ? byDate : getId().compareTo(other.getId());
    }

    @Override
    public String toString() {
        return String.format("%-8s %-9s %-8s %-10s %6.1fkg %-11s %-8s Rs.%.2f", getId(), getType(), customerId,
                destination, weightKg, status, driverId, calculateCharge());
    }
}
