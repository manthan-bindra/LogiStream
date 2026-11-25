package com.logistream.model;

/** Delivery priority. Lower level number = served first by the PriorityQueue. */
public enum Priority {
    URGENT(1), HIGH(2), NORMAL(3);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
