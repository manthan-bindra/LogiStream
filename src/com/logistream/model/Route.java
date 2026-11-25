package com.logistream.model;

import com.logistream.util.DateUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** A driver's ordered list of delivery stops (parcel IDs), kept in a LinkedList. */
public class Route extends Entity {
    private final String driverId;
    private final String date;
    private final LinkedList<String> stops = new LinkedList<String>();
    private boolean active;

    public Route(String id, String driverId, List<String> stops) {
        this(id, driverId, DateUtil.today(), stops, true);
    }

    public Route(String id, String driverId, String date, List<String> stops, boolean active) {
        super(id);
        this.driverId = driverId;
        this.date = date;
        this.stops.addAll(stops);
        this.active = active;
    }

    public String getDriverId() { return driverId; }
    public String getDate() { return date; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getStops() { return new ArrayList<String>(stops); }
    public int size() { return stops.size(); }
    public void addStop(String parcelId) { stops.addLast(parcelId); }
    public boolean hasStop(String parcelId) { return stops.contains(parcelId); }

    /** Uses an Iterator to remove safely while walking the list. */
    public boolean removeStop(String parcelId) {
        Iterator<String> it = stops.iterator();
        while (it.hasNext()) {
            if (it.next().equals(parcelId)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public String toRecord() {
        return String.join("|", getId(), driverId, date, String.join(",", stops), String.valueOf(active));
    }

    @Override
    public String toString() {
        return String.format("%-8s driver=%-8s %s %-8s stops=%s", getId(), driverId, date,
                active ? "ACTIVE" : "DONE", stops);
    }
}
