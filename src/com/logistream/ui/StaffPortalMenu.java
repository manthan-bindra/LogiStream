package com.logistream.ui;

import com.logistream.app.AppContext;
import com.logistream.database.PortalDao;
import com.logistream.database.PortalParcel;
import com.logistream.model.Customer;
import com.logistream.model.Driver;
import com.logistream.model.Parcel;
import com.logistream.model.Route;
import com.logistream.model.Shift;
import com.logistream.model.Vehicle;
import com.logistream.util.ConsoleUI;
import com.logistream.util.InputHelper;
import java.sql.SQLException;
import java.util.List;

/** Staff workspace for logistics operations and a combined data overview. */
public class StaffPortalMenu extends BaseMenu {
    private final PortalDao portal;
    private final AppContext context;
    private final Runnable demoLoader;

    public StaffPortalMenu(InputHelper in, PortalDao portal, AppContext context, Runnable demoLoader) {
        super(in);
        this.portal = portal;
        this.context = context;
        this.demoLoader = demoLoader;
    }

    @Override public String title() { return "Staff Portal"; }
    @Override protected String[] options() {
        return new String[]{"Dashboard - system overview", "Customer & Delivery Records", "Logistics Operations",
                "Load demo logistics data"};
    }

    @Override protected void handle(int choice) {
        switch (choice) {
            case 1: showOverview(); break;
            case 2: new StaffRecordsMenu(in, portal).run(); break;
            case 3: new StaffOperationsMenu(in, context).run(); break;
            case 4: demoLoader.run(); break;
            default: break;
        }
    }

    private void showOverview() {
        try {
            ConsoleUI.banner("Registered customer accounts (MySQL)");
            List<Customer> customers = portal.findAllCustomers();
            printList(customers, "No customer accounts have registered yet.");

            ConsoleUI.banner("Customer parcel requests (MySQL)");
            List<PortalParcel> parcels = portal.findAllParcels();
            printList(parcels, "No customer portal parcels have been registered yet.");
        } catch (SQLException e) {
            ConsoleUI.error("Could not load MySQL customer information: " + e.getMessage());
        }

        ConsoleUI.banner("Drivers and vehicles (this application session)");
        ConsoleUI.info("Session customer records:");
        printList(context.customers().listAll(), "No session customers. Customer portal accounts are listed above.");
        for (Driver driver : context.drivers().listAll()) System.out.println("  " + driver);
        for (Vehicle vehicle : context.vehicles().listAll()) System.out.println("  " + vehicle);
        if (context.drivers().listAll().isEmpty()) ConsoleUI.info("No in-memory drivers yet. Load demo logistics data to populate the operations view.");

        ConsoleUI.banner("Shifts, routes and dispatch status (this application session)");
        for (Shift shift : context.shifts().listAll()) System.out.println("  " + shift);
        for (Route route : context.routes().listAll()) System.out.println("  " + route);
        for (Parcel parcel : context.parcels().listAll()) System.out.println("  " + parcel);
        if (context.shifts().listAll().isEmpty() && context.routes().listAll().isEmpty()
                && context.parcels().listAll().isEmpty()) ConsoleUI.info("No operations have run in this session yet.");
    }

}
