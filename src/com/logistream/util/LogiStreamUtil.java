package com.logistream.util;

import com.logistream.exception.LogiStreamException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Shared console, date, and input helpers for this small console application. */
public final class LogiStreamUtil {
    private LogiStreamUtil() { }

    public static void banner(String title) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("  " + title.toUpperCase());
        System.out.println("=".repeat(60));
    }

    public static void success(String msg) { System.out.println("[OK]    " + msg); }
    public static void error(String msg) { System.out.println("[ERROR] " + msg); }
    public static void info(String msg) { System.out.println("[INFO]  " + msg); }

    public static String today() { return new SimpleDateFormat("yyyy-MM-dd").format(new Date()); }
    public static String nowText() { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); }
    public static String stamp() { return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); }
    public static String format(long millis) {
        return millis == 0 ? "-" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(millis));
    }

    public static String clean(String value) {
        return value == null ? "" : value.replace("|", "/").replace("\n", " ").trim();
    }

    public static boolean isValidPhone(String phone) { return phone != null && phone.matches("\\d{10}"); }
    public static void requirePhone(String phone) {
        if (!isValidPhone(phone)) throw new LogiStreamException("Phone must be exactly 10 digits.");
    }
    public static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) throw new LogiStreamException(field + " cannot be empty.");
    }
    public static void requirePositive(double value, String field) {
        if (value <= 0) throw new LogiStreamException(field + " must be greater than zero.");
    }
}
