package com.logistream.model;

public class Bike extends Vehicle {
    public static final double DEFAULT_CAPACITY = 20;

    public Bike(String id, String regNo) {
        this(id, regNo, DEFAULT_CAPACITY);   // constructor overloading
    }

    public Bike(String id, String regNo, double capacityKg) {
        super(id, regNo, capacityKg);
    }

    @Override
    public double costPerKm() {
        return 3;
    }

    @Override
    public String getType() {
        return "BIKE";
    }
}
