package com.logistream.model;

/** Abstract delivery vehicle. Subclasses define their own running cost. */
public abstract class Vehicle extends Entity {
    private final String regNo;
    private final double capacityKg;

    protected Vehicle(String id, String regNo, double capacityKg) {
        super(id);
        this.regNo = regNo;
        this.capacityKg = capacityKg;
    }

    public abstract double costPerKm();

    public abstract String getType();

    public String getRegNo() { return regNo; }
    public double getCapacityKg() { return capacityKg; }

    @Override
    public String toRecord() {
        return String.join("|", getType(), getId(), regNo, String.valueOf(capacityKg));
    }

    @Override
    public String toString() {
        return String.format("%-8s %-5s %-12s cap=%7.1fkg  Rs.%.1f/km", getId(), getType(), regNo, capacityKg, costPerKm());
    }
}
