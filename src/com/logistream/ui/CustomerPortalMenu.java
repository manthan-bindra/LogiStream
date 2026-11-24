package com.logistream.ui;

import com.logistream.util.LogiStreamUtil;

import com.logistream.database.PortalDao;
import com.logistream.database.PortalParcel;
import com.logistream.model.Customer;
import com.logistream.util.InputHelper;
import java.sql.SQLException;
import java.util.List;

/** Logged-in customer actions: track parcels and register new deliveries. */
public class CustomerPortalMenu extends BaseMenu {
    private final PortalDao portal;
    private final Customer customer;

    public CustomerPortalMenu(InputHelper in, PortalDao portal, Customer customer) {
        super(in);
        this.portal = portal;
        this.customer = customer;
    }

    /** Customer registration/login screen, folded into this portal to keep the UI class count down. */
    public static void openAccess(InputHelper in, PortalDao portal) {
        while (true) {
            LogiStreamUtil.banner("Customer Access");
            System.out.println("   1. New customer registration");
            System.out.println("   2. Existing customer login");
            System.out.println("   0. Back");
            int choice = in.readInt("Choose", 0, 2);
            if (choice == 0) return;
            try {
                Customer customer;
                if (choice == 1) {
                    String name = in.readText("Full name");
                    String phone = in.readPhone("Phone");
                    String address = in.readText("Address");
                    String zone = in.readText("Delivery zone");
                    String username = in.readText("Choose a username");
                    String password = in.readText("Choose a password (at least 6 characters)");
                    if (password.length() < 6) {
                        LogiStreamUtil.error("Password must contain at least 6 characters.");
                        continue;
                    }
                    customer = portal.registerCustomer(username, password, name, phone, address, zone);
                    LogiStreamUtil.success("Account created. Your customer ID is " + customer.getId() + ".");
                } else {
                    String username = in.readText("Username");
                    String password = in.readText("Password");
                    customer = portal.loginCustomer(username, password);
                    if (customer == null) {
                        LogiStreamUtil.error("Username or password is incorrect.");
                        continue;
                    }
                    LogiStreamUtil.success("Welcome back, " + customer.getName() + ".");
                }
                new CustomerPortalMenu(in, portal, customer).run();
            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("23"))
                    LogiStreamUtil.error("That username or customer record already exists. Try another username.");
                else LogiStreamUtil.error("Could not complete the customer request: " + e.getMessage());
            }
        }
    }

    @Override public String title() { return "Customer Portal - " + customer.getName(); }
    @Override protected String[] options() {
        return new String[]{"View my parcels and delivery status", "Register a new parcel"};
    }

    @Override protected void handle(int choice) {
        try {
            if (choice == 1) {
                List<PortalParcel> parcels = portal.findCustomerParcels(customer.getId());
                printList(parcels, "You have not registered any parcels yet.");
            } else registerParcel();
        } catch (SQLException e) {
            LogiStreamUtil.error("Could not access parcel records: " + e.getMessage());
        }
    }

    private void registerParcel() throws SQLException {
        System.out.println("  Parcel types: 1=STANDARD  2=EXPRESS  3=FRAGILE");
        int typeChoice = in.readInt("Choose type", 1, 3);
        String type = new String[]{"STANDARD", "EXPRESS", "FRAGILE"}[typeChoice - 1];
        String destination = in.readText("Destination zone");
        double weight = in.readDouble("Weight in kg", 0.1);
        PortalParcel parcel = portal.registerParcel(customer, type, destination, weight);
        LogiStreamUtil.success("Parcel registered: " + parcel.getId() + " (status: REGISTERED).");
    }
}
