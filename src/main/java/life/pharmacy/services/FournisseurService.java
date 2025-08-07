package life.pharmacy.services;

import life.pharmacy.config.Database;
import life.pharmacy.models.Fournisseur;

import java.sql.*;
import java.util.ArrayList;
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
}
