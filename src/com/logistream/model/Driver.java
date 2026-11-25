package com.logistream.model;

/** A delivery driver. Encapsulated fields, changed only through methods. */
public class Driver extends Entity {
    private String name;
    private String phone;
    private String zone;
    private String vehicleId;
    private DriverStatus status;
    private int deliveredCount;

    public Driver(String id, String name, String phone, String zone) {
        this(id, name, phone, zone, "-", DriverStatus.OFF_DUTY, 0);
    }

    public Driver(String id, String name, String phone, String zone, String vehicleId,
                  DriverStatus status, int deliveredCount) {
        super(id);
        this.name = name;
        this.phone = phone;
        this.zone = zone;
        this.vehicleId = vehicleId;
        this.status = status;
        this.deliveredCount = deliveredCount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public int getDeliveredCount() { return deliveredCount; }
    public void incrementDelivered() { deliveredCount++; }

    @Override
    public String toRecord() {
        return String.join("|", getId(), name, phone, zone, vehicleId, status.name(), String.valueOf(deliveredCount));
    }

    @Override
    public String toString() {
        return String.format("%-8s %-14s %-11s %-8s veh=%-8s %-11s delivered=%d",
                getId(), name, phone, zone, vehicleId, status, deliveredCount);
    }
}
