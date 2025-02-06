package com.example.myapp.enums;
/**
 * Enumeration representing different time intervals.
 * <p>
 * Each interval is defined by a label (e.g., "1m" for one minute) and its equivalent duration in milliseconds.
 * </p>
 */
public enum Interval {
    ONE_MINUTE("1m", 60000),
    FIVE_MINUTES("5m", 300000),
    ONE_HOUR("1h", 3600000),
    ONE_DAY("1d", 86400000);

    private final String label;
    private final long milliseconds;

    Interval(String label, long milliseconds) {
        this.label = label;
        this.milliseconds = milliseconds;
    }

    public String getLabel() {
        return label;
    }

    public long getMilliseconds() {
        return milliseconds;
    }


    public static Interval fromLabel(String label) {
        for (Interval interval : values()) {
            if (interval.label.equals(label)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Unsupported interval: " + label);
    }
}
