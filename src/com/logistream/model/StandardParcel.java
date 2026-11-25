package com.logistream.model;

/** Normal parcel: base fee plus per-kg rate. */
public class StandardParcel extends Parcel {
    public StandardParcel(String id, String customerId, String destination, double weightKg) {
        super(id, customerId, destination, weightKg, Priority.NORMAL);
    }

    public StandardParcel(String id, String customerId, String destination, double weightKg,
                          ParcelStatus status, String driverId, String createdOn) {
        super(id, customerId, destination, weightKg, Priority.NORMAL, status, driverId, createdOn);
    }

    @Override
    public double calculateCharge() {
        return 40 + 12 * getWeightKg();
    }

    @Override
    public String getType() {
        return "STANDARD";
    }
}
