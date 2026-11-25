package com.logistream.model;

import com.logistream.exception.InvalidInputException;
import com.logistream.exception.LogiStreamException;

/** FACTORY pattern: builds the right Parcel subclass from a type name or from a file line. */
public final class ParcelFactory {
    private ParcelFactory() {
    }

    public static Parcel create(String type, String id, String customerId, String destination,
                                double weightKg, double insuredValue) {
        String t = type.toUpperCase();
        if (t.equals("STANDARD")) return new StandardParcel(id, customerId, destination, weightKg);
        if (t.equals("EXPRESS")) return new ExpressParcel(id, customerId, destination, weightKg);
        if (t.equals("FRAGILE")) return new FragileParcel(id, customerId, destination, weightKg, insuredValue);
        throw new InvalidInputException("Unknown parcel type: " + type + " (use STANDARD, EXPRESS or FRAGILE)");
    }

    /** Record layout: type|id|customerId|destination|weight|status|driverId|date|extra */
    public static Parcel fromRecord(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 9) throw new LogiStreamException("Bad parcel record: " + line);
        double weight = Double.parseDouble(f[4]);          // wrapper class parsing
        ParcelStatus status = ParcelStatus.valueOf(f[5]);
        if (f[0].equals("STANDARD")) return new StandardParcel(f[1], f[2], f[3], weight, status, f[6], f[7]);
        if (f[0].equals("EXPRESS")) return new ExpressParcel(f[1], f[2], f[3], weight, status, f[6], f[7]);
        if (f[0].equals("FRAGILE"))
            return new FragileParcel(f[1], f[2], f[3], weight, Double.parseDouble(f[8]), status, f[6], f[7]);
        throw new LogiStreamException("Unknown parcel type in file: " + f[0]);
    }
}
