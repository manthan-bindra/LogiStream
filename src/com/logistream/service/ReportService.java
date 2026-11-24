package com.logistream.service;

import com.logistream.util.LogiStreamUtil;

import com.logistream.model.Driver;
import com.logistream.model.Parcel;
import com.logistream.model.ParcelStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/** Operational analytics built with collections (EnumMap, TreeMap), a Comparator and StringBuilder. */
public class ReportService {
    private final ParcelService parcels;
    private final DriverService drivers;
    private final ShiftService shifts;

    public ReportService(ParcelService parcels, DriverService drivers, ShiftService shifts) {
        this.parcels = parcels; this.drivers = drivers; this.shifts = shifts;
    }

    /** Builds a text bar such as "#####" using StringBuilder. */
    private String bar(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('#');
        return sb.toString();
    }

    public List<String> generate() {
        List<Parcel> all = parcels.listAll();
        List<String> out = new ArrayList<String>();
        out.add("LOGISTREAM OPERATIONS REPORT  -  " + LogiStreamUtil.nowText());
        out.add("--------------------------------------------------------");
        out.add("Total parcels: " + all.size());

        Map<ParcelStatus, Integer> byStatus = new EnumMap<ParcelStatus, Integer>(ParcelStatus.class);
        for (ParcelStatus s : ParcelStatus.values()) byStatus.put(s, 0);
        Map<String, Double> revenue = new TreeMap<String, Double>();
        Map<String, Integer> byZone = new TreeMap<String, Integer>();
        double totalWeight = 0;
        double totalRevenue = 0;
        Parcel heaviest = null;

        for (Parcel p : all) {
            byStatus.put(p.getStatus(), byStatus.get(p.getStatus()) + 1);
            Integer zoneCount = byZone.get(p.getDestination());
            byZone.put(p.getDestination(), zoneCount == null ? 1 : zoneCount + 1);
            totalWeight += p.getWeightKg();
            if (heaviest == null || p.getWeightKg() > heaviest.getWeightKg()) heaviest = p;
            if (p.getStatus() == ParcelStatus.DELIVERED) {
                Double current = revenue.get(p.getType());
                revenue.put(p.getType(), (current == null ? 0.0 : current) + p.calculateCharge());
                totalRevenue += p.calculateCharge();
            }
        }

        out.add("");
        out.add("Parcels by status:");
        for (Map.Entry<ParcelStatus, Integer> e : byStatus.entrySet()) {
            out.add(String.format("  %-12s %-3d %s", e.getKey(), e.getValue(), bar(e.getValue())));
        }
        int delivered = byStatus.get(ParcelStatus.DELIVERED);
        int failed = byStatus.get(ParcelStatus.FAILED);
        out.add(delivered + failed == 0 ? "Delivery success rate: n/a"
                : String.format("Delivery success rate: %.1f%%", 100.0 * delivered / (delivered + failed)));

        out.add("");
        out.add("Revenue from delivered parcels, by type:");
        for (Map.Entry<String, Double> e : revenue.entrySet()) {
            out.add(String.format("  %-10s Rs.%.2f", e.getKey(), e.getValue()));
        }
        out.add(String.format("  %-10s Rs.%.2f", "TOTAL", totalRevenue));

        out.add("");
        out.add("Parcels by destination zone:");
        for (Map.Entry<String, Integer> e : byZone.entrySet()) {
            out.add(String.format("  %-10s %d", e.getKey(), e.getValue()));
        }

        out.add("");
        out.add(String.format("Average parcel weight: %.2f kg", all.isEmpty() ? 0.0 : totalWeight / all.size()));
        if (heaviest != null) out.add(String.format("Heaviest parcel: %s (%.1f kg)", heaviest.getId(), heaviest.getWeightKg()));

        out.add("");
        out.add("Driver leaderboard (top 5 by deliveries):");
        List<Driver> ranking = drivers.listAll();
        ranking.sort((a, b) -> {
            int byCount = Integer.compare(b.getDeliveredCount(), a.getDeliveredCount());
            return byCount != 0 ? byCount : a.getName().compareToIgnoreCase(b.getName());
        });
        for (int i = 0; i < ranking.size() && i < 5; i++) {
            Driver d = ranking.get(i);
            out.add(String.format("  %d. %-8s %-14s %d delivered", i + 1, d.getId(), d.getName(), d.getDeliveredCount()));
        }

        out.add("");
        out.add("Minutes worked per driver (all shifts):");
        for (Map.Entry<String, Double> e : shifts.minutesByDriver().entrySet()) {
            out.add(String.format("  %-8s %.1f min", e.getKey(), e.getValue()));
        }
        return out;
    }

    public List<String> collectionSummary(List<Parcel> parcels) {
        Set<String> uniqueZones = new HashSet<String>();
        Set<String> enteredZones = new LinkedHashSet<String>();
        Set<String> sortedZones = new TreeSet<String>();
        Deque<String> recentIds = new ArrayDeque<String>();
        for (Parcel parcel : parcels) {
            uniqueZones.add(parcel.getDestination());
            enteredZones.add(parcel.getDestination());
            sortedZones.add(parcel.getDestination());
            recentIds.addLast(parcel.getId());
            if (recentIds.size() > 5) recentIds.removeFirst();
        }
        List<String> lines = new ArrayList<String>();
        lines.add("Unique destination zones (HashSet): " + uniqueZones);
        lines.add("Zones in first-seen order (LinkedHashSet): " + enteredZones);
        lines.add("Zones alphabetically (TreeSet): " + sortedZones);
        lines.add("Five most recent parcel IDs (Deque): " + recentIds);
        ListIterator<String> iterator = new ArrayList<String>(recentIds).listIterator();
        StringBuffer ids = new StringBuffer();
        while (iterator.hasNext()) {
            if (ids.length() > 0) ids.append(" -> ");
            ids.append(iterator.next());
        }
        lines.add("Recent parcel sequence (ListIterator + StringBuffer): " + ids);
        String literal = "LOGISTREAM";
        String interned = new String("LOGISTREAM").intern();
        lines.add("String Pool reference equality after intern(): " + (literal == interned));
        return lines;
    }


}
