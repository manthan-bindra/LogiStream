package com.logistream.ui;

import com.logistream.model.Customer;
import com.logistream.model.Parcel;
import com.logistream.model.ParcelStatus;
import com.logistream.service.CustomerService;
import com.logistream.service.ParcelService;
import com.logistream.util.ConsoleUI;
import com.logistream.util.InputHelper;

public class ParcelMenu extends BaseMenu {
    private final ParcelService parcels;
    private final CustomerService customers;

    public ParcelMenu(InputHelper in, ParcelService parcels, CustomerService customers) {
        super(in);
        this.parcels = parcels;
        this.customers = customers;
    }

    @Override
    public String title() {
        return "Parcel Management";
    }

    @Override
    protected String[] options() {
        return new String[]{"Register parcel", "View all parcels (urgent first)", "Search by keyword",
                "Search by status", "Update destination", "Update status", "Delete parcel",
                "Undo last status change", "Show delivery charge with discount"};
    }

    @Override
    protected void handle(int choice) {
        switch (choice) {
            case 1: register(); break;
            case 2:
                printList(parcels.listAll(), "No parcels yet.");
                break;
            case 3:
                printList(parcels.search(in.readText("Keyword (id, customer, zone, type)")), "No match.");
                break;
            case 4:
                printList(parcels.search(readStatus()), "No parcels with that status.");
                break;
            case 5:
                parcels.updateDestination(in.readText("Parcel ID"), in.readText("New destination zone"));
                ConsoleUI.success("Destination updated.");
                break;
            case 6:
                parcels.updateStatus(in.readText("Parcel ID"), readStatus());
                ConsoleUI.success("Status updated.");
                break;
            case 7:
                String id = in.readText("Parcel ID");
                if (in.confirm("Delete " + id + "?")) {
                    parcels.delete(id);
                    ConsoleUI.success("Parcel deleted.");
                }
                break;
            case 8:
                ConsoleUI.info(parcels.undoLast());
                break;
            case 9:
                Parcel p = parcels.get(in.readText("Parcel ID"));
                double pct = in.readDouble("Discount %", 0);
                System.out.printf("  Normal charge: Rs.%.2f   After %.1f%% discount: Rs.%.2f%n",
                        p.calculateCharge(), pct, p.calculateCharge(pct));
                break;
            default: break;
        }
    }

    private void register() {
        Customer c = customers.get(in.readText("Customer ID"));
        System.out.println("  Types: 1=STANDARD  2=EXPRESS  3=FRAGILE");
        int t = in.readInt("Type", 1, 3);
        String type = new String[]{"STANDARD", "EXPRESS", "FRAGILE"}[t - 1];
        String zone = in.readOptional("Destination zone [default " + c.getZone() + "]");
        if (zone.isBlank()) zone = c.getZone();
        double weight = in.readDouble("Weight in kg", 0.1);
        Parcel p = t == 3
                ? parcels.register(type, c.getId(), zone, weight, in.readDouble("Insured value (Rs.)", 0))
                : parcels.register(type, c.getId(), zone, weight);
        ConsoleUI.success("Registered " + p.getId() + "  charge Rs." + String.format("%.2f", p.calculateCharge()));
    }

    private ParcelStatus readStatus() {
        ParcelStatus[] all = ParcelStatus.values();
        for (int i = 0; i < all.length; i++) System.out.printf("   %d=%s", i + 1, all[i]);
        System.out.println();
        return all[in.readInt("Status", 1, all.length) - 1];
    }
}
