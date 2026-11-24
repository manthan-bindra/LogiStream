package com.logistream;

import com.logistream.util.LogiStreamUtil;

import com.logistream.app.AppContext;
import com.logistream.database.DatabaseConnection;
import com.logistream.database.PortalDao;
import com.logistream.ui.CustomerPortalMenu;
import com.logistream.ui.StaffPortalMenu;
import com.logistream.util.InputHelper;
import java.sql.SQLException;
import java.util.Scanner;

/** Console entry point for LogiStream's staff and customer portals. */
public class LogiStreamApp {
    public static void main(String[] args) {
        InputHelper in = new InputHelper(new Scanner(System.in));
        PortalDao portal = new PortalDao(new DatabaseConnection());
        try {
            portal.initialize();
        } catch (SQLException e) {
            LogiStreamUtil.error("Cannot initialize the MySQL database: " + e.getMessage());
            LogiStreamUtil.info("Start MySQL, create the logistream database, and check .env before launching again.");
            return;
        }

        AppContext context = new AppContext();
        while (true) {
            LogiStreamUtil.banner("LogiStream - Login");
            System.out.println("  1. Login as staff member");
            System.out.println("  2. Continue as customer");
            System.out.println("  0. Exit");
            int choice = in.readInt("Choose", 0, 2);
            if (choice == 0) break;
            if (choice == 2) {
                CustomerPortalMenu.openAccess(in, portal);
                continue;
            }
            String username = in.readText("Staff username");
            String password = in.readText("Staff password");
            try {
                if (!portal.loginStaff(username, password)) {
                    LogiStreamUtil.error("Staff username or password is incorrect.");
                    continue;
                }
                LogiStreamUtil.success("Staff login successful.");
                new StaffPortalMenu(in, portal, context).run();
            } catch (SQLException e) {
                LogiStreamUtil.error("Could not verify staff login: " + e.getMessage());
            }
        }
        System.out.println("\nThank you for using LogiStream.");
    }
}
