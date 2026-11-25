package com.logistream.model;

/** Same-day parcel: URGENT priority, higher rates. */
public class ExpressParcel extends Parcel {
    public ExpressParcel(String id, String customerId, String destination, double weightKg) {
        super(id, customerId, destination, weightKg, Priority.URGENT);
    }

    public ExpressParcel(String id, String customerId, String destination, double weightKg,
                         ParcelStatus status, String driverId, String createdOn) {
        super(id, customerId, destination, weightKg, Priority.URGENT, status, driverId, createdOn);
    }

    @Override
    public double calculateCharge() {
        return 90 + 25 * getWeightKg();
    }

    @Override
    public String getType() {
        return "EXPRESS";
    }
}
