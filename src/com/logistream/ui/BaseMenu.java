package com.logistream.ui;

import com.logistream.util.LogiStreamUtil;

import com.logistream.exception.LogiStreamException;
import com.logistream.util.InputHelper;

import java.util.List;

/**
 * TEMPLATE METHOD pattern: run() draws the menu, reads the choice and catches errors.
 * Each subclass only supplies its options and what to do for each choice.
 */
public abstract class BaseMenu implements Menu {
    protected final InputHelper in;

    protected BaseMenu(InputHelper in) {
        this.in = in;
    }

    protected abstract String[] options();

    protected abstract void handle(int choice);

    protected String zeroOptionLabel() { return "Back"; }

    @Override
    public void run() {
        while (true) {
            LogiStreamUtil.banner(title());
            String[] options = options();
            for (int i = 0; i < options.length; i++) {
                System.out.printf("  %2d. %s%n", i + 1, options[i]);
            }
            System.out.println("   0. " + zeroOptionLabel());
            int choice = in.readInt("Choose", 0, options.length);
            if (choice == 0) return;
            try {
                handle(choice);
            } catch (LogiStreamException e) {                 // our own business-rule errors
                LogiStreamUtil.error(e.getMessage());
            } catch (RuntimeException e) {                    // anything unexpected: report, don't crash
                LogiStreamUtil.error("Unexpected problem: " + e);
            }
        }
    }

    protected void printList(List<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            LogiStreamUtil.info(emptyMessage);
            return;
        }
        for (Object item : items) System.out.println("  " + item);
        System.out.println("  (" + items.size() + " record(s))");
    }
}
