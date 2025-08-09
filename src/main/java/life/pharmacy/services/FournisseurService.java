package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Fournisseur;
import life.pharmacy.utils.ExcelExporter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FournisseurService {
    private static final Object LOCK = new Object();

    static {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS fournisseurs (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nom TEXT NOT NULL,
                            contact TEXT,
                            adresse TEXT
                        )
                    """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Fournisseur> getAll() {
        synchronized (LOCK) {
            List<Fournisseur> fournisseurs = new ArrayList<>();
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM fournisseurs ORDER BY nom ASC")) {
                while (rs.next()) {
                    fournisseurs.add(new Fournisseur(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("contact"),
                            rs.getString("adresse")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return fournisseurs;
        }
    }

    public static void insert(Fournisseur fournisseur) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO fournisseurs (nom, contact, adresse) VALUES (?, ?, ?)")) {
                pstmt.setString(1, fournisseur.getNom());
                pstmt.setString(2, fournisseur.getContact());
                pstmt.setString(3, fournisseur.getAdresse());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void update(Fournisseur fournisseur) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE fournisseurs SET nom=?, contact=?, adresse=? WHERE id=?")) {
                pstmt.setString(1, fournisseur.getNom());
                pstmt.setString(2, fournisseur.getContact());
                pstmt.setString(3, fournisseur.getAdresse());
                pstmt.setInt(4, fournisseur.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(int id) {
        synchronized (LOCK) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM fournisseurs WHERE id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // ... dans FournisseurService

    public static void importCSV(File excelFile) {
        try {
            List<List<String>> rows = ExcelExporter.read(excelFile);
            if (rows == null || rows.size() <= 1) return;

            // En-tête présumé : Nom | Adresse | Telephone
            for (int i = 1; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                try {
                    String nom = r.size() > 0 ? r.get(0).trim() : "";
                    String adresse = r.size() > 1 ? r.get(1).trim() : "";
                    String contact = r.size() > 2 ? r.get(2).trim() : "";

                    var f = new life.pharmacy.models.Fournisseur();
                    try { f.setNom(nom); } catch (Exception ignored) {}
                    try { f.setAdresse(adresse); } catch (Exception ignored) {}
                    try { f.setContact(contact); } catch (Exception ignored) {}

                    insert(f);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void exportCSV(File excelFile) {
        try {
            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("Nom", "Adresse", "Contact"));
            for (var f : getAll()) {
                rows.add(Arrays.asList(
                        safe(f.getNom()),
                        safe(f.getAdresse()),
                        safe(f.getContact())
                ));
            }
            ExcelExporter.write(rows, excelFile);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String safe(String s) { return s == null ? "" : s; }


}
