package com.logistream.util;

/**
 * Uses a 2-D ARRAY as a distance matrix (in km) between the four city zones.
 * Dispatch uses it to send a parcel to the nearest suitable driver.
 */
public final class ZoneMap {
    public static final String[] ZONES = {"North", "South", "East", "West"};

    //                         North South East West
    private static final int[][] DISTANCE = {
            /* North */ {0, 10, 6, 6},
            /* South */ {10, 0, 6, 6},
            /* East  */ {6, 6, 0, 10},
            /* West  */ {6, 6, 10, 0}
    };

    private static final int UNKNOWN_ZONE_DISTANCE = 15;

    private ZoneMap() {
    }

    private static int indexOf(String zone) {
        for (int i = 0; i < ZONES.length; i++) {
            if (ZONES[i].equalsIgnoreCase(zone)) return i;
        }
        return -1;
    }

    public static int distance(String from, String to) {
        int a = indexOf(from);
        int b = indexOf(to);
        if (a < 0 || b < 0) return from.equalsIgnoreCase(to) ? 0 : UNKNOWN_ZONE_DISTANCE;
        return DISTANCE[a][b];
    }
}
