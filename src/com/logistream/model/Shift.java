package com.logistream.model;

import com.logistream.util.DateUtil;

/** One working shift of a driver. Open while endMillis is 0. */
public class Shift extends Entity {
    private final String driverId;
    private final long startMillis;
    private long endMillis;

    public Shift(String id, String driverId) {
        this(id, driverId, System.currentTimeMillis(), 0L);
    }

    public Shift(String id, String driverId, long startMillis, long endMillis) {
        super(id);
        this.driverId = driverId;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    public String getDriverId() { return driverId; }
    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
    public boolean isOpen() { return endMillis == 0; }

    public void close() {
        this.endMillis = System.currentTimeMillis();
    }

    public double minutesWorked() {
        long until = isOpen() ? System.currentTimeMillis() : endMillis;
        return (until - startMillis) / 60000.0;
    }

    @Override
    public String toRecord() {
        return String.join("|", getId(), driverId, String.valueOf(startMillis), String.valueOf(endMillis));
    }

    @Override
    public String toString() {
        return String.format("%-8s driver=%-8s start=%s end=%s (%.1f min)", getId(), driverId,
                DateUtil.format(startMillis), isOpen() ? "OPEN" : DateUtil.format(endMillis), minutesWorked());
    }
}
