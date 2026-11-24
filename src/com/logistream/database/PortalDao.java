package com.logistream.database;

import com.logistream.model.Customer;
import com.logistream.repository.MemoryRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** JDBC persistence for portal accounts and customer parcel requests. */
public class PortalDao {
    private final DatabaseConnection database;

    public PortalDao(DatabaseConnection database) {
        this.database = database;
    }

    public void initialize() throws SQLException {
        try (Connection c = database.open(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS logistream_customers ("
                    + "id VARCHAR(20) PRIMARY KEY, name VARCHAR(80) NOT NULL, phone VARCHAR(20) NOT NULL, "
                    + "address VARCHAR(120) NOT NULL, zone VARCHAR(40) NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS logistream_customer_accounts ("
                    + "username VARCHAR(60) PRIMARY KEY, customer_id VARCHAR(20) NOT NULL UNIQUE, "
                    + "password_salt VARCHAR(64) NOT NULL, password_hash VARCHAR(128) NOT NULL, "
                    + "FOREIGN KEY (customer_id) REFERENCES logistream_customers(id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS logistream_portal_parcels ("
                    + "id VARCHAR(20) PRIMARY KEY, customer_id VARCHAR(20) NOT NULL, type VARCHAR(20) NOT NULL, "
                    + "destination VARCHAR(80) NOT NULL, weight_kg DOUBLE NOT NULL, status VARCHAR(20) NOT NULL, "
                    + "driver_id VARCHAR(20) NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (customer_id) REFERENCES logistream_customers(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS logistream_staff_accounts ("
                    + "username VARCHAR(60) PRIMARY KEY, password_salt VARCHAR(64) NOT NULL, "
                    + "password_hash VARCHAR(128) NOT NULL)");
        }
        seedStaffAccountIfNeeded();
    }

    private void seedStaffAccountIfNeeded() throws SQLException {
        int accountCount;
        try (Connection c = database.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM logistream_staff_accounts")) {
            rs.next();
            accountCount = rs.getInt(1);
        }
        if (accountCount == 0) {
            String salt = newSalt();
            try (Connection c = database.open(); PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO logistream_staff_accounts (username, password_salt, password_hash) VALUES (?, ?, ?)")) {
                insert.setString(1, DatabaseConnection.STAFF_USERNAME);
                insert.setString(2, salt);
                insert.setString(3, hashPassword(DatabaseConnection.STAFF_PASSWORD, salt));
                insert.executeUpdate();
            }
        }
    }

    public Customer registerCustomer(String username, String password, String name,
                                     String phone, String address, String zone) throws SQLException {
        String id = nextCustomerId();
        String salt = newSalt();
        try (Connection c = database.open()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement customer = c.prepareStatement(
                        "INSERT INTO logistream_customers (id, name, phone, address, zone) VALUES (?, ?, ?, ?, ?)")) {
                    customer.setString(1, id);
                    customer.setString(2, name);
                    customer.setString(3, phone);
                    customer.setString(4, address);
                    customer.setString(5, zone);
                    customer.executeUpdate();
                }
                try (PreparedStatement account = c.prepareStatement(
                        "INSERT INTO logistream_customer_accounts (username, customer_id, password_salt, password_hash) VALUES (?, ?, ?, ?)")) {
                    account.setString(1, username);
                    account.setString(2, id);
                    account.setString(3, salt);
                    account.setString(4, hashPassword(password, salt));
                    account.executeUpdate();
                }
                c.commit();
                return new Customer(id, name, phone, address, zone);
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private String nextCustomerId() throws SQLException {
        List<String> ids = new ArrayList<String>();
        try (Connection c = database.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id FROM logistream_customers")) {
            while (rs.next()) ids.add(rs.getString(1));
        }
        return MemoryRepository.nextId("CUS", ids);
    }

    public Customer loginCustomer(String username, String password) throws SQLException {
        String sql = "SELECT c.id, c.name, c.phone, c.address, c.zone, a.password_salt, a.password_hash "
                + "FROM logistream_customer_accounts a JOIN logistream_customers c ON c.id = a.customer_id WHERE a.username = ?";
        try (Connection c = database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next() || !passwordMatches(password, rs.getString("password_salt"), rs.getString("password_hash"))) return null;
                return new Customer(rs.getString("id"), rs.getString("name"), rs.getString("phone"),
                        rs.getString("address"), rs.getString("zone"));
            }
        }
    }

    public boolean loginStaff(String username, String password) throws SQLException {
        try (Connection c = database.open();
             PreparedStatement p = c.prepareStatement("SELECT password_salt, password_hash FROM logistream_staff_accounts WHERE username = ?")) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() && passwordMatches(password, rs.getString(1), rs.getString(2));
            }
        }
    }

    public List<Customer> findAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<Customer>();
        try (Connection c = database.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, name, phone, address, zone FROM logistream_customers ORDER BY name")) {
            while (rs.next()) customers.add(new Customer(rs.getString("id"), rs.getString("name"),
                    rs.getString("phone"), rs.getString("address"), rs.getString("zone")));
        }
        return customers;
    }

    public List<PortalParcel> findCustomerParcels(String customerId) throws SQLException {
        return findParcels("WHERE p.customer_id = ?", customerId);
    }

    public List<PortalParcel> findAllParcels() throws SQLException {
        return findParcels("", null);
    }

    private List<PortalParcel> findParcels(String filter, String customerId) throws SQLException {
        List<PortalParcel> parcels = new ArrayList<PortalParcel>();
        String sql = "SELECT p.id, p.customer_id, c.name, p.type, p.destination, p.weight_kg, p.status, p.driver_id "
                + "FROM logistream_portal_parcels p JOIN logistream_customers c ON c.id = p.customer_id " + filter + " ORDER BY p.created_at DESC";
        try (Connection c = database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            if (customerId != null) p.setString(1, customerId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) parcels.add(new PortalParcel(rs.getString("id"), rs.getString("customer_id"),
                        rs.getString("name"), rs.getString("type"), rs.getString("destination"),
                        rs.getDouble("weight_kg"), rs.getString("status"), rs.getString("driver_id")));
            }
        }
        return parcels;
    }

    public PortalParcel registerParcel(Customer customer, String type, String destination,
                                       double weightKg) throws SQLException {
        List<String> ids = new ArrayList<String>();
        try (Connection c = database.open(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT id FROM logistream_portal_parcels")) {
            while (rs.next()) ids.add(rs.getString(1));
        }
        String id = MemoryRepository.nextId("PCL", ids);
        try (Connection c = database.open(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO logistream_portal_parcels (id, customer_id, type, destination, weight_kg, status) VALUES (?, ?, ?, ?, ?, 'REGISTERED')")) {
            p.setString(1, id);
            p.setString(2, customer.getId());
            p.setString(3, type);
            p.setString(4, destination);
            p.setDouble(5, weightKg);
            p.executeUpdate();
        }
        return new PortalParcel(id, customer.getId(), customer.getName(), type, destination, weightKg, "REGISTERED", null);
    }

    public boolean updateParcelStatus(String parcelId, String status, String driverId) throws SQLException {
        String sql = "UPDATE logistream_portal_parcels SET status = ?, driver_id = ? WHERE id = ?";
        try (Connection c = database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, status);
            if (driverId == null || driverId.trim().isEmpty()) p.setNull(2, java.sql.Types.VARCHAR);
            else p.setString(2, driverId.trim());
            p.setString(3, parcelId);
            return p.executeUpdate() > 0;
        }
    }

    private String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String encodedSalt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(encodedSalt), 120000, 256);
        try {
            return Base64.getEncoder().encodeToString(
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash the account password.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private boolean passwordMatches(String password, String salt, String expectedHash) {
        byte[] actual = Base64.getDecoder().decode(hashPassword(password, salt));
        byte[] expected = Base64.getDecoder().decode(expectedHash);
        return MessageDigest.isEqual(actual, expected);
    }

}
