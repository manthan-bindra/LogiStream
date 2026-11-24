package com.logistream.util;

import java.util.Scanner;

/** Wraps Scanner so menus get safe, validated input (re-asks until the value is valid). */
public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt + ": ");
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Non-empty text. */
    public String readText(String prompt) {
        while (true) {
            String s = LogiStreamUtil.clean(readLine(prompt));
            if (!s.isEmpty()) return s;
            LogiStreamUtil.error("Value cannot be empty.");
        }
    }

    /** Text that may be left blank (e.g. "press Enter to keep old value"). */
    public String readOptional(String prompt) {
        return LogiStreamUtil.clean(readLine(prompt + " (Enter to skip)"));
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(readLine(prompt));
                if (v >= min && v <= max) return v;
                LogiStreamUtil.error("Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                LogiStreamUtil.error("Please enter a whole number.");
            }
        }
    }

    public double readDouble(String prompt, double min) {
        while (true) {
            try {
                double v = Double.parseDouble(readLine(prompt));
                if (v >= min) return v;
                LogiStreamUtil.error("Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                LogiStreamUtil.error("Please enter a number.");
            }
        }
    }

    public String readPhone(String prompt) {
        while (true) {
            String s = readLine(prompt + " (10 digits)");
            if (LogiStreamUtil.isValidPhone(s)) return s;
            LogiStreamUtil.error("Phone must be exactly 10 digits.");
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n)").toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
        }
    }
}
