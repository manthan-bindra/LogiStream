package com.logistream.model;

public class Van extends Vehicle {
    public static final double DEFAULT_CAPACITY = 600;

    public Van(String id, String regNo) {
        this(id, regNo, DEFAULT_CAPACITY);
    }

    public Van(String id, String regNo, double capacityKg) {
        super(id, regNo, capacityKg);
    }

    @Override
    public double costPerKm() {
        return 12;
    }

    @Override
    public String getType() {
        return "VAN";
    }
}
