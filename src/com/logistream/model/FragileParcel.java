package com.logistream.model;

/** Fragile parcel: HIGH priority, adds 2% insurance on the declared value. */
public class FragileParcel extends Parcel {
    private final double insuredValue;

    public FragileParcel(String id, String customerId, String destination, double weightKg, double insuredValue) {
        super(id, customerId, destination, weightKg, Priority.HIGH);
        this.insuredValue = insuredValue;
    }

    public FragileParcel(String id, String customerId, String destination, double weightKg, double insuredValue,
                         ParcelStatus status, String driverId, String createdOn) {
        super(id, customerId, destination, weightKg, Priority.HIGH, status, driverId, createdOn);
        this.insuredValue = insuredValue;
    }

    public double getInsuredValue() {
        return insuredValue;
    }

    @Override
    public double calculateCharge() {
        return 60 + 15 * getWeightKg() + 0.02 * insuredValue;
    }

    @Override
    public String getType() {
        return "FRAGILE";
    }

    @Override
    protected String extraField() {
        return String.valueOf(insuredValue);
    }
}
