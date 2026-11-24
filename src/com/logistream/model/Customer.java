package com.logistream.model;

public class Customer extends Entity {
    private String name;
    private String phone;
    private String address;
    private String zone;

    public Customer(String id, String name, String phone, String address, String zone) {
        super(id);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.zone = zone;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    @Override
    public String toRecord() {
        return String.join("|", getId(), name, phone, address, zone);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-14s %-11s %-8s %s", getId(), name, phone, zone, address);
    }
}
