package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Client;
import life.pharmacy.models.Produit;
import life.pharmacy.utils.ExcelExporter;
import life.pharmacy.utils.ExcelImporter;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientService {

    private static final Object LOCK = new Object();

    static {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS clients (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nom TEXT NOT NULL,
                            telephone TEXT,
                            email TEXT
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Client> getAll() {
        synchronized (LOCK) {
            List<Client> clients = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM clients ORDER BY nom ASC")) {
                while (rs.next()) {
                    clients.add(new Client(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("telephone"),
                            rs.getString("email")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return clients;
        }
    }

    public static void insert(Client client) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO clients (nom, telephone, email) VALUES (?, ?, ?)")) {
                pstmt.setString(1, client.getNom());
                pstmt.setString(2, client.getTelephone());
                pstmt.setString(3, client.getEmail());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // === Nouvelle méthode ===
    public static Client getById(int id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Client c = new Client();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setEmail(rs.getString("email"));
                c.setTelephone(rs.getString("telephone"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void update(Client client) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE clients SET nom=?, telephone=?, email=? WHERE id=?")) {
                pstmt.setString(1, client.getNom());
                pstmt.setString(2, client.getTelephone());
                pstmt.setString(3, client.getEmail());
                pstmt.setInt(4, client.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(int id) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM clients WHERE id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void importCSV(File excelFile) {
        try {
            List<List<String>> rows = ExcelExporter.read(excelFile);
            if (rows == null || rows.size() <= 1) return;

            // En-tête présumé : Nom | Email | Telephone
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                try {
                    String nom = r.size() > 0 ? r.get(0).trim() : "";
                    String email = r.size() > 1 ? r.get(1).trim() : "";
                    String telephone = r.size() > 2 ? r.get(2).trim() : "";

                    // Construction via setters (plus robuste)
                    life.pharmacy.models.Client c = new life.pharmacy.models.Client();
                    try { c.setNom(nom); } catch (NoSuchMethodError | Exception ignored) {}
                    try { c.setEmail(email); } catch (Exception ignored) {}
                    try { c.setTelephone(telephone); } catch (Exception ignored) {}

                    insert(c); // adapte si ta méthode s'appelle autrement
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportCSV(File excelFile) {
        try {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("Nom", "Email", "Telephone"));
            for (life.pharmacy.models.Client c : getAll()) {
                rows.add(Arrays.asList(
                        safe(c.getNom()),
                        safe(c.getEmail()),
                        safe(c.getTelephone())
                ));
            }
            ExcelExporter.write(rows, excelFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }




}
